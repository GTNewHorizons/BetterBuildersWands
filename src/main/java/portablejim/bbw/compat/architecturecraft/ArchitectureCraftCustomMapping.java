package portablejim.bbw.compat.architecturecraft;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.registry.GameRegistry;
import gcewing.architecture.common.tile.TileShape;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.conversion.CustomMapping;
import portablejim.bbw.shims.IWorldShim;

public class ArchitectureCraftCustomMapping extends CustomMapping {

    private static final Block SHAPE_BLOCK = GameRegistry.findBlock("ArchitectureCraft", "shape");
    private static final Block GLOWING_SHAPE_BLOCK = GameRegistry.findBlock("ArchitectureCraft", "shapeSE");

    public ArchitectureCraftCustomMapping(Block shapeBlock, int metadata) {
        super(shapeBlock, metadata, new ItemStack(Item.getItemFromBlock(shapeBlock)), shapeBlock, metadata, true);
    }

    @Override
    public ItemStack getItems(IWorldShim world, Point3d point) {
        TileEntity tileEntity = world.getWorld().getTileEntity(point.x, point.y, point.z);
        if (tileEntity instanceof TileShape) {
            TileShape shapeTile = ((TileShape) tileEntity);
            ItemStack itemStack = shapeTile.newItemStack(1);
            itemStack.setItemDamage(getMeta());
            return itemStack;
        }
        return super.getItems(world, point);
    }

    public static void register() {
        BetterBuildersWandsMod.instance.mappingManager.setMapping(new ArchitectureCraftCustomMapping(SHAPE_BLOCK, 0));
        if (GLOWING_SHAPE_BLOCK != null) {
            BetterBuildersWandsMod.instance.mappingManager
                    .setMapping(new ArchitectureCraftCustomMapping(GLOWING_SHAPE_BLOCK, 15));
        }
    }
}
