package portablejim.bbw.shims;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Wrap functions for a creative player.
 */
public class CreativePlayerShim extends BasicPlayerShim implements IPlayerShim {

    public CreativePlayerShim(EntityPlayer player) {
        super(player);
    }

    @Override
    public int countItems(ItemStack itemStack, boolean isNBTSensitive) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int useItem(ItemStack itemStack, boolean isNBTSensitive) {
        return itemStack.stackSize;
    }

    @Override
    public ItemStack getNextItem(Block block, int meta) {
        return new ItemStack(block, 1, meta);
    }
}
