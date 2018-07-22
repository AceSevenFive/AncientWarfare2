package net.shadowmage.ancientwarfare.structure.block;

import codechicken.lib.model.DummyBakedModel;
import codechicken.lib.model.ModelRegistryHelper;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegistrar;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructures;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public final class BlockGateProxy extends BlockContainer implements IClientRegistrar {

	public BlockGateProxy() {
		super(Material.ROCK);
		setCreativeTab(null);
		setUnlocalizedName("gate_proxy");
		setRegistryName(new ResourceLocation(AncientWarfareStructures.MOD_ID, "gate_proxy"));
		setResistance(2000.f);
		setHardness(5.f);
		AncientWarfareStructures.proxy.addClientRegistrar(this);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TEGateProxy();
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		//nothing gets dropped
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	} //TODO really normal cube after all the other stuff? test why this is the case

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return WorldTools.getTile(world, pos, TEGateProxy.class).map(g -> g.onBlockPicked(target)).orElse(ItemStack.EMPTY);
	}

	@Override
	public int quantityDropped(Random par1Random) {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return WorldTools.getTile(world, pos, TEGateProxy.class).map(g -> g.onBlockClicked(player, hand)).orElse(false);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		Optional<TEGateProxy> proxy = WorldTools.getTile(world, pos, TEGateProxy.class);
		if (proxy.isPresent()) {
			proxy.get().onBlockAttacked(player);
		} else if (player != null && player.capabilities.isCreativeMode) {
			return super.removedByPlayer(state, world, pos, player, false);
		}
		return false;
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addExhaustion(0.025F);
	}

	@Override
	public void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount) {
		//no xp drop
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	//Actually "can go through", for mob pathing
	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		if (WorldTools.getTile(world, pos, TEGateProxy.class).map(TEGateProxy::isGateClosed).orElse(false)) {
			return false;
		}

		//Gate is probably open, Search identical neighbour
		if (world.getBlockState(pos.offset(EnumFacing.WEST)).getBlock() == this) { //TODO only the first half of these should be needed
			return world.getBlockState(pos.offset(EnumFacing.EAST)).getBlock() == this;
		} else if (world.getBlockState(pos.offset(EnumFacing.NORTH)).getBlock() == this) {
			return world.getBlockState(pos.offset(EnumFacing.SOUTH)).getBlock() == this;
		} else if (world.getBlockState(pos.offset(EnumFacing.EAST)).getBlock() == this) {
			return world.getBlockState(pos.offset(EnumFacing.WEST)).getBlock() == this;
		} else if (world.getBlockState(pos.offset(EnumFacing.SOUTH)).getBlock() == this) {
			return world.getBlockState(pos.offset(EnumFacing.NORTH)).getBlock() == this;
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerClient() {
		ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "normal");
		ModelLoaderHelper.registerItem(this, modelLocation);
		ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return modelLocation;
			}
		});
		ModelRegistryHelper.register(modelLocation, new DummyBakedModel());
	}
}
