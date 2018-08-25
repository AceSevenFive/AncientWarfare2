/*
 Copyright 2012-2013 John Cummens (aka Shadowmage, Shadowmage4513)
 This software is distributed under the terms of the GNU General Public License.
 Please see COPYING for precise license information.

 This file is part of Ancient Warfare.

 Ancient Warfare is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Ancient Warfare is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.structure.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.core.util.Json;
import net.shadowmage.ancientwarfare.core.util.JsonTagReader;
import net.shadowmage.ancientwarfare.core.util.JsonTagWriter;
import net.shadowmage.ancientwarfare.structure.api.TemplateParsingException.TemplateRuleParsingException;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuildingException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/*
 * base template-rule class.  Plugins should define their own rule classes.
 * all data to place the block/entity/target of the rule must be contained in the rule.
 * ONLY one rule per block-position in the template.  So -- no entity/block combination in same space unless
 * handled specially via a plugin rule
 *
 * @author Shadowmage
 */
public abstract class TemplateRule {

	public int ruleNumber = -1;

	/*
	 * all sub-classes must implement a no-param constructor for when loaded from file (at which point they should initialize from the parseRuleData method)
	 */
	public TemplateRule() {

	}

	/*
	 * input params are the target position for placement of this rule and destination orientation
	 */
	public abstract void handlePlacement(World world, int turns, BlockPos pos, IStructureBuilder builder) throws StructureBuildingException;

	public abstract void parseRuleData(NBTTagCompound tag);

	public abstract void writeRuleData(NBTTagCompound tag);

	public abstract void addResources(NonNullList<ItemStack> resources);

	public abstract boolean shouldPlaceOnBuildPass(World world, int turns, BlockPos pos, int buildPass);

	public void writeRule(BufferedWriter out) throws IOException {
		NBTTagCompound tag = new NBTTagCompound();
		writeRuleData(tag);
		writeTag(out, tag);
	}

	public void parseRule(int ruleNumber, List<String> lines) throws TemplateRuleParsingException {
		this.ruleNumber = ruleNumber;
		NBTTagCompound tag = readTag(lines);
		parseRuleData(tag);
	}

	public final void writeTag(BufferedWriter out, NBTTagCompound tag) throws IOException {
		String line = Json.getJsonData(JsonTagWriter.getJsonForTag(tag));
		out.write(line);
		out.newLine();
	}

	public final NBTTagCompound readTag(List<String> ruleData) throws TemplateRuleParsingException {
		for (String line : ruleData)
		{
			if (line.startsWith("JSON:{")) {
				return JsonTagReader.parseTagCompound(line);
			}
		}
		return new NBTTagCompound();
	}

	@Override
	public String toString() {
		return "Template rule: " + ruleNumber + " type: " + getClass().getSimpleName();
	}

	protected BlockPos getBlockPosFromNBT(NBTTagCompound tag) {
		return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
	}

	protected NBTTagCompound writeBlockPosToNBT(NBTTagCompound tag, BlockPos pos) {
		tag.setInteger("x", pos.getX());
		tag.setInteger("y", pos.getY());
		tag.setInteger("z", pos.getZ());
		return tag;
	}
}
