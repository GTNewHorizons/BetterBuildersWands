package portablejim.bbw.compat.architecturecraft;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.registry.GameRegistry;
import gcewing.architecture.common.tile.TileShape;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.conversion.CustomMapping;
import portablejim.bbw.shims.IWorldShim;

public class ArchitectureCraftCustomMapping extends CustomMapping {

    private static final Block SHAPE_BLOCK = GameRegistry.findBlock("ArchitectureCraft", "shape");

    public ArchitectureCraftCustomMapping() {
        super(SHAPE_BLOCK, 0, new ItemStack(Item.getItemFromBlock(SHAPE_BLOCK)), SHAPE_BLOCK, 0, true);
    }

    @Override
    public ItemStack getItems(IWorldShim world, Point3d point) {
        TileEntity tileEntity = world.getWorld().getTileEntity(point.x, point.y, point.z);
        if (tileEntity instanceof TileShape) {
            TileShape shapeTile = ((TileShape) tileEntity);
            return shapeTile.newItemStack(1);
        }
        return super.getItems(world, point);
    }
}
