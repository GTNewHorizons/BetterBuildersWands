package portablejim.bbw.shims;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import portablejim.bbw.basics.Point3d;

/**
 * Wrap player functions.
 */
public interface IPlayerShim {

    int countItems(ItemStack itemStack, boolean isNBTSensitive);

    boolean useItem(ItemStack itemStack, boolean isNBTSensitive);

    ItemStack getNextItem(Block block, int meta);

    Point3d getPlayerPosition();

    EntityPlayer getPlayer();

    double getReach();

    boolean isCreative();
}
