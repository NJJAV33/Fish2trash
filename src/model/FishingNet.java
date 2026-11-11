package model;

import util.GameConstants;

/** A lost fishing net (a "ghost net"). Big, slow and worth the most. */
public class FishingNet extends OceanTrash {

    public FishingNet(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_NET, speedX, GameConstants.POINTS_NET);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.FISHING_NET;
    }
}
