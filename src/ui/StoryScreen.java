package ui;

import game.ChapterResult;
import game.GameSession;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import model.EquipmentType;
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
 * The story pages shown before a chapter is played: how the last chapter
 * ended (if there was one), what this chapter is about, and the new tool the
 * crew gets for it (if there is one).
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
        int chapter = session.getChapterNumber();
        ChapterResult last = session.getLastResult();
        boolean retrying = last != null && !last.isCleared();

        List<String> pages = new ArrayList<>();
        pageTitles.clear();
        pageBackdrops.clear();
        // A retry goes straight to the chapter's own pages.
        if (!retrying && chapter == 1) {
            addPage(pages, StoryText.GAME_TITLE, StoryText.CRAWL, ChapterPalette.SHALLOWS);
        }
        if (!retrying && chapter > 1) {
            // The page about the cleared chapter shows that place cleaned up.
            addPage(pages, StoryText.chapterTitle(chapter - 1), StoryText.outro(chapter - 1),
                    ChapterPalette.forChapter(chapter - 1).getCleaned());
        }
        for (String page : StoryText.introPages(chapter)) {
            addPage(pages, StoryText.chapterTitle(chapter), page, ChapterPalette.forChapter(chapter));
        }
        gearPageIndex = NO_GEAR_PAGE;
        String gearText = StoryText.gearPage(chapter);
        if (GameConstants.EQUIPMENT_ENABLED && !gearText.isEmpty()) {
            gearTool = EquipmentType.newestIn(chapter);
            gearPageIndex = pages.size();
            addPage(pages, StoryText.GEAR_TITLE + ": " + gearTool.getDisplayName().toUpperCase(),
                    gearText, ChapterPalette.forChapter(chapter));
        }
        pager.start(pages.toArray(new String[0]));
        time = 0;
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
}
