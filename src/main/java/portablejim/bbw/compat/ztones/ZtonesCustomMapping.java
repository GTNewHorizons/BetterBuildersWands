package portablejim.bbw.compat.ztones;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.core.conversion.CustomMapping;

public class ZtonesCustomMapping extends CustomMapping {

    public static final Item OFANIX = GameRegistry.findItem("Ztones", "ofanix");

    public ZtonesCustomMapping(ItemStack items) {
        super(Blocks.cobblestone, 0, items, Blocks.cobblestone, 0);
    }

    public static void register() {
        if (OFANIX != null) {
            BetterBuildersWandsMod.instance.mappingManager.setMapping(new ZtonesCustomMapping(new ItemStack(OFANIX)));
        }
    }
}
