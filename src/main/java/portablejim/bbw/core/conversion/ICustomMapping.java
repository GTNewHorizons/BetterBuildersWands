package portablejim.bbw.core.conversion;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import portablejim.bbw.basics.Point3d;
import portablejim.bbw.shims.IPlayerShim;
import portablejim.bbw.shims.IWorldShim;

public interface ICustomMapping {

    /**
     * @return The block being looked at.
     */
    Block getLookBlock();

    /**
     * @return The meta of the block being looked at.
     */
    int getMeta();

    /**
     * Gets the item that should be placed.
     * 
     * @param world  The world where the block will be placed.
     * @param player The player placing the blocks.
     * @param point  The position where the player is looking.
     * @return An item stack representing a single block being placed.
     */
    ItemStack getItems(IWorldShim world, IPlayerShim player, Point3d point);

    /**
     * @return True to copy the nbt data of the tile entity of the original block.
     */
    boolean shouldCopyTileNBT();
}
