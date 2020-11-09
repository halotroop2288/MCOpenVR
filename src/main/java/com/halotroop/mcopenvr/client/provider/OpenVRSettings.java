package com.halotroop.mcopenvr.client.provider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRSettings_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Settings object provider
 *
 * @author halotroop2288
 */
public final class OpenVRSettings {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Settings");
	protected static VR_IVRSettings_FnTable instance;

	public static VR_IVRSettings_FnTable get() {
		return instance;
	}

	static void initOpenVRSettings() {
		instance = new VR_IVRSettings_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSettings_Version,
				MCOpenVR.hmdErrorStoreBuf));
		if (!MCOpenVR.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			MCOpenVR.LOGGER.info("OpenVR Settings initialized OK");
		} else {
			MCOpenVR.LOGGER.error("VRSettings init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(MCOpenVR.getError())
					.getString(0));
			instance = null;
		}
	}
}
