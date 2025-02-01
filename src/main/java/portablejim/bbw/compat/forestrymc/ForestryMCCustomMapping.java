package portablejim.bbw.compat.forestrymc;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import forestry.arboriculture.blocks.BlockRegistryArboriculture;
import forestry.arboriculture.tiles.TileWood;
import forestry.plugins.PluginArboriculture;
import forestry.plugins.PluginManager;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.conversion.CustomMapping;
import portablejim.bbw.shims.IWorldShim;

public class ForestryMCCustomMapping extends CustomMapping {

    public ForestryMCCustomMapping(Block lookBlock, int meta) {
        super(lookBlock, meta, new ItemStack(Item.getItemFromBlock(lookBlock)), lookBlock, meta, true);
    }

    @Override
    public ItemStack getItems(IWorldShim world, Point3d point) {
        TileEntity tileEntity = world.getTile(point);
        if (tileEntity instanceof TileWood) {
            return TileWood.getPickBlock(getLookBlock(), world.getWorld(), point.x, point.y, point.z);
        }
        return super.getItems(world, point);
    }

    public static void register() {
        if (PluginManager.Module.ARBORICULTURE.isEnabled()) {
            BlockRegistryArboriculture blocks = PluginArboriculture.blocks;
            registerMappings(blocks.logs, 0, 4, 8);
            registerMappings(blocks.logsFireproof, 0, 4, 8);
            registerMappings(blocks.planks, 0);
            registerMappings(blocks.planks, 0);
            registerMappings(blocks.slabs, 0, 8);
            registerMappings(blocks.slabsFireproof, 0, 8);
            registerMappings(blocks.slabsDouble, 0);
            registerMappings(blocks.slabsDoubleFireproof, 0);
            registerMappings(blocks.stairs, range(7));
            registerMappings(blocks.stairsFireproof, range(7));
            registerMappings(blocks.fences, 0);
            registerMappings(blocks.fencesFireproof, 0);
        }
    }

    private static void registerMappings(Block block, int... metas) {
        for (int meta : metas) {
            BetterBuildersWandsMod.instance.mappingManager.setMapping(new ForestryMCCustomMapping(block, meta));
        }
    }

    private static int[] range(int max) {
        int[] range = new int[max + 1];
        for (int i = 0; i <= max; i++) {
            range[i] = i;
        }
        return range;
    }
}
