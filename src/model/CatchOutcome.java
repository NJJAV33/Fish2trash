package model;

/**
 * What happens when a tool touches an ocean object.
 *
 * Each outcome knows which CatchResolver method to call, so the game never
 * needs a switch over outcomes.
 */
public enum CatchOutcome {

    /** The tool grabs the object. */
    CATCH {
        @Override
        public void resolve(CatchResolver resolver, OceanObject object) {
            resolver.onCatch(object);
        }
    },
    /** Wrong tool: the object slips off and the hook comes back empty. */
    SLIP {
        @Override
        public void resolve(CatchResolver resolver, OceanObject object) {
            resolver.onSlip(object);
        }
    },
    /** The tool goes straight through, as if the object were not there. */
    PASS {
        @Override
        public void resolve(CatchResolver resolver, OceanObject object) {
            resolver.onPass(object);
        }
    };

    public abstract void resolve(CatchResolver resolver, OceanObject object);
}
