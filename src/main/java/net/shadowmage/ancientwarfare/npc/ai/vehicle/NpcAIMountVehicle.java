package net.shadowmage.ancientwarfare.npc.ai.vehicle;

import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

public class NpcAIMountVehicle<T extends NpcBase & IVehicleUser> extends NpcAI<T> {
	private static final double MOUNT_REACH = 1.0D;

	public NpcAIMountVehicle(T npc) {
		super(npc);
	}

	@Override
	@SuppressWarnings("squid:S3655")
	public boolean shouldExecute() {
		return !npc.isRiding() && npc.canContinueRidingVehicle() && npc.getVehicle().isPresent() && !npc.getVehicle().get().isBeingRidden();
	}

	@Override
	@SuppressWarnings("squid:S3655")
	public void updateTask() {
		//noinspection ConstantConditions
		VehicleBase vehicle = npc.getVehicle().get();
		double distance = npc.getDistanceSq(vehicle.getPosition());

		if (npc.getEntityBoundingBox().grow(MOUNT_REACH).intersects(vehicle.getEntityBoundingBox())) {
			npc.startRiding(vehicle);
		} else {
			moveToPosition(vehicle.getPosition(), distance);
			npc.addAITask(TASK_MOVE);
		}
	}

	@Override
	public void resetTask() {
		super.resetTask();
		npc.resetVehicle();
		npc.removeAITask(TASK_MOVE);
	}
}
