import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads the source code itself and checks the design rules of the project:
 *
 *   1. model and game contain no drawing, window or sound code
 *      (no java.awt, javax.swing or javax.sound imports).
 *   2. Nothing asks "instanceof" - behaviour differences use polymorphism.
 *   3. No public fields, except constants (public static final).
 *
 * It runs with the other tests, so breaking a rule fails the build.
 */
public final class ArchitectureTest {

    private static final Path SOURCE_ROOT = Paths.get("src");
    private static final String[] UI_FREE_PACKAGES = {"model", "game"};
    private static final String[] FORBIDDEN_IMPORTS = {"java.awt", "javax.swing", "javax.sound"};

    private ArchitectureTest() {
        // only static methods
    }

    public static void run() {
        theCheckerItselfCatchesViolations();

        List<String> problems = new ArrayList<>();
        List<Path> files = javaFilesUnder(SOURCE_ROOT);
        for (Path file : files) {
            problems.addAll(problemsIn(file.toString(), readLines(file), isUiFree(file)));
        }
        Check.assertTrue("source files were found under src/", files.size() >= 10);
        Check.assertTrue("design rules hold in src/ " + problems, problems.isEmpty());
    }

    /** Feeds the checker deliberately bad lines, to prove it would notice them. */
    private static void theCheckerItselfCatchesViolations() {
        Check.assertEquals("awt import in the model is caught", 1,
                problemsIn("Bad.java", List.of("import java.awt.Color;"), true).size());
        Check.assertEquals("awt import in the ui is allowed", 0,
                problemsIn("Fine.java", List.of("import java.awt.Color;"), false).size());
        Check.assertEquals("instanceof is caught", 1,
                problemsIn("Bad.java", List.of("if (thing instanceof Fish) {"), false).size());
        Check.assertEquals("a public field is caught", 1,
                problemsIn("Bad.java", List.of("    public int score;"), false).size());
        Check.assertEquals("a public constant is allowed", 0,
                problemsIn("Fine.java", List.of("    public static final int QUOTA = 100;"), false).size());
        Check.assertEquals("a public method is allowed", 0,
                problemsIn("Fine.java", List.of("    public abstract int getScore();"), false).size());
        Check.assertEquals("words inside comments and strings are ignored", 0,
                problemsIn("Fine.java", List.of(
                        "// no instanceof here",
                        "/* public int hidden;",
                        "   import java.awt.Color; */",
                        "String s = \"instanceof\";"), true).size());
    }

    /** Returns one message per broken rule in the given source lines. */
    static List<String> problemsIn(String fileName, List<String> lines, boolean uiFree) {
        List<String> problems = new ArrayList<>();
        boolean insideBlockComment = false;

        for (int i = 0; i < lines.size(); i++) {
            String code = lines.get(i);

            // Cut comments and string contents away, so only real code is checked.
            if (insideBlockComment) {
                int end = code.indexOf("*/");
                if (end < 0) {
                    continue;
                }
                code = code.substring(end + 2);
                insideBlockComment = false;
            }
            code = code.replaceAll("\"(\\\\.|[^\"\\\\])*\"", "\"\"");
            code = code.replaceAll("/\\*.*?\\*/", "");
            int blockStart = code.indexOf("/*");
            if (blockStart >= 0) {
                code = code.substring(0, blockStart);
                insideBlockComment = true;
            }
            int lineComment = code.indexOf("//");
            if (lineComment >= 0) {
                code = code.substring(0, lineComment);
            }
            code = code.trim();

            String where = fileName + ":" + (i + 1);
            if (uiFree && code.startsWith("import ")) {
                for (String forbidden : FORBIDDEN_IMPORTS) {
                    if (code.contains(" " + forbidden + ".")) {
                        problems.add(where + " imports " + forbidden);
                    }
                }
            }
            if (code.matches(".*\\binstanceof\\b.*")) {
                problems.add(where + " uses instanceof");
            }
            if (isPublicField(code)) {
                problems.add(where + " has a public field");
            }
        }
        return problems;
    }

    /** A field declaration is a "public ...;" line with no brackets that is not a constant. */
    private static boolean isPublicField(String code) {
        boolean declaration = code.startsWith("public ") && code.endsWith(";") && !code.contains("(");
        boolean constant = code.contains(" static final ") || code.contains(" final static ");
        return declaration && !constant;
    }

    private static boolean isUiFree(Path file) {
        for (String name : UI_FREE_PACKAGES) {
            if (file.startsWith(SOURCE_ROOT.resolve(name))) {
                return true;
            }
        }
        return false;
    }

    private static List<Path> javaFilesUnder(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
