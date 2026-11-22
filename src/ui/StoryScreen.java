package ui;

import game.LevelResult;
import game.GameSession;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import model.EquipmentType;
import model.LevelData;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.StoryBoxPainter;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/**
 * The story pages shown before a level is played: how the last level ended
 * (if there was one), the page that opens a new chapter with the new tool
 * the crew gets for it, and what this level is about.
 */
public final class StoryScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int NO_GEAR_PAGE = -1;
    private static final int BADGE_X = 186;
    private static final int BADGE_Y = 160;
    private static final int BADGE_RADIUS = 36;
    private static final double BADGE_TOOL_SCALE = 1.6;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final StoryBoxPainter box = new StoryBoxPainter();
    private final TextPager pager = new TextPager();
    private final List<String> pageTitles = new ArrayList<>();
    private final List<ChapterPalette> pageBackdrops = new ArrayList<>();
    private int gearPageIndex;       // the page that presents a new tool, or NO_GEAR_PAGE
    private EquipmentType gearTool;  // the tool on that page
    private double time;

    public StoryScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        GameSession session = screens.getSession();
        LevelData next = session.getLevelData();
        int chapter = next.getChapter();
        int level = next.getLevel();
        LevelResult last = session.getLastResult();
        boolean retrying = last != null && !last.isCleared();
        ChapterPalette look = lookOf(chapter, level - 1);
        List<String> pages = new ArrayList<>();
        pageTitles.clear();
        pageBackdrops.clear();
        gearPageIndex = NO_GEAR_PAGE;

        // A retry goes straight to the level's own page. The opening crawl
        // belongs to a run from the very start, not one picked from the level select.
        if (!retrying && last == null && next.getNumber() == 1) {
            addPage(pages, StoryText.GAME_TITLE, StoryText.CRAWL, look);
        }
        if (!retrying && last != null) {
            // The page about the cleared level shows that place as clean as it has become.
            addPage(pages, StoryText.levelTitle(last.getChapter(), last.getLevel()),
                    StoryText.levelOutro(last.getChapter(), last.getLevel()),
                    lookOf(last.getChapter(), last.getLevel()));
        }
        if (!retrying && level == 1) {
            // A new chapter: the page that opens it, and the tool the crew gets for it.
            addPage(pages, StoryText.chapterTitle(chapter), StoryText.chapterOpening(chapter), look);
            String gearText = StoryText.gearPage(chapter);
            if (GameConstants.EQUIPMENT_ENABLED && !gearText.isEmpty()) {
                gearTool = EquipmentType.newestIn(chapter);
                gearPageIndex = pages.size();
                addPage(pages, StoryText.GEAR_TITLE + ": " + gearTool.getDisplayName().toUpperCase(), gearText, look);
            }
        }
        addPage(pages, StoryText.levelTitle(chapter, level), StoryText.levelIntro(chapter, level), look);

        pager.start(pages.toArray(new String[0]));
        time = 0;
    }

    /** How a chapter's sea looks once the given number of its levels are cleared. */
    static ChapterPalette lookOf(int chapter, int levelsCleared) {
        return ChapterPalette.forChapter(chapter)
                .cleanedBy((double) levelsCleared / LevelData.LEVELS_PER_CHAPTER);
    }

    private void addPage(List<String> pages, String title, String text, ChapterPalette backdropLook) {
        pages.add(text);
        pageTitles.add(title);
        pageBackdrops.add(backdropLook);
    }

    @Override
    public void update(double dt) {
        time += dt;
        pager.update(dt);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            int pageBefore = pager.getPageIndex();
            if (pager.advance()) {
                screens.go(GameState.PLAYING);   // no pages left: time to play
            }
            // Turning to the page with the new tool gets its own little fanfare.
            boolean openedGearPage = pager.getPageIndex() != pageBefore && pager.getPageIndex() == gearPageIndex;
            screens.getAudio().play(openedGearPage ? SoundEffect.TOOL_UNLOCK : SoundEffect.MENU_SELECT);
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            screens.go(GameState.PLAYING);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdrop(g, pageBackdrops.get(pager.getPageIndex()));
        g.setColor(UiPalette.SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        // Title card
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(130, 110, 540, 100, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(130, 110, 540, 100, 18, 18);
        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, pageTitles.get(pager.getPageIndex()), CENTER_X, 165);
        if (pager.getPageIndex() == gearPageIndex) {
            paintGearBadge(g);
        }
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, (pager.getPageIndex() + 1) + " / " + pager.getPageCount(), CENTER_X, 192);
        TextDraw.rightAligned(g, StoryText.SKIP_PROMPT, GameConstants.WORLD_WIDTH - 20, 24);

        String prompt = pager.isPageComplete() ? StoryText.NEXT_PROMPT : null;
        box.paint(g, pager.getVisibleText(), pager.getFullText(), prompt, time);
    }

    /** A round badge on the title card with the new tool in it, drawn large and working. */
    private void paintGearBadge(Graphics2D g) {
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillOval(BADGE_X - BADGE_RADIUS, BADGE_Y - BADGE_RADIUS, 2 * BADGE_RADIUS, 2 * BADGE_RADIUS);
        g.setColor(UiPalette.GOLD);
        g.drawOval(BADGE_X - BADGE_RADIUS, BADGE_Y - BADGE_RADIUS, 2 * BADGE_RADIUS, 2 * BADGE_RADIUS);

        Graphics2D tool = (Graphics2D) g.create();
        tool.translate(BADGE_X, BADGE_Y);
        tool.scale(BADGE_TOOL_SCALE, BADGE_TOOL_SCALE);
        backdrop.getPainters().paintTool(tool, gearTool, time, true);
        tool.dispose();
    }

    @Override
    public String getAccessibleName() {
        int index = pager.getPageIndex();
        return index >= 0 && index < pageTitles.size() ? pageTitles.get(index) : "Story";
    }

    @Override
    public String getAccessibleDescription() {
        return pager.getFullText().replace('\n', ' ');
    }
}
