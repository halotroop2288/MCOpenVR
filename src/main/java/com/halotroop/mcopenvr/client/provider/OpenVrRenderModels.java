package com.halotroop.mcopenvr.client.provider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRRenderModels_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Render Models object provider
 *
 * @author halotroop2288
 */
public final class OpenVrRenderModels {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Render Models");
	protected static VR_IVRRenderModels_FnTable instance;

	public static VR_IVRRenderModels_FnTable get() {
		return instance;
	}

	static void initOpenVRRenderModels() {
		instance = new VR_IVRRenderModels_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRRenderModels_Version, McOpenVr.hmdErrorStoreBuf));
		if (!McOpenVr.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenVR RenderModels initialized OK");
		} else {
			LOGGER.error("VRRenderModels init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(McOpenVr.getError()).getString(0));
			instance = null;
		}
	}
}
