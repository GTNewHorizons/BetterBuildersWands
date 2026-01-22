package portablejim.bbw.compat.ztones;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public class Ztones {

    private static final Item OFANIX = GameRegistry.findItem("Ztones", "ofanix");
    private static boolean isLoaded = false;

    public static void init() {
        isLoaded = (OFANIX != null);
    }

    public static Item getOfanix() {
        return OFANIX;
    }

    public static boolean isLoaded() {
        return isLoaded;
    }
}
