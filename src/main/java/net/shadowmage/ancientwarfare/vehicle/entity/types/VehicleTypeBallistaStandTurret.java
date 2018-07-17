package net.shadowmage.ancientwarfare.vehicle.entity.types;

import net.minecraft.util.ResourceLocation;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

public class VehicleTypeBallistaStandTurret extends VehicleTypeBallista {
	public VehicleTypeBallistaStandTurret(int typeNum) {
		super(typeNum);
		this.configName = "ballista_stand_turret";
		this.baseMissileVelocityMax = 42.f;//stand versions should have higher velocity, as should fixed version--i.e. mobile turret should have the worst of all versions
		this.width = 1.2f;
		this.height = 1.4f;

		this.armorBaySize = 4;
		this.upgradeBaySize = 4;

		this.turretVerticalOffset = 18.f * 0.0625f;
		this.riderForwardsOffset = -1.8f;
		this.riderVerticalOffset = 0.35f;
		this.riderSits = false;
		this.drivable = true;//adjust based on isMobile or not
		this.baseForwardSpeed = 0.f;
		this.baseStrafeSpeed = .5f;
		this.riderMovesWithTurret = true;
		this.yawAdjustable = true;//adjust based on hasTurret or not
		this.turretRotationMax = 45.f;
		this.displayName = "item.vehicleSpawner.5";
		this.displayTooltip.add("item.vehicleSpawner.tooltip.torsion");
		this.displayTooltip.add("item.vehicleSpawner.tooltip.fixed");
		this.displayTooltip.add("item.vehicleSpawner.tooltip.midturret");
	}

	@Override
	public ResourceLocation getTextureForMaterialLevel(int level) {
		switch (level) {
			case 0:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_1.png");
			case 1:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_2.png");
			case 2:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_3.png");
			case 3:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_4.png");
			case 4:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_5.png");
			default:
				return new ResourceLocation(AncientWarfareCore.MOD_ID, "textures/model/vehicle/ballista_stand_1.png");
		}
	}

}
