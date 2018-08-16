package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.core.api.ModuleStatus;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;

public class RFProxy {
	//TODO  look into simplifying this implementation / I don't think this proxy stuff is even needed.
	public static RFProxy instance;

	public static void loadInstance() {
		if (ModuleStatus.redstoneFluxEnabled) {
			try {
				instance = (RFProxy) Class.forName("net.shadowmage.ancientwarfare.automation.proxy.RFProxyActual").newInstance();
				AncientWarfareAutomation.LOG.info("RF Compatability loaded successfully");
			}
			catch (Exception e) {
				e.printStackTrace();
				instance = new RFProxy();
			}
		} else {
			instance = new RFProxy();
		}
	}

	protected RFProxy() {
		//NOOP
	}

	public boolean isRFTile(TileEntity te) {
		return false;
	}

	public double transferPower(ITorqueTile generator, EnumFacing from, TileEntity target) {
		return 0;
	}

}
