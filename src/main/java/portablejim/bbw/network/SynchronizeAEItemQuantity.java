package portablejim.bbw.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.glodblock.github.util.Util;

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
            BasicPlayerShim.AEItemSize = readVar(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            writeVar(buf, size);
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
            if (obj == null) {
                BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(0), player);
                return null;
            }
            IAEItemStack item = obj.getStorageList().findPrecise(AEItemStack.create(message.stack));
            if (item != null && (obj.rangeCheck()
                    || (Loader.isModLoaded("ae2fc") && Util.hasInfinityBoosterCard(obj.getItemStack())))) {
                int size = (int) Math.min(item.getStackSize(), Integer.MAX_VALUE);
                BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(size), player);
            } else {
                BetterBuildersWandsMod.instance.networkWrapper.sendTo(new SyncClient(0), player);
            }
            return null;
        }
    }

    private static final byte INT = -128;
    private static final byte SHORT = -127;

    private static final int USHORT_MAX = 0xFFFF;
    private static final short UBYTE_MAX = 0xFF - 4;

    public static void writeVar(ByteBuf buf, int value) {
        if (value > USHORT_MAX) {
            buf.writeByte(INT);
            buf.writeInt(value);
            return;
        }
        if (value > UBYTE_MAX) {
            buf.writeByte(SHORT);
            buf.writeShort(value);
            return;
        }
        buf.writeByte(value);
    }

    public static int readVar(ByteBuf buf) {
        short type = buf.readByte();
        switch (type) {
            case INT:
                return buf.readInt();
            case SHORT:
                return buf.readShort();
            default:
                return type;
        }
    }
}
