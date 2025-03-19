package portablejim.bbw.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;

import cpw.mods.fml.common.FMLLog;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.EnumFluidLock;
import portablejim.bbw.basics.EnumLock;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.conversion.CustomMapping;
import portablejim.bbw.core.wands.IWand;
import portablejim.bbw.shims.IPlayerShim;
import portablejim.bbw.shims.IWorldShim;

/**
 * Does the heavy work of working out the blocks to place and places them.
 */
public class WandWorker {

    private final IWand wand;
    private final IPlayerShim player;
    private final IWorldShim world;

    HashSet<Point3d> allCandidates = new HashSet<Point3d>();

    public WandWorker(IWand wand, IPlayerShim player, IWorldShim world) {

        this.wand = wand;
        this.player = player;
        this.world = world;
    }

    public ItemStack getProperItemStack(IWorldShim world, IPlayerShim player, Point3d blockPos) {
        Block block = world.getBlock(blockPos);
        int meta = world.getMetadata(blockPos);
        CustomMapping customMapping = BetterBuildersWandsMod.instance.mappingManager.getMapping(block, meta);
        if (customMapping != null) {
            return customMapping.getItems(world, blockPos);
        }
        String blockString = String.format("%s/%s", Block.blockRegistry.getNameForObject(block), meta);
        if (!BetterBuildersWandsMod.instance.configValues.HARD_BLACKLIST_SET.contains(blockString)) {
            ItemStack exactItemstack = new ItemStack(block, 1, meta);
            if (Item.getItemFromBlock(block) != null && player.countItems(exactItemstack, false) > 0) {
                return exactItemstack;
            }
            return getEquivalentItemStack(blockPos);
        }
        return null;
    }

    public ItemStack getEquivalentItemStack(Point3d blockPos) {
        Block block = world.getBlock(blockPos);
        int meta = world.getMetadata(blockPos);
        // ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ItemStack stack = null;
        String blockString = String.format("%s/%s", Block.blockRegistry.getNameForObject(block), meta);
        if (block.canSilkHarvest(world.getWorld(), player.getPlayer(), blockPos.x, blockPos.y, blockPos.z, meta)) {
            stack = BetterBuildersWandsMod.instance.blockCache.getStackedBlock(world, blockPos);
        } else if (!BetterBuildersWandsMod.instance.configValues.SOFT_BLACKLIST_SET.contains(blockString)) {
            Item dropped = block.getItemDropped(meta, new Random(), 0);
            if (dropped != null) {
                stack = new ItemStack(dropped, block.quantityDropped(meta, 0, new Random()), block.damageDropped(meta));
            }
        }
        // ForgeEventFactory.fireBlockHarvesting(items,this.world.getWorld(), block, blockPos.x, blockPos.y, blockPos.z,
        // world.getMetadata(blockPos), 0, 1.0F, true, this.player.getPlayer());
        return stack;
    }

    private boolean shouldContinue(Point3d currentCandidate, Block targetBlock, int targetMetadata,
            Block candidateSupportingBlock, int candidateSupportingMeta, AxisAlignedBB blockBB, EnumFluidLock fluidLock,
            boolean isNBTSensitive, TileEntity targetTile, TileEntity candidateSupportingTile) {
        if (!world.blockIsAir(currentCandidate)) {
            Block currrentCandidateBlock = world.getBlock(currentCandidate);
            if (!(fluidLock == EnumFluidLock.IGNORE && currrentCandidateBlock != null
                    && (currrentCandidateBlock instanceof IFluidBlock
                            || currrentCandidateBlock instanceof BlockLiquid)))
                return false;
        } ;
        if (currentCandidate.y >= 255) return false;
        /*
         * if((FluidRegistry.getFluid("water").getBlock().equals(world.getBlock(currentCandidate)) ||
         * FluidRegistry.getFluid("lava").getBlock().equals(world.getBlock(currentCandidate))) &&
         * world.getMetadata(currentCandidate) == 0){ return false; }
         */
        if (!targetBlock.equals(candidateSupportingBlock)) return false;
        if (targetMetadata != candidateSupportingMeta) return false;
        // if(targetBlock instanceof BlockCrops) return false;
        if (!targetBlock.canPlaceBlockAt(world.getWorld(), currentCandidate.x, currentCandidate.y, currentCandidate.z))
            return false;
        if (!targetBlock.canBlockStay(world.getWorld(), currentCandidate.x, currentCandidate.y, currentCandidate.z))
            return false;
        if (!targetBlock.canReplace(
                world.getWorld(),
                currentCandidate.x,
                currentCandidate.y,
                currentCandidate.z,
                targetMetadata,
                new ItemStack(candidateSupportingBlock, 1, candidateSupportingMeta)))
            return false;
        if (isNBTSensitive) {
            if (targetTile == null || candidateSupportingTile == null) {
                return false;
            }
            NBTTagCompound targetNBT = new NBTTagCompound();
            NBTTagCompound candidateSupportingNBT = new NBTTagCompound();
            targetTile.writeToNBT(targetNBT);
            candidateSupportingTile.writeToNBT(candidateSupportingNBT);
            targetNBT.removeTag("x");
            targetNBT.removeTag("y");
            targetNBT.removeTag("z");
            candidateSupportingNBT.removeTag("x");
            candidateSupportingNBT.removeTag("y");
            candidateSupportingNBT.removeTag("z");
            if (!targetNBT.equals(candidateSupportingNBT)) {
                return false;
            }
        }
        return !world.entitiesInBox(blockBB);
    }

    public LinkedList<Point3d> getBlockPositionList(Point3d blockLookedAt, ForgeDirection placeDirection, int maxBlocks,
            EnumLock directionLock, EnumLock faceLock, EnumFluidLock fluidLock, boolean isNBTSensitive) {
        LinkedList<Point3d> candidates = new LinkedList<Point3d>();
        LinkedList<Point3d> toPlace = new LinkedList<Point3d>();

        Block targetBlock = world.getBlock(blockLookedAt);
        int targetMetadata = world.getMetadata(blockLookedAt);
        TileEntity targetTile = world.getTile(blockLookedAt);
        Point3d startingPoint = blockLookedAt.move(placeDirection);

        int directionMaskInt = directionLock.mask;
        int faceMaskInt = faceLock.mask;

        if (((directionLock != EnumLock.HORIZONTAL && directionLock != EnumLock.VERTICAL)
                || (placeDirection != ForgeDirection.UP && placeDirection != ForgeDirection.DOWN))
                && (directionLock != EnumLock.NORTHSOUTH
                        || (placeDirection != ForgeDirection.NORTH && placeDirection != ForgeDirection.SOUTH))
                && (directionLock != EnumLock.EASTWEST
                        || (placeDirection != ForgeDirection.EAST && placeDirection != ForgeDirection.WEST))) {
            candidates.add(startingPoint);
        }
        AxisAlignedBB blockBB = targetBlock
                .getCollisionBoundingBoxFromPool(world.getWorld(), blockLookedAt.x, blockLookedAt.y, blockLookedAt.z);
        while (candidates.size() > 0 && toPlace.size() < maxBlocks) {
            Point3d currentCandidate = candidates.removeFirst();

            Point3d supportingPoint = currentCandidate.move(placeDirection.getOpposite());
            Block candidateSupportingBlock = world.getBlock(supportingPoint);
            int candidateSupportingMeta = world.getMetadata(supportingPoint);
            TileEntity candidateSupportingTile = world.getTile(supportingPoint);
            AxisAlignedBB candidateBB = blockBB;
            if (candidateBB != null) {
                candidateBB = candidateBB.copy().offset(
                        currentCandidate.x - blockLookedAt.x,
                        currentCandidate.y - blockLookedAt.y,
                        currentCandidate.z - blockLookedAt.z);
            }
            if (shouldContinue(
                    currentCandidate,
                    targetBlock,
                    targetMetadata,
                    candidateSupportingBlock,
                    candidateSupportingMeta,
                    candidateBB,
                    fluidLock,
                    isNBTSensitive,
                    targetTile,
                    candidateSupportingTile) && allCandidates.add(currentCandidate)) {
                toPlace.add(currentCandidate);

                switch (placeDirection) {
                    case DOWN:
                    case UP:
                        if ((faceMaskInt & EnumLock.UP_DOWN_MASK) > 0) {
                            if ((directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.NORTH));
                            if ((directionMaskInt & EnumLock.EAST_WEST_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.EAST));
                            if ((directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.SOUTH));
                            if ((directionMaskInt & EnumLock.EAST_WEST_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.WEST));
                            if ((directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0
                                    && (directionMaskInt & EnumLock.EAST_WEST_MASK) > 0) {
                                candidates.add(currentCandidate.move(ForgeDirection.NORTH).move(ForgeDirection.EAST));
                                candidates.add(currentCandidate.move(ForgeDirection.NORTH).move(ForgeDirection.WEST));
                                candidates.add(currentCandidate.move(ForgeDirection.SOUTH).move(ForgeDirection.EAST));
                                candidates.add(currentCandidate.move(ForgeDirection.SOUTH).move(ForgeDirection.WEST));
                            }
                        }
                        break;
                    case NORTH:
                    case SOUTH:
                        if ((faceMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0) {
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.UP));
                            if ((directionMaskInt & EnumLock.EAST_WEST_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.EAST));
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN));
                            if ((directionMaskInt & EnumLock.EAST_WEST_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.WEST));
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0
                                    && (directionMaskInt & EnumLock.EAST_WEST_MASK) > 0) {
                                candidates.add(currentCandidate.move(ForgeDirection.UP).move(ForgeDirection.EAST));
                                candidates.add(currentCandidate.move(ForgeDirection.UP).move(ForgeDirection.WEST));
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN).move(ForgeDirection.EAST));
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN).move(ForgeDirection.WEST));
                            }
                        }
                        break;
                    case WEST:
                    case EAST:
                        if ((faceMaskInt & EnumLock.EAST_WEST_MASK) > 0) {
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.UP));
                            if ((directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.NORTH));
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN));
                            if ((directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0)
                                candidates.add(currentCandidate.move(ForgeDirection.SOUTH));
                            if ((directionMaskInt & EnumLock.UP_DOWN_MASK) > 0
                                    && (directionMaskInt & EnumLock.NORTH_SOUTH_MASK) > 0) {
                                candidates.add(currentCandidate.move(ForgeDirection.UP).move(ForgeDirection.NORTH));
                                candidates.add(currentCandidate.move(ForgeDirection.UP).move(ForgeDirection.SOUTH));
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN).move(ForgeDirection.NORTH));
                                candidates.add(currentCandidate.move(ForgeDirection.DOWN).move(ForgeDirection.SOUTH));
                            }
                        }
                }
            }
        }
        return toPlace;
    }

    public ArrayList<Point3d> placeBlocks(ItemStack wandItem, LinkedList<Point3d> blockPosList, Point3d originalBlock,
            ItemStack sourceItems, int side, float hitX, float hitY, float hitZ) {
        ArrayList<Point3d> placedBlocks = new ArrayList<Point3d>();
        for (Point3d blockPos : blockPosList) {
            boolean blockPlaceSuccess;
            CustomMapping mapping = BetterBuildersWandsMod.instance.mappingManager
                    .getMapping(world.getBlock(originalBlock), world.getMetadata(originalBlock));
            if (mapping != null) {
                blockPlaceSuccess = world.setBlock(
                        originalBlock,
                        blockPos,
                        mapping.getPlaceBlock(),
                        mapping.getPlaceMeta(),
                        mapping.shouldCopyTileNBT());
            } else {
                blockPlaceSuccess = world.copyBlock(originalBlock, blockPos);
            }

            if (blockPlaceSuccess) {
                Block block = world.getBlock(originalBlock);
                world.playPlaceAtBlock(blockPos, block);
                placedBlocks.add(blockPos);
                if (!player.isCreative()) {
                    wand.placeBlock(wandItem, player.getPlayer());
                }
                boolean takeFromInventory = player.useItem(sourceItems, mapping != null && mapping.shouldCopyTileNBT());
                if (!takeFromInventory) {
                    FMLLog.info("BBW takeback: %s", blockPos.toString());
                    world.setBlockToAir(blockPos);
                    placedBlocks.remove(placedBlocks.size() - 1);
                } else {
                    block.onBlockPlacedBy(
                            world.getWorld(),
                            blockPos.x,
                            blockPos.y,
                            blockPos.z,
                            player.getPlayer(),
                            sourceItems);
                }
            }
        }

        return placedBlocks;
    }
}
