/**
 * Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 * This software is distributed under the terms of the GNU General Public License.
 * Please see COPYING for precise license information.
 * <p>
 * This file is part of Ancient Warfare.
 * <p>
 * Ancient Warfare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Ancient Warfare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.entity.Entity;
import net.minecraft.util.RayTraceResult;
import net.minecraft.world.World;
import shadowmage.ancient_warfare.common.item.ItemLoader;
import shadowmage.ancient_warfare.common.research.ResearchGoalNumbers;
import shadowmage.ancient_warfare.common.utils.ItemStackWrapperCrafting;

public class AmmoPebbleShot extends Ammo {

	/**
	 * @param ammoType
	 */
	public AmmoPebbleShot(int ammoType, int weight) {
		super(ammoType);
		this.isPersistent = false;
		this.isArrow = false;
		this.isRocket = false;
		this.ammoWeight = weight;
		this.secondaryAmmoCount = weight;
		this.secondaryAmmoType = ammoBallShot;
		float scaleFactor = weight + 45.f;
		this.renderScale = (weight / scaleFactor) * 2;
		this.iconTexture = "ammoPebble1";
		this.configName = "pebble_shot_" + weight;
		this.modelTexture = new ResourceLocation(AncientWarfareCore.modID, "model/vehicle/ammo/ammoStoneShot");

		this.neededResearch.add(ResearchGoalNumbers.explosives1);
		int cases = 1;
		int explosives = 1;
		this.numCrafted = 4;
		switch (weight) {
			case 10:
				this.neededResearch.add(ResearchGoalNumbers.ballistics1);
				cases = 1;
				explosives = 1;
				break;

			case 15:
				this.neededResearch.add(ResearchGoalNumbers.ballistics1);
				cases = 2;
				explosives = 2;
				break;

			case 30:
				this.neededResearch.add(ResearchGoalNumbers.ballistics2);
				cases = 4;
				explosives = 4;
				break;

			case 45:
				this.neededResearch.add(ResearchGoalNumbers.ballistics3);
				cases = 6;
				explosives = 6;
				break;
		}

		this.resources.add(new ItemStackWrapperCrafting(ItemLoader.clusterCharge, explosives, false, false));
		this.resources.add(new ItemStackWrapperCrafting(ItemLoader.explosiveCharge, explosives, false, false));
		this.resources.add(new ItemStackWrapperCrafting(ItemLoader.clayCasing, cases, false, false));
	}

	@Override
	public void onImpactWorld(World world, float x, float y, float z, MissileBase missile, RayTraceResult hit) {

	}

	@Override
	public void onImpactEntity(World world, Entity ent, float x, float y, float z, MissileBase missile) {

	}

}