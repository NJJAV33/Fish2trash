package game;

/** The screens the game can be on. */
public enum GameState {
    MENU,
    HOW_TO,
    /** Story pages before a chapter. */
    STORY,
    PLAYING,
    CHAPTER_CLEAR,
    GAME_OVER,
    /** The ending and the run recap, after chapter 3. */
    ENDING,
    /** Typing a name for a run that made the high-score list. */
    NAME_ENTRY,
    HIGH_SCORES
}
