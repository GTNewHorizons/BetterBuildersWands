package portablejim.bbw.core.wands;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Interface for IWand objects that abstract the wand working.
 */
public interface IWand {
    int getMaxBlocks(ItemStack itemStack);

    boolean placeBlock(ItemStack itemStack, EntityLivingBase entityLivingBase);
}
