package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.block.AWStructuresBlocks;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpawnerSettings {

	private List<EntitySpawnGroup> spawnGroups = new ArrayList<>();

	private ItemStackHandler inventory = new ItemStackHandler(9);

	private boolean debugMode;
	private boolean transparent;
	private boolean respondToRedstone;//should this spawner respond to redstone impulses
	private boolean redstoneMode;//false==toggle, true==pulse/tick to spawn
	private boolean prevRedstoneState;//used to cache the powered status from last tick, to compare to this tick

	private int playerRange;
	private int mobRange;
	private int range = 4;

	private int maxDelay = 20 * 20;
	private int minDelay = 20 * 10;

	private int spawnDelay = maxDelay;

	private int maxNearbyMonsters;

	private boolean lightSensitive;

	private int xpToDrop;

	float blockHardness = 2.f;

	/*
	 * fields for a 'fake' tile-entity...set from the real tile-entity when it has its
	 * world set (which is before first updateEntity() is called)
	 */
	public World world;
	private BlockPos pos;

	public static SpawnerSettings getDefaultSettings() {
		SpawnerSettings settings = new SpawnerSettings();
		settings.playerRange = 16;
		settings.mobRange = 4;
		settings.maxNearbyMonsters = 8;

		EntitySpawnGroup group = new EntitySpawnGroup();
		group.addSpawnSetting(new EntitySpawnSettings());
		settings.addSpawnGroup(group);

		return settings;
	}

	public void setWorld(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public void onUpdate() {
		if (!respondToRedstone) {
			updateNormalMode();
		} else if (redstoneMode) {
			updateRedstoneModePulse();
		} else {
			updateRedstoneModeToggle();
		}
		if (spawnGroups.isEmpty()) {
			world.setBlockToAir(pos);
		}
	}

	private void updateRedstoneModeToggle() {
		prevRedstoneState = world.isBlockIndirectlyGettingPowered(pos) > 0 || world.getStrongPower(pos) > 0;
		if (respondToRedstone && !redstoneMode && !prevRedstoneState) {
			//noop
			return;
		}
		updateNormalMode();
	}

	private void updateRedstoneModePulse() {
		boolean powered = world.isBlockIndirectlyGettingPowered(pos) > 0 || world.getStrongPower(pos) > 0;
		if (!prevRedstoneState && powered) {
			spawnEntities();
		}
		prevRedstoneState = powered;
	}

	private void updateNormalMode() {
		if (spawnDelay > 0) {
			spawnDelay--;
		}
		if (spawnDelay <= 0) {
			int delayRange = maxDelay - minDelay;
			spawnDelay = minDelay + (delayRange <= 0 ? 0 : world.rand.nextInt(delayRange));
			spawnEntities();
		}
	}

	private void spawnEntities() {
		if (lightSensitive) {
			int light = world.getBlockState(pos).getLightValue(world, pos);

			//TODO check this light calculation stuff...
			if (light >= 8) {
				return;
			}
		}
		if (playerRange > 0) {
			List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(playerRange, playerRange, playerRange));
			if (nearbyPlayers.isEmpty()) {
				return;
			}
			boolean doSpawn = false;
			for (EntityPlayer player : nearbyPlayers) {
				if (debugMode || !player.capabilities.isCreativeMode) {
					doSpawn = true;
					break;
				}
			}
			if (!doSpawn) {
				return;
			}
		}

		if (maxNearbyMonsters > 0 && mobRange > 0) {
			int nearbyCount = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(mobRange, mobRange, mobRange)).size();
			if (nearbyCount >= maxNearbyMonsters) {
				AWLog.logDebug("skipping spawning because of too many nearby entities");
				return;
			}
		}

		int totalWeight = 0;
		for (EntitySpawnGroup group : this.spawnGroups)//count total weights
		{
			totalWeight += group.groupWeight;
		}
		int rand = totalWeight == 0 ? 0 : world.rand.nextInt(totalWeight);//select an object
		int check = 0;
		EntitySpawnGroup toSpawn = null;
		int index = 0;
		for (EntitySpawnGroup group : this.spawnGroups)//iterate to find selected object
		{
			check += group.groupWeight;
			if (rand < check)//object found, break
			{
				toSpawn = group;
				break;
			}
			index++;
		}

		if (toSpawn != null) {
			toSpawn.spawnEntities(world, pos, index, range);
			if (toSpawn.shouldRemove()) {
				spawnGroups.remove(toSpawn);
			}
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("respondToRedstone", respondToRedstone);
		if (respondToRedstone) {
			tag.setBoolean("redstoneMode", redstoneMode);
			tag.setBoolean("prevRedstoneState", prevRedstoneState);
		}
		tag.setInteger("minDelay", minDelay);
		tag.setInteger("maxDelay", maxDelay);
		tag.setInteger("spawnDelay", spawnDelay);
		tag.setInteger("playerRange", playerRange);
		tag.setInteger("mobRange", mobRange);
		tag.setInteger("spawnRange", range);
		tag.setInteger("maxNearbyMonsters", maxNearbyMonsters);
		tag.setInteger("xpToDrop", xpToDrop);
		tag.setBoolean("lightSensitive", lightSensitive);
		tag.setBoolean("transparent", transparent);
		tag.setBoolean("debugMode", debugMode);
		NBTTagList groupList = new NBTTagList();
		NBTTagCompound groupTag;
		for (EntitySpawnGroup group : this.spawnGroups) {
			groupTag = new NBTTagCompound();
			group.writeToNBT(groupTag);
			groupList.appendTag(groupTag);
		}
		tag.setTag("spawnGroups", groupList);

		tag.setTag("inventory", inventory.serializeNBT());

		return tag;
	}

	public void readFromNBT(NBTTagCompound tag) {
		spawnGroups.clear();
		respondToRedstone = tag.getBoolean("respondToRedstone");
		if (respondToRedstone) {
			redstoneMode = tag.getBoolean("redstoneMode");
			prevRedstoneState = tag.getBoolean("prevRedstoneState");
		}
		minDelay = tag.getInteger("minDelay");
		maxDelay = tag.getInteger("maxDelay");
		spawnDelay = tag.getInteger("spawnDelay");
		playerRange = tag.getInteger("playerRange");
		mobRange = tag.getInteger("mobRange");
		range = tag.getInteger("spawnRange");
		maxNearbyMonsters = tag.getInteger("maxNearbyMonsters");
		xpToDrop = tag.getInteger("xpToDrop");
		lightSensitive = tag.getBoolean("lightSensitive");
		transparent = tag.getBoolean("transparent");
		debugMode = tag.getBoolean("debugMode");
		NBTTagList groupList = tag.getTagList("spawnGroups", Constants.NBT.TAG_COMPOUND);
		EntitySpawnGroup group;
		for (int i = 0; i < groupList.tagCount(); i++) {
			group = new EntitySpawnGroup();
			group.readFromNBT(groupList.getCompoundTagAt(i));
			spawnGroups.add(group);
		}
		if (tag.hasKey("inventory")) {
			inventory.deserializeNBT(tag.getCompoundTag("inventory"));
		}
	}

	public void addSpawnGroup(EntitySpawnGroup group) {
		spawnGroups.add(group);
	}

	public List<EntitySpawnGroup> getSpawnGroups() {
		return spawnGroups;
	}

	public final boolean isLightSensitive() {
		return lightSensitive;
	}

	public final void toggleLightSensitive() {
		this.lightSensitive = !lightSensitive;
	}

	public final boolean isRespondToRedstone() {
		return respondToRedstone;
	}

	public final void toggleRespondToRedstone() {
		this.respondToRedstone = !respondToRedstone;
	}

	public final boolean getRedstoneMode() {
		return redstoneMode;
	}

	public final void toggleRedstoneMode() {
		this.redstoneMode = !redstoneMode;
	}

	public final int getPlayerRange() {
		return playerRange;
	}

	public final void setPlayerRange(int playerRange) {
		this.playerRange = playerRange;
	}

	public final int getMobRange() {
		return mobRange;
	}

	public final void setMobRange(int mobRange) {
		this.mobRange = mobRange;
	}

	public final int getSpawnRange() {
		return this.range;
	}

	public final void setSpawnRange(int range) {
		this.range = range;
	}

	public final int getMaxDelay() {
		return maxDelay;
	}

	public final void setMaxDelay(int maxDelay) {
		if (minDelay > maxDelay)
			minDelay = maxDelay;
		this.maxDelay = maxDelay;
	}

	public final int getMinDelay() {
		return minDelay;
	}

	public final void setMinDelay(int minDelay) {
		if (minDelay > maxDelay)
			maxDelay = minDelay;
		this.minDelay = minDelay;
	}

	public final int getSpawnDelay() {
		return spawnDelay;
	}

	public final void setSpawnDelay(int spawnDelay) {
		if (spawnDelay > maxDelay)
			maxDelay = spawnDelay;
		if (spawnDelay < minDelay)
			minDelay = spawnDelay;
		this.spawnDelay = spawnDelay;
	}

	public final int getMaxNearbyMonsters() {
		return maxNearbyMonsters;
	}

	public final void setMaxNearbyMonsters(int maxNearbyMonsters) {
		this.maxNearbyMonsters = maxNearbyMonsters;
	}

	public final void setXpToDrop(int xp) {
		this.xpToDrop = xp;
	}

	public final void setBlockHardness(float hardness) {
		this.blockHardness = hardness;
	}

	public final int getXpToDrop() {
		return xpToDrop;
	}

	public final float getBlockHardness() {
		return blockHardness;
	}

	public final IItemHandler getInventory() {
		return inventory;
	}

	public final boolean isDebugMode() {
		return debugMode;
	}

	public final void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public final boolean isTransparent() {
		return transparent;
	}

	public final void toggleTransparent() {
		this.transparent = !transparent;
	}

	public static final class EntitySpawnGroup {
		private int groupWeight = 1;
		private List<EntitySpawnSettings> entitiesToSpawn = new ArrayList<>();

		public void setWeight(int weight) {
			this.groupWeight = weight <= 0 ? 1 : weight;
		}

		public void addSpawnSetting(EntitySpawnSettings setting) {
			entitiesToSpawn.add(setting);
		}

		private void spawnEntities(World world, BlockPos spawnPos, int grpIndex, int range) {
			EntitySpawnSettings settings;
			Iterator<EntitySpawnSettings> it = entitiesToSpawn.iterator();
			int index = 0;
			while (it.hasNext() && (settings = it.next()) != null) {
				settings.spawnEntities(world, spawnPos, range);
				if (settings.shouldRemove()) {
					it.remove();
				}

				int a1 = 0;
				int b2 = settings.remainingSpawnCount;
				int a = (a1 << 16) | (grpIndex & 0x0000ffff);
				int b = (index << 16) | (b2 & 0x0000ffff);
				world.addBlockEvent(spawnPos, AWStructuresBlocks.advancedSpawner, a, b);
				index++;
			}
		}

		private boolean shouldRemove() {
			return entitiesToSpawn.isEmpty();
		}

		public List<EntitySpawnSettings> getEntitiesToSpawn() {
			return entitiesToSpawn;
		}

		public int getWeight() {
			return groupWeight;
		}

		public void writeToNBT(NBTTagCompound tag) {
			tag.setInteger("groupWeight", groupWeight);
			NBTTagList settingsList = new NBTTagList();

			NBTTagCompound settingTag;
			for (EntitySpawnSettings setting : this.entitiesToSpawn) {
				settingTag = new NBTTagCompound();
				setting.writeToNBT(settingTag);
				settingsList.appendTag(settingTag);
			}
			tag.setTag("settingsList", settingsList);
		}

		public void readFromNBT(NBTTagCompound tag) {
			groupWeight = tag.getInteger("groupWeight");
			NBTTagList settingsList = tag.getTagList("settingsList", Constants.NBT.TAG_COMPOUND);
			EntitySpawnSettings setting;
			for (int i = 0; i < settingsList.tagCount(); i++) {
				setting = new EntitySpawnSettings();
				setting.readFromNBT(settingsList.getCompoundTagAt(i));
				this.entitiesToSpawn.add(setting);
			}
		}
	}

	public static final class EntitySpawnSettings {
		private ResourceLocation entityId = new ResourceLocation("pig");
		private NBTTagCompound customTag;
		private int minToSpawn = 2;
		private int maxToSpawn = 4;
		int remainingSpawnCount = -1;
		private boolean forced;

		public EntitySpawnSettings() {

		}

		public EntitySpawnSettings(ResourceLocation entityId) {
			setEntityToSpawn(entityId);
		}

		public final void writeToNBT(NBTTagCompound tag) {
			tag.setString("entityId", entityId.toString());
			if (customTag != null) {
				tag.setTag("customTag", customTag);
			}
			tag.setBoolean("forced", forced);
			tag.setInteger("minToSpawn", minToSpawn);
			tag.setInteger("maxToSpawn", maxToSpawn);
			tag.setInteger("remainingSpawnCount", remainingSpawnCount);
		}

		public final void readFromNBT(NBTTagCompound tag) {
			setEntityToSpawn(new ResourceLocation(tag.getString("entityId")));
			if (tag.hasKey("customTag")) {
				customTag = tag.getCompoundTag("customTag");
			}
			forced = tag.getBoolean("forced");
			minToSpawn = tag.getInteger("minToSpawn");
			maxToSpawn = tag.getInteger("maxToSpawn");
			remainingSpawnCount = tag.getInteger("remainingSpawnCount");
		}

		public final void setEntityToSpawn(ResourceLocation entityId) {
			this.entityId = entityId;
			if (!ForgeRegistries.ENTITIES.containsKey(this.entityId)) {
				AWLog.logError(entityId + " is not a valid entityId.  Spawner default to Zombie.");
				this.entityId = new ResourceLocation("zombie");
			}
			if (AWStructureStatics.excludedSpawnerEntities.contains(this.entityId.toString())) {
				AWLog.logError(entityId + " has been set as an invalid entity for spawners!  Spawner default to Zombie.");
				this.entityId = new ResourceLocation("zombie");
			}
		}

		public final void setCustomSpawnTag(@Nullable NBTTagCompound tag) {
			this.customTag = tag;
		}

		public final void setSpawnCountMin(int min) {
			this.minToSpawn = min;
		}

		public final void setSpawnCountMax(int max) {
			if (minToSpawn < max)
				this.maxToSpawn = max;
			else
				this.maxToSpawn = this.minToSpawn;
		}

		public final void setSpawnLimitTotal(int total) {
			this.remainingSpawnCount = total;
		}

		public final void toggleForce() {
			forced = !forced;
		}

		private boolean shouldRemove() {
			return remainingSpawnCount == 0;
		}

		public final ResourceLocation getEntityId() {
			return entityId;
		}

		public final String getEntityName() {
			if (customTag != null && customTag.hasKey("factionName")) {
				return EntityTools.getUnlocName(entityId).replace("faction", getCustomTag().getString("factionName"));
			}
			return EntityTools.getUnlocName(entityId);
		}

		public final int getSpawnMin() {
			return minToSpawn;
		}

		public final int getSpawnMax() {
			return maxToSpawn;
		}

		public final int getSpawnTotal() {
			return remainingSpawnCount;
		}

		public final boolean isForced() {
			return forced;
		}

		public final NBTTagCompound getCustomTag() {
			return customTag;
		}

		private int getNumToSpawn(Random rand) {
			int randRange = maxToSpawn - minToSpawn;
			int toSpawn;
			if (randRange <= 0) {
				toSpawn = minToSpawn;
			} else {
				toSpawn = minToSpawn + rand.nextInt(randRange);
			}
			if (remainingSpawnCount >= 0 && toSpawn > remainingSpawnCount) {
				toSpawn = remainingSpawnCount;
			}
			return toSpawn;
		}

		private void spawnEntities(World world, BlockPos spawnPos, int range) {
			int toSpawn = getNumToSpawn(world.rand);

			for (int i = 0; i < toSpawn; i++) {
				Entity e = EntityList.createEntityByIDFromName(entityId, world);
				if (e == null)
					return;
				boolean doSpawn = false;
				int spawnTry = 0;
				while (!doSpawn && spawnTry < range + 5) {
					int x = spawnPos.getX() - range + world.rand.nextInt(range * 2 + 1);
					int z = spawnPos.getZ() - range + world.rand.nextInt(range * 2 + 1);
					for (int y = spawnPos.getY() - range; y <= spawnPos.getY() + range; y++) {
						e.setLocationAndAngles(x + 0.5d, y, z + 0.5d, world.rand.nextFloat() * 360, 0);
						if (!forced && e instanceof EntityLiving) {
							doSpawn = ((EntityLiving) e).getCanSpawnHere() && ((EntityLiving) e).isNotColliding();
							if (doSpawn)
								break;
						} else {
							doSpawn = true;
							break;
						}
					}
					spawnTry++;
				}
				if (doSpawn) {
					spawnEntityAt(e, world);
					if (remainingSpawnCount > 0) {
						remainingSpawnCount--;
					}
				}
			}
		}

		//TODO  sendSoundPacket(world, pos);
		private void spawnEntityAt(Entity e, World world) {
			if (e instanceof EntityLiving) {
				((EntityLiving) e).onInitialSpawn(world.getDifficultyForLocation(e.getPosition()), null);
				((EntityLiving) e).spawnExplosionParticle();
			}
			if (customTag != null) {
				NBTTagCompound temp = new NBTTagCompound();
				e.writeToNBT(temp);
				Set<String> keys = customTag.getKeySet();
				for (String key : keys) {
					temp.setTag(key, customTag.getTag(key));
				}
				e.readFromNBT(temp);
			}
			world.spawnEntity(e);
		}
	}
}
