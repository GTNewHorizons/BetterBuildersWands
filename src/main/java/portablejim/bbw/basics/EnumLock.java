package portablejim.bbw.basics;

/**
 * Enum for the directions that the wands can extend.
 */
public enum EnumLock {
    NORTHSOUTH(1),
    VERTICAL(2),
    VERTICALEASTWEST(3),
    EASTWEST(4),
    HORIZONTAL(5),
    VERTICALNORTHSOUTH(6),
    NOLOCK(7);

    public final int mask;

    public static final int NORTH_SOUTH_MASK = 1;
    public static final int UP_DOWN_MASK = 2;
    public static final int EAST_WEST_MASK = 4;

    EnumLock(int mask) {
        this.mask = mask;
    }

    public static EnumLock fromMask(int inputMask) {
        EnumLock locks[] = {NORTHSOUTH, VERTICAL, VERTICALEASTWEST, EASTWEST, HORIZONTAL, VERTICALNORTHSOUTH, NOLOCK};

        int safeMask = inputMask & 7;
        return locks[safeMask - 1];
    }
}
