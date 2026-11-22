import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps docs/class-diagram.md honest: every class in src/ must be mentioned
 * on that page, and every class the diagrams draw must really exist.
 * Adding, renaming or deleting a class without updating the page fails here.
 */
public final class DiagramTest {

    private static final Path SOURCE_ROOT = Paths.get("src");
    private static final Path DIAGRAM_PAGE = Paths.get("docs", "class-diagram.md");

    // "class Hook" or "class Hook {" inside a diagram
    private static final Pattern DECLARED = Pattern.compile("^\\s*class\\s+(\\w+)");
    // "Boat *-- Hook", "Screen <|.. MainMenu", "A ..> B : label" and so on
    private static final Pattern RELATED =
            Pattern.compile("^\\s*(\\w+)\\s+(<\\|--|<\\|\\.\\.|\\*--|o--|\\.\\.>|-->)\\s+(\\w+)");
    private static final Pattern ENUM_CONST = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    private static final Pattern WORD = Pattern.compile("\\b[A-Z]\\w+\\b");

    private DiagramTest() {
        // only static methods
    }

    public static void run() {
        Set<String> sourceClasses = classNamesIn(SOURCE_ROOT);
        Map<String, Class<?>> sourceEnums = enumClassesIn(SOURCE_ROOT);
        List<String> page = readLines(DIAGRAM_PAGE);

        Set<String> mentioned = new TreeSet<>();
        Set<String> drawn = new TreeSet<>();
        Map<String, List<String>> diagramEnums = new LinkedHashMap<>();
        boolean insideDiagram = false;
        String currentClass = null;
        boolean currentIsEnum = false;
        List<String> currentConsts = new ArrayList<>();

        for (String line : page) {
            Matcher words = WORD.matcher(line);
            while (words.find()) {
                mentioned.add(words.group());
            }
            if (line.startsWith("```")) {
                insideDiagram = line.startsWith("```mermaid");
                currentClass = null;
                currentIsEnum = false;
                continue;
            }
            if (insideDiagram) {
                Matcher declared = DECLARED.matcher(line);
                if (declared.find()) {
                    drawn.add(declared.group(1));
                    if (line.trim().endsWith("{")) {
                        currentClass = declared.group(1);
                        currentIsEnum = false;
                        currentConsts = new ArrayList<>();
                    }
                    continue;
                }
                if (currentClass != null) {
                    String trimmed = line.trim();
                    if (trimmed.equals("}")) {
                        if (currentIsEnum && !currentConsts.isEmpty()) {
                            diagramEnums.put(currentClass, currentConsts);
                        }
                        currentClass = null;
                        currentIsEnum = false;
                    } else if (trimmed.equals("<<enumeration>>")) {
                        currentIsEnum = true;
                    } else if (currentIsEnum && ENUM_CONST.matcher(trimmed).matches()) {
                        currentConsts.add(trimmed);
                    }
                }
                Matcher related = RELATED.matcher(line);
                if (related.find()) {
                    drawn.add(related.group(1));
                    drawn.add(related.group(3));
                }
            }
        }

        Set<String> missingFromPage = new TreeSet<>(sourceClasses);
        missingFromPage.removeAll(mentioned);
        Set<String> notInCode = new TreeSet<>(drawn);
        notInCode.removeAll(sourceClasses);

        Check.assertTrue("source classes were found", sourceClasses.size() >= 50);
        Check.assertTrue("the diagrams draw classes", drawn.size() >= 40);
        Check.assertTrue("every class in src/ is on the diagram page, missing: " + missingFromPage,
                missingFromPage.isEmpty());
        Check.assertTrue("every class drawn on the page exists in src/, unknown: " + notInCode,
                notInCode.isEmpty());

        Check.assertEquals("every enum in src/ has its constants documented on the diagram page",
                sourceEnums.keySet(), new TreeSet<>(diagramEnums.keySet()));
        for (Map.Entry<String, Class<?>> entry : sourceEnums.entrySet()) {
            List<String> expected = new ArrayList<>();
            for (Object value : entry.getValue().getEnumConstants()) {
                expected.add(value.toString());
            }
            Check.assertEquals("enum constants match for " + entry.getKey(),
                    expected, diagramEnums.get(entry.getKey()));
        }
    }

    private static Map<String, Class<?>> enumClassesIn(Path root) {
        Map<String, Class<?>> enums = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList());
            for (Path file : files) {
                Path relative = root.relativize(file);
                String fqcn = relative.toString()
                        .replace('/', '.')
                        .replace('\\', '.');
                fqcn = fqcn.substring(0, fqcn.length() - ".java".length());
                Class<?> type = Class.forName(fqcn, false, DiagramTest.class.getClassLoader());
                if (type.isEnum()) {
                    enums.put(type.getSimpleName(), type);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return enums;
    }

    /** The name of every .java file under the folder, without ".java". */
    private static Set<String> classNamesIn(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> readLines(Path file) {
        try {
            return new ArrayList<>(Files.readAllLines(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
