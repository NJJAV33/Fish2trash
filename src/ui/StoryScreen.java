package ui;

import game.ChapterResult;
import game.GameSession;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
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
 * ended (if there was one), then what this chapter is about.
 */
public final class StoryScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final StoryBoxPainter box = new StoryBoxPainter();
    private final TextPager pager = new TextPager();
    private final List<String> pageTitles = new ArrayList<>();
    private final List<ChapterPalette> pageBackdrops = new ArrayList<>();
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
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            if (pager.advance()) {
                screens.go(GameState.PLAYING);   // no pages left: time to play
            }
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
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, (pager.getPageIndex() + 1) + " / " + pager.getPageCount(), CENTER_X, 192);
        TextDraw.rightAligned(g, StoryText.SKIP_PROMPT, GameConstants.WORLD_WIDTH - 20, 24);

        String prompt = pager.isPageComplete() ? StoryText.NEXT_PROMPT : null;
        box.paint(g, pager.getVisibleText(), pager.getFullText(), prompt, time);
    }
}
