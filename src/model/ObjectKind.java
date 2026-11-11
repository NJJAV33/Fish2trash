package model;

/**
 * The kinds of object that live in the ocean.
 *
 * Other code uses the kind as a lookup key (for example, to pick the painter
 * that draws an object), so nobody ever needs to ask "is this a Fish?".
 */
public enum ObjectKind {
    CAN,
    SMALL_FISH
}
