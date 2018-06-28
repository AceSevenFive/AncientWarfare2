package net.shadowmage.ancientwarfare.structure.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.gui.elements.Button;
import net.shadowmage.ancientwarfare.core.gui.elements.Checkbox;
import net.shadowmage.ancientwarfare.core.gui.elements.CompositeScrolled;
import net.shadowmage.ancientwarfare.core.gui.elements.GuiElement;
import net.shadowmage.ancientwarfare.core.gui.elements.Label;
import net.shadowmage.ancientwarfare.core.gui.elements.Text;

import java.util.Set;

public class GuiStructureBiomeSelection extends GuiContainerBase {

	private final GuiStructureScanner parent;

	private Checkbox whiteList;
	private Text searchBox;
	private CompositeScrolled area;
	private Listener listener;

	public GuiStructureBiomeSelection(GuiStructureScanner parent) {
		super(parent.getContainer());
		this.parent = parent;
		this.shouldCloseOnVanillaKeys = false;
	}

	@Override
	public void initElements() {

		addGuiElement(new Label(8, 8, I18n.format("guistrings.select_biomes") + ":"));

		whiteList = new Checkbox(8, 20, 16, 16, "guistrings.biome_whitelist") {
			@Override
			public void onToggled() {
				parent.getContainer().updateValidator(v -> v.setBiomeWhiteList(checked()));
			}
		};
		whiteList.setChecked(parent.getContainer().getValidator().isBiomeWhiteList());
		addGuiElement(whiteList);

		searchBox = new Text(80, 8, 170, "", this) {
			@Override
			protected void handleKeyInput(int keyCode, char ch) {
				String old = getText();
				super.handleKeyInput(keyCode, ch);
				String text = getText();
				if (!text.equals(old)) {
					refreshGui();
				}
			}
		};
		addGuiElement(searchBox);
		area = new CompositeScrolled(this, 0, 40, 256, 200);
		this.addGuiElement(area);

		Button button = new Button(256 - 8 - 55, 20, 55, 12, "guistrings.done") {
			@Override
			protected void onPressed() {
				Minecraft.getMinecraft().displayGuiScreen(parent);
			}
		};
		addGuiElement(button);

		listener = new Listener(Listener.MOUSE_UP) {
			@Override
			public boolean onEvent(GuiElement widget, ActivationEvent evt) {
				if (widget.isMouseOverElement(evt.mx, evt.my)) {
					Set<String> biomeNames = parent.getContainer().getValidator().getBiomeList();
					if (((BiomeCheck) widget).checked()) {
						biomeNames.add(((BiomeCheck) widget).name);
					} else {
						biomeNames.remove(((BiomeCheck) widget).name);
					}
					parent.getContainer().updateValidator(v -> v.setBiomeList(biomeNames));
				}
				return true;
			}
		};
		refreshBiomeList();
	}

	private void refreshBiomeList() {
		int totalHeight = 3;
		Set<String> biomeNames = parent.getContainer().getValidator().getBiomeList();
		area.clearElements();
		String name;
		Checkbox box;
		for (Biome biome : Biome.REGISTRY) {
			if (biome == null) {
				continue;
			}
			//noinspection ConstantConditions
			name = biome.getRegistryName().toString();
			if (name.contains(searchBox.getText()) || biome.getBiomeName().contains(searchBox.getText())) {
				box = new BiomeCheck(totalHeight, biome);
				area.addGuiElement(box);
				totalHeight += 16;
				if (biomeNames.contains(name)) {
					box.setChecked(true);
				}
				box.addNewListener(listener);
			}
		}
		area.setAreaSize(totalHeight);
	}

	@Override
	public void setupElements() {
		whiteList.setChecked(parent.getContainer().getValidator().isBiomeWhiteList());
		refreshBiomeList();
	}

	private class BiomeCheck extends Checkbox {

		private final String name;

		/*
		 * @param topLeftY height of display
		 * @param label text displayed
		 */
		public BiomeCheck(int topLeftY, Biome biome) {
			super(8, topLeftY, 16, 16, String.format("%s (%s)", biome.getBiomeName(), biome.getRegistryName().toString()));
			//noinspection ConstantConditions
			this.name = biome.getRegistryName().toString();
		}
	}
}
