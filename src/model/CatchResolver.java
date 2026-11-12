package model;

/**
 * Whoever applies the result of a tool touching an object (the GameManager).
 * CatchOutcome calls exactly one of these methods.
 */
public interface CatchResolver {

    void onCatch(OceanObject object);

    void onSlip(OceanObject object);

    void onPass(OceanObject object);
}
