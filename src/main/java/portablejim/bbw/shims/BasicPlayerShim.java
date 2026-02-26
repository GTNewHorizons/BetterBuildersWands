package portablejim.bbw.shims;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.compat.ztones.Ztones;
import portablejim.bbw.core.MEHandler;
import portablejim.bbw.network.SynchronizeAEItemQuantity;
import vazkii.botania.api.item.IBlockProvider;

/**
 * Wrap a player to provide basic functions.
 */
public class BasicPlayerShim implements IPlayerShim {

    private final EntityPlayer player;
    private final boolean providersEnabled;
    private final boolean aeEnabled;
    // Inventory > Hotbar > Backhand
    private final int[] slotPriority;

    public static int AEItemSize = 0;

    public BasicPlayerShim(EntityPlayer player) {
        this.player = player;
        this.providersEnabled = areProvidersEnabled();
        this.aeEnabled = Loader.isModLoaded("appliedenergistics2");
        this.slotPriority = new int[this.player.inventory.mainInventory.length];

        int idx = 0;
        for (int i = this.player.inventory.mainInventory.length - 2; i >= 0; i--) {
            this.slotPriority[idx++] = i;
        }
        this.slotPriority[idx] = this.player.inventory.mainInventory.length - 1;
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

        if (aeEnabled) {
            total += getAEItem(itemStack, Integer.MAX_VALUE - total, false);
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

        if (Ztones.isLoaded() && player.inventory.hasItem(Ztones.getOfanix())
                && itemStack.getItem() == Item.getItemFromBlock(Blocks.cobblestone)) {
            return needUse;
        }

        if (aeEnabled) {
            toUse += getAEItem(itemStack, needUse, true);
            if (toUse >= needUse) {
                return needUse;
            }
        }

        List<ItemStack> providers = new ArrayList<>();
        for (int i : this.slotPriority) {
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
            } else {
                if (providersEnabled && inventoryStack != null && inventoryStack.getItem() instanceof IBlockProvider) {
                    providers.add(inventoryStack);
                }
            }
        }

        Block block = getBlock(itemStack);
        int meta = getBlockMeta(itemStack);
        // IBlockProvider does not support removing more than one item in an atomic operation.
        if (!providers.isEmpty()) {
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

    private static class LastItem {

        private static Item item;
        private static int meta;
        private static NBTTagCompound nbt;

        public static boolean equls(ItemStack stack) {
            if (item != stack.getItem()) return false;
            if (meta != stack.getItemDamage()) return false;
            return Objects.equals(nbt, stack.getTagCompound());
        }

        public static void set(ItemStack stack) {
            item = stack.getItem();
            meta = stack.getItemDamage();
            nbt = stack.getTagCompound();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    public int getAEItem(ItemStack item, int size, boolean MODULATE) {
        if (!player.worldObj.isRemote) {
            WirelessTerminalGuiObject obj = MEHandler.getTerminalGuiObject(player);
            if (obj != null && obj.rangeCheck()) {
                if (MEHandler.securityCheck(player, obj.getGrid(), SecurityPermissions.EXTRACT)) {
                    IMEMonitor<IAEItemStack> inventory = obj.getItemInventory();
                    if (inventory == null) return 0;
                    IAEItemStack stack = inventory.extractItems(
                            AEItemStack.create(item).setStackSize(size),
                            MODULATE ? Actionable.MODULATE : Actionable.SIMULATE,
                            new PlayerSource(player, obj));
                    if (stack == null) return 0;
                    return (int) stack.getStackSize();
                }
            }
        } else {
            if (MEHandler.hasTerminal(player)) {
                if (!LastItem.equls(item)) {
                    BetterBuildersWandsMod.instance.networkWrapper
                            .sendToServer(new SynchronizeAEItemQuantity.SyncServer(item));
                    LastItem.set(item);
                    return Math.min(AEItemSize, size);
                }
                if (player.worldObj.getTotalWorldTime() % 10 == 0) {
                    BetterBuildersWandsMod.instance.networkWrapper
                            .sendToServer(new SynchronizeAEItemQuantity.SyncServer(item));
                    return Math.min(AEItemSize, size);
                }
            } else {
                AEItemSize = 0;
            }
        }

        return Math.min(AEItemSize, size);
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
