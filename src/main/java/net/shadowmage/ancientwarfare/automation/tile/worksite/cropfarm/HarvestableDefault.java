package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.util.Collections;
import java.util.List;

public class HarvestableDefault implements IHarvestable {
	@Override
	public List<BlockPos> getPositionsToHarvest(World world, BlockPos pos, IBlockState state) {
		if (state.getBlock() instanceof IGrowable && !((IGrowable) state.getBlock()).canGrow(world, pos, state, world.isRemote)) {
			return Collections.singletonList(pos);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean canBeFertilized(IBlockState state, World world, BlockPos pos) {
		if (!(state.getBlock() instanceof IGrowable)) {
			return false;
		}

		IGrowable growable = (IGrowable) state.getBlock();

		return growable.canGrow(world, pos, state, world.isRemote) && growable.canUseBonemeal(world, world.rand, pos, state);
	}

	@Override
	public boolean harvest(World world, IBlockState state, BlockPos pos, EntityPlayer player, int fortune, IItemHandler inventory) {
		Block block = state.getBlock();
		NonNullList<ItemStack> stacks = NonNullList.create();

		block.getDrops(stacks, world, pos, state, fortune);

		if (!InventoryTools.canInventoryHold(inventory, stacks)) {
			return false;
		}

		if (!BlockTools.breakBlockNoDrops(world, player, pos, state)) {
			return false;
		}

		ItemStack plantable = InventoryTools.removeItem(stacks, i -> i.getItem() instanceof IPlantable, 1);

		if (!plantable.isEmpty()) {
			BlockTools.placeItemBlock(plantable, world, pos, EnumFacing.UP);
		}

		InventoryTools.insertOrDropItems(inventory, stacks, world, pos);
		return true;
	}

	@Override
	public boolean matches(IBlockState state) {
		return true;
	}
}
