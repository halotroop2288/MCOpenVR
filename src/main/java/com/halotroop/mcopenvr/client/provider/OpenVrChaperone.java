package com.halotroop.mcopenvr.client.provider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRChaperone_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Chaperone object provider
 *
 * @author halotroop2288
 */
public final class OpenVrChaperone {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Chaperone");
	protected static VR_IVRChaperone_FnTable instance;

	public static VR_IVRChaperone_FnTable get() {
		return instance;
	}

	static void initOpenVRChaperone() {
		instance = new VR_IVRChaperone_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRChaperone_Version, McOpenVr.hmdErrorStoreBuf));
		if (!McOpenVr.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenVR chaperone initialized.");
		} else {
			LOGGER.error("VRChaperone init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(McOpenVr.getError()).getString(0));
			instance = null;
		}
	}
}
