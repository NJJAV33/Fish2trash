package model;

/**
 * The kinds of object that live in the ocean.
 *
 * Each kind knows how to build its own object (create), so the spawner never
 * needs an if/else or switch over kinds. Other code uses the kind as a lookup
 * key (for example, to pick the painter that draws an object), so nobody ever
 * has to ask "is this a Fish?".
 */
public enum ObjectKind {

    PLASTIC_BOTTLE(true) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new PlasticBottle(x, y, speedX);
        }
    },
    CAN(true) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new Can(x, y, speedX);
        }
    },
    FISHING_NET(true) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new FishingNet(x, y, speedX);
        }
    },
    SMALL_FISH(false) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new SmallFish(x, y, speedX);
        }
    },
    BIG_FISH(false) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new BigFish(x, y, speedX);
        }
    },
    PUFFERFISH(false) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new Pufferfish(x, y, speedX);
        }
    },
    BARRACUDA(false) {
        @Override
        public OceanObject create(double x, double y, double speedX) {
            return new Barracuda(x, y, speedX);
        }
    };

    private final boolean trash;

    ObjectKind(boolean trash) {
        this.trash = trash;
    }

    /** True for things the crew should collect, false for wildlife. */
    public boolean isTrash() {
        return trash;
    }

    /** Builds a new object of this kind at the given place and speed. */
    public abstract OceanObject create(double x, double y, double speedX);
}
