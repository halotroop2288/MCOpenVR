package com.halotroop.mcopenvr.client.provider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVROCSystem_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenComposite object provider<br>
 * <br>
 * Warning: OpenComposite is not officially supported! <br>
 * This exists only to let developers know if their users are using OpenComposite.
 *
 * @author halotroop2288
 */
public final class OpenComposite {
	private static final Logger LOGGER = LogManager.getLogger("OpenComposite");

	public static VR_IVROCSystem_FnTable instance;

	public static VR_IVROCSystem_FnTable get() {
		return instance;
	}

	static void init() {
		instance = new VR_IVROCSystem_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(VR_IVROCSystem_FnTable.Version, McOpenVr.hmdErrorStoreBuf));
		if (!McOpenVr.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenComposite initialized.");
		} else {
			LOGGER.error("OpenComposite not found: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(McOpenVr.getError()).getString(0));
			instance = null;
		}
	}
}
