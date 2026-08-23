package portablejim.bbw.core.conversion;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import portablejim.bbw.basics.Point3d;
import portablejim.bbw.shims.IPlayerShim;
import portablejim.bbw.shims.IWorldShim;

/**
 * Created by james on 18/12/15.
 */
public class CustomMapping implements ICustomMapping {

    private final Block lookBlock;
    private final int meta;
    private final ItemStack items;
    private final Block placeBlock;
    private final int placeMeta;
    private final boolean shouldCopyTileNBT;

    public CustomMapping(Block lookBlock, int lookMeta, ItemStack items, Block placeBlock, int placeMeta) {
        this(lookBlock, lookMeta, items, placeBlock, placeMeta, false);
    }

    public CustomMapping(Block lookBlock, int lookMeta, ItemStack items, Block placeBlock, int placeMeta,
            boolean shouldCopyTileNBT) {

        this.lookBlock = lookBlock;
        this.meta = lookMeta;
        this.items = items;
        this.placeBlock = placeBlock;
        this.placeMeta = placeMeta;
        this.shouldCopyTileNBT = shouldCopyTileNBT;
    }

    public Block getLookBlock() {
        return lookBlock;
    }

    public int getMeta() {
        return meta;
    }

    public ItemStack getItems() {
        return items;
    }

    public ItemStack getItems(IWorldShim world, IPlayerShim player, Point3d point) {
        return getItems();
    }

    public Block getPlaceBlock() {
        return placeBlock;
    }

    public int getPlaceMeta() {
        return placeMeta;
    }

    public boolean shouldCopyTileNBT() {
        return shouldCopyTileNBT;
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof CustomMapping && this.equals((CustomMapping) that));
    }

    public boolean equals(CustomMapping that) {
        return this.lookBlock == that.lookBlock && this.meta == that.meta
                && this.items == that.items
                && this.placeBlock == that.placeBlock
                && this.placeMeta == that.placeMeta
                && this.shouldCopyTileNBT == that.shouldCopyTileNBT;
    }

    @Override
    public int hashCode() {
        int result = lookBlock.hashCode();
        result = 31 * result + meta;
        result = 31 * result + items.hashCode();
        result = 31 * result + placeBlock.hashCode();
        result = 31 * result + placeMeta;
        result = 31 * result + Boolean.hashCode(shouldCopyTileNBT);
        return result;
    }
}
