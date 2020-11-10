package com.halotroop.mcopenvr.client.provider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRApplications_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Applications object provider
 *
 * @author halotroop2288
 */
public final class OpenVrApplications {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Apps");
	protected static VR_IVRApplications_FnTable instance;

	public static VR_IVRApplications_FnTable get() {
		return instance;
	}

	static void initOpenVRApplications() {
		instance = new VR_IVRApplications_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRApplications_Version, McOpenVr.hmdErrorStoreBuf));
		if (!McOpenVr.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenVR Applications initialized OK");
		} else {
			LOGGER.error("VRApplications init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(McOpenVr.getError()).getString(0));
			instance = null;
		}
	}
}
