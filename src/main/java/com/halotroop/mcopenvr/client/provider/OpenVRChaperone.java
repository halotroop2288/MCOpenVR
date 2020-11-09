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
public final class OpenVRChaperone {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Chaperone");
	protected static VR_IVRChaperone_FnTable instance;

	public static VR_IVRChaperone_FnTable get() {
		return instance;
	}

	static void initOpenVRChaperone() {
		instance = new VR_IVRChaperone_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRChaperone_Version, MCOpenVR.hmdErrorStoreBuf));
		if (!MCOpenVR.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenVR chaperone initialized.");
		} else {
			LOGGER.error("VRChaperone init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(MCOpenVR.getError()).getString(0));
			instance = null;
		}
	}
}
