package ui;

/**
 * Shows story pages one at a time with a typewriter effect: the letters of a
 * page appear one after another, and ENTER first completes the page, then
 * moves to the next one.
 */
final class TextPager {

    private static final double LETTERS_PER_SECOND = 40;

    private String[] pages = {""};
    private int page;
    private double lettersShown;

    /** Starts over with a new set of pages. */
    void start(String[] newPages) {
        pages = newPages;
        page = 0;
        lettersShown = 0;
    }

    void update(double dt) {
        lettersShown = Math.min(pages[page].length(), lettersShown + LETTERS_PER_SECOND * dt);
    }

    /**
     * What ENTER does: finish the current page if it is still appearing,
     * otherwise turn to the next page.
     *
     * @return true when there was no next page to turn to (the story is done)
     */
    boolean advance() {
        if (!isPageComplete()) {
            lettersShown = pages[page].length();
            return false;
        }
        if (page < pages.length - 1) {
            page++;
            lettersShown = 0;
            return false;
        }
        return true;
    }

    boolean isPageComplete() {
        return lettersShown >= pages[page].length();
    }

    /** The part of the current page that has appeared so far. */
    String getVisibleText() {
        return pages[page].substring(0, (int) lettersShown);
    }

    /** The whole current page, whether or not it has fully appeared. */
    String getFullText() {
        return pages[page];
    }

    int getPageIndex() {
        return page;
    }

    int getPageCount() {
        return pages.length;
    }
}
