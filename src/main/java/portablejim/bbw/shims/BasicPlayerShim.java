package portablejim.bbw.shims;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import portablejim.bbw.basics.Point3d;
import vazkii.botania.api.item.IBlockProvider;

/**
 * Wrap a player to provide basic functions.
 */
public class BasicPlayerShim implements IPlayerShim {

    private final EntityPlayer player;
    private final boolean providersEnabled;

    public BasicPlayerShim(EntityPlayer player) {
        this.player = player;
        this.providersEnabled = areProvidersEnabled();
    }

    private static Block getBlock(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem());
    }

    private static int getBlockMeta(ItemStack stack) {
        return stack.getHasSubtypes() ? stack.getItemDamage() : 0;
    }

    private static boolean areProvidersEnabled() {
        try {
            boolean disable = new Object() instanceof IBlockProvider;
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public double getReach() {
        if (player instanceof EntityPlayerMP) {
            return ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
        }
        return 5F;
    }

    @Override
    public int countItems(ItemStack itemStack, boolean isNBTSensitive) {
        if (itemStack == null || player.inventory == null || player.inventory.mainInventory == null) {
            return 0;
        }

        int total = 0;
        Block block = getBlock(itemStack);
        int meta = getBlockMeta(itemStack);

        for (ItemStack inventoryStack : player.inventory.mainInventory) {
            if (inventoryStack != null && itemStack.isItemEqual(inventoryStack)
                    && (!isNBTSensitive || ItemStack.areItemStackTagsEqual(itemStack, inventoryStack))) {
                total += Math.max(0, inventoryStack.stackSize);
            } else
                if (providersEnabled && inventoryStack != null && inventoryStack.getItem() instanceof IBlockProvider) {
                    IBlockProvider prov = (IBlockProvider) inventoryStack.getItem();
                    int provCount = prov.getBlockCount(player, itemStack, inventoryStack, block, meta);
                    if (provCount == -1) return Integer.MAX_VALUE;
                    total += provCount;
                }
        }

        return itemStack.stackSize > 0 ? total / itemStack.stackSize : 0;
    }

    /**
     * @param itemStack Required Quantity
     * @return Actua Quantity
     */
    @Override
    public int useItem(ItemStack itemStack, boolean isNBTSensitive) {
        if (itemStack == null || player.inventory == null || player.inventory.mainInventory == null) {
            return 0;
        }

        // Reverse direction to leave hotbar to last.
        int toUse = 0;
        final int needUse = itemStack.stackSize;
        List<ItemStack> providers = new ArrayList<>();

        for (int i = player.inventory.mainInventory.length - 1; i >= 0; i--) {
            ItemStack inventoryStack = player.inventory.mainInventory[i];
            final int need = needUse - toUse;
            if (inventoryStack != null && itemStack.isItemEqual(inventoryStack)
                    && (!isNBTSensitive || ItemStack.areItemStackTagsEqual(itemStack, inventoryStack))) {
                if (inventoryStack.stackSize < need) {
                    toUse += inventoryStack.stackSize;
                    inventoryStack.stackSize = 0;
                } else {
                    inventoryStack.stackSize -= need;
                    toUse = needUse;
                }
                if (inventoryStack.stackSize == 0) {
                    player.inventory.setInventorySlotContents(i, null);
                }
                player.inventoryContainer.detectAndSendChanges();
                if (toUse >= needUse) {
                    return needUse;
                }
            } else
                if (providersEnabled && inventoryStack != null && inventoryStack.getItem() instanceof IBlockProvider) {
                    providers.add(inventoryStack);
                }
        }

        // IBlockProvider does not support removing more than one item in an atomic operation.
        if (!providers.isEmpty()) {
            Block block = getBlock(itemStack);
            int meta = getBlockMeta(itemStack);
            IBlockProvider prov = (IBlockProvider) providers.get(0).getItem();
            for (ItemStack provStack : providers) {
                assert prov != null;
                final int available = prov.getBlockCount(player, itemStack, provStack, block, meta);
                if (available != 0) {
                    while (prov.provideBlock(player, itemStack, provStack, block, meta, true) && needUse > toUse) {
                        ++toUse;
                    }
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
        }

        return toUse;
    }

    @Override
    public ItemStack getNextItem(Block block, int meta) {
        for (int i = player.inventory.mainInventory.length - 1; i >= 0; i--) {
            ItemStack inventoryStack = player.inventory.mainInventory[i];
        }

        return null;
    }

    @Override
    public Point3d getPlayerPosition() {
        return new Point3d((int) player.posX, (int) player.posY, (int) player.posZ);
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCreative() {
        return player.capabilities.isCreativeMode;
    }
}
