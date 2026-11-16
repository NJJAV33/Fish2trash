package game;

/** The screens the game can be on. */
public enum GameState {
    MENU,
    /** Picking a level that is already open, to start a run from there. */
    LEVEL_SELECT,
    HOW_TO,
    /** Story pages before a chapter. */
    STORY,
    PLAYING,
    LEVEL_CLEAR,
    GAME_OVER,
    /** The ending and the run recap, after chapter 3. */
    ENDING,
    /** Typing a name for a run that made the high-score list. */
    NAME_ENTRY,
    HIGH_SCORES
}
