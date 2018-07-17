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

package net.shadowmage.ancientwarfare.structure.template;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.structure.network.PacketStructure;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureRemove;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StructureTemplateManager {
	private HashMap<String, StructureTemplateClient> clientTemplates = new HashMap<>();//server-side client-templates
	private HashMap<String, BufferedImage> templateImages = new HashMap<>();//server-side images
	private HashMap<String, StructureTemplate> loadedTemplates = new HashMap<>();

	private StructureTemplateManager() {
	}

	public static final StructureTemplateManager INSTANCE = new StructureTemplateManager();

	public void addTemplate(StructureTemplate template) {
		if (template.getValidationSettings() == null) {
			return;
		}
		if (template.getValidationSettings().isWorldGenEnabled()) {
			WorldGenStructureManager.INSTANCE.registerWorldGenStructure(template);
		}
		loadedTemplates.put(template.name, template);
		StructureTemplateClient cl = new StructureTemplateClient(template);
		clientTemplates.put(template.name, cl);

		MinecraftServer server = FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? FMLCommonHandler.instance().getMinecraftServerInstance() : null;
		if (server != null && server.isServerRunning() && server.getPlayerList() != null) {
			NBTTagCompound tag = new NBTTagCompound();
			cl.writeToNBT(tag);
			PacketStructure pkt = new PacketStructure();
			pkt.packetData.setTag("singleStructure", tag);
			NetworkHandler.sendToAllPlayers(pkt);
		} else if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			StructureTemplateManagerClient.instance().addTemplate(cl);
		}
	}

	public void onPlayerConnect(EntityPlayerMP player) {
		NBTTagList list = new NBTTagList();
		for (StructureTemplateClient cl : clientTemplates.values()) {
			NBTTagCompound tag = new NBTTagCompound();
			cl.writeToNBT(tag);
			list.appendTag(tag);
		}
		PacketStructure pkt = new PacketStructure();
		pkt.packetData.setTag("structureList", list);
		NetworkHandler.sendToPlayer(player, pkt);
	}

	public boolean removeTemplate(String name) {
		if (this.loadedTemplates.containsKey(name)) {
			this.loadedTemplates.remove(name);
			this.clientTemplates.remove(name);
			this.templateImages.remove(name);
			NetworkHandler.sendToAllPlayers(new PacketStructureRemove(name));
			return true;
		}
		return false;
	}

	public void removeAll() {
		//creating a new list because otherwise we run into concurrent modification exception as the collection is both queried and modified
		new ArrayList<>(loadedTemplates.keySet()).forEach(this::removeTemplate);
	}

	public StructureTemplate getTemplate(String name) {
		return this.loadedTemplates.get(name);
	}

	public void addTemplateImage(String imageName, BufferedImage image) {
		this.templateImages.put(imageName, image);
	}

	public BufferedImage getTemplateImage(String imageName) {
		return templateImages.get(imageName);
	}

	public Map<String, StructureTemplate> getSurvivalStructures() {
		return loadedTemplates.entrySet().stream().filter(e -> e.getValue().getValidationSettings().isSurvival()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
