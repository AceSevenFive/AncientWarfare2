package net.shadowmage.ancientwarfare.structure.template.build;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.api.TemplateRule;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;

import java.util.Optional;

public class StructureBuilderTicked extends StructureBuilder {

	public boolean invalid = false;
	private boolean hasClearedArea;
	private BlockPos clearPos;

	public StructureBuilderTicked(World world, StructureTemplate template, EnumFacing face, BlockPos pos) {
		super(world, template, face, pos);
		clearPos = bb.min;
	}

	public StructureBuilderTicked()//nbt-constructor
	{

	}

	public void tick(EntityPlayer player) {
		if (!hasClearedArea) {
			while (!breakClearTargetBlock(player)) {
				if (!incrementClear()) {
					hasClearedArea = true;
					break;
				}
			}
			if (!incrementClear()) {
				hasClearedArea = true;
			}
		} else if (!this.isFinished()) {
			while (!this.isFinished()) {
				Optional<TemplateRule> rule = template.getRuleAt(currentX, currentY, currentZ);
				if (rule.map(r -> !r.shouldPlaceOnBuildPass(world, turns, destination, currentPriority)).orElse(false)) {
					increment();//skip that position, was either air/null rule, or could not be placed on current pass, auto-increment to next
				} else//place it...
				{
					rule.ifPresent(this::placeRule);
					break;
				}
			}
			increment();//finally, increment to next position (will trigger isFinished if actually done, has no problems if already finished)
		}
	}

	private boolean breakClearTargetBlock(EntityPlayer player) {
		return BlockTools.breakBlockAndDrop(world, clearPos);
	}

	private boolean incrementClear() {
		clearPos = clearPos.east();
		if (clearPos.getX() > bb.max.getX()) {
			clearPos = new BlockPos(bb.min.getX(), clearPos.getY(), clearPos.getZ());
			clearPos = clearPos.south();
			if (clearPos.getZ() > bb.max.getZ()) {
				clearPos = new BlockPos(bb.min.getX(), clearPos.up().getY(), bb.min.getZ());
				if (clearPos.getY() > bb.max.getY()) {
					return false;
				}
			}
		}
		return true;
	}

	public void setWorld(World world)//should be called on first-update of the TE (after its world is set)
	{
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public void readFromNBT(NBTTagCompound tag)//should be called immediately after construction
	{
		String name = tag.getString("name");
		StructureTemplate template = StructureTemplateManager.INSTANCE.getTemplate(name);
		if (template != null) {
			this.template = template;
			this.currentX = tag.getInteger("x");
			this.currentY = tag.getInteger("y");
			this.currentZ = tag.getInteger("z");
			this.clearPos = BlockPos.fromLong(tag.getLong("clearPos"));
			this.hasClearedArea = tag.getBoolean("cleared");
			this.turns = tag.getInteger("turns");
			this.buildFace = EnumFacing.VALUES[tag.getByte("buildFace")];
			this.maxPriority = tag.getInteger("maxPriority");
			this.currentPriority = tag.getInteger("currentPriority");

			this.bb = new StructureBB(BlockPos.fromLong(tag.getLong("bbMin")), BlockPos.fromLong(tag.getLong("bbMax")));
			this.buildOrigin = BlockPos.fromLong(tag.getLong("buildOrigin"));
			this.incrementDestination();
		} else {
			invalid = true;
		}
	}

	public void writeToNBT(NBTTagCompound tag) {
		tag.setString("name", template.name);
		tag.setByte("face", (byte) buildFace.ordinal());
		tag.setInteger("turns", turns);
		tag.setInteger("maxPriority", maxPriority);
		tag.setInteger("currentPriority", currentPriority);
		tag.setInteger("x", currentX);
		tag.setInteger("y", currentY);
		tag.setInteger("z", currentZ);
		tag.setLong("clearPos", clearPos.toLong());
		tag.setBoolean("cleared", hasClearedArea);

		tag.setLong("buildOrigin", buildOrigin.toLong());
		tag.setLong("bbMin", bb.min.toLong());
		tag.setLong("bbMax", bb.max.toLong());
	}

	/*
	 * @return
	 */
	public StructureTemplate getTemplate() {
		return template;
	}

}
