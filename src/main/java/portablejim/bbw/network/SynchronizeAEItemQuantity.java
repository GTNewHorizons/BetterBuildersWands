package portablejim.bbw.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.glodblock.github.util.Util;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.core.MEHandler;
import portablejim.bbw.shims.BasicPlayerShim;

public abstract class SynchronizeAEItemQuantity<T extends IMessage> implements IMessage, IMessageHandler<T, IMessage> {

    public static class SyncClient extends SynchronizeAEItemQuantity<SyncClient> {

        private int size;

        public SyncClient() {

        }

        public SyncClient(int value) {
            this.size = value;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            BasicPlayerShim.AEItemSize = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(size);
        }

        @Override
        public IMessage onMessage(SyncClient message, MessageContext ctx) {
            return null;
        }
    }

    public static class SyncServer extends SynchronizeAEItemQuantity<SyncServer> {

        private ItemStack stack;

        public SyncServer() {

        }

        public SyncServer(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            stack = ByteBufUtils.readItemStack(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeItemStack(buf, stack);
        }

        @Override
        public IMessage onMessage(SyncServer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            WirelessTerminalGuiObject obj = MEHandler.getTerminalGuiObject(player);

            if (obj == null || message.stack == null) {
                BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(0), player);
                return null;
            }

            IMEMonitor<IAEItemStack> inventory = obj.getItemInventory();

            if (inventory != null) {
                boolean isInRange = obj.rangeCheck()
                        || (Loader.isModLoaded("ae2fc") && Util.hasInfinityBoosterCard(obj.getItemStack()));

                if (isInRange) {
                    IAEItemStack aeStack = AEItemStack.create(message.stack);

                    IAEItemStack found = inventory.getStorageList().findPrecise(aeStack);

                    int size = (found != null) ? (int) Math.min(found.getStackSize(), Integer.MAX_VALUE) : 0;
                    BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(size), player);
                    return null;
                }
            }

            BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(0), player);
            return null;
        }
    }
}
