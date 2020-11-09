package com.halotroop.mcopenvr.client;

import com.halotroop.mcopenvr.client.provider.MCOpenVR;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads natives before any rendering calls are made,
 * and initializes OpenVR before Minecraft starts.
 *
 * @author halotroop2288
 */
@Environment(EnvType.CLIENT)
public final class NativeLoaderEntrypoint implements PrePreLaunch {
	public static final Logger LOGGER = LogManager.getLogger("MCOpenVR");

	@Override
	public void onPrePreLaunch() {
		try {
			Class.forName("jopenvr.JOpenVRLibrary");
			if (MCOpenVR.init()) LOGGER.info("MCOpenVR initialized.");
			else LOGGER.error("MCOpenVR not successfully initialized.");
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			LOGGER.error("JOpenVR failed to load: " + e.getMessage());
		}
	}
}
