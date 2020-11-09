package com.halotroop.mcopenvr.client.provider;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRCompositor_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Compositor object provider
 *
 * @author halotroop2288
 */
public class OpenVRCompositor {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Compositor");

	protected static VR_IVRCompositor_FnTable instance;

	public static void initOpenVRCompositor() throws Exception {
		if (MCOpenVR.vrSystem != null) {
			instance = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, MCOpenVR.hmdErrorStoreBuf));
			if (!MCOpenVR.isError()) {
				LOGGER.info("OpenVR Compositor initialized OK.");
				instance.setAutoSynch(false);
				instance.read();
				instance.SetTrackingSpace.apply(JOpenVRLibrary.ETrackingUniverseOrigin.ETrackingUniverseOrigin_TrackingUniverseStanding);

				int buffSize = 20;
				Pointer s = new Memory(buffSize);

				LOGGER.info("TrackingSpace: " + instance.GetTrackingSpace.apply());

				MCOpenVR.vrSystem.GetStringTrackedDeviceProperty.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd,
						JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_ManufacturerName_String,
						s, buffSize, MCOpenVR.getHmdErrorStore());
				String id = s.getString(0);
				LOGGER.debug("Device manufacturer is: " + id);

				MCOpenVR.setDetectedHardware(HardwareType.fromManufacturer(id));
//				VRHotkeys.loadExternalCameraConfig(); // TODO, is this even needed?
			} else {
				throw new Exception(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(MCOpenVR.getError()).getString(0));
			}
		}

		if (instance == null) {
			System.out.println("Skipping VR Compositor...");
			if (MCOpenVR.vrSystem != null) {
				MCOpenVR.setVsyncToPhotons(MCOpenVR.vrSystem.GetFloatTrackedDeviceProperty
						.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd, JOpenVRLibrary.ETrackedDeviceProperty
										.ETrackedDeviceProperty_Prop_SecondsFromVsyncToPhotons_Float,
								MCOpenVR.getHmdErrorStore()));
			} else {
				MCOpenVR.setVsyncToPhotons(0f);
			}
		}

		// left eye
		MCOpenVR.texBounds.uMax = 1f;
		MCOpenVR.texBounds.uMin = 0f;
		MCOpenVR.texBounds.vMax = 1f;
		MCOpenVR.texBounds.vMin = 0f;
		MCOpenVR.texBounds.setAutoSynch(false);
		MCOpenVR.texBounds.setAutoRead(false);
		MCOpenVR.texBounds.setAutoWrite(false);
		MCOpenVR.texBounds.write();


		// texture type
		MCOpenVR.texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		MCOpenVR.texType0.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		MCOpenVR.texType0.handle = Pointer.createConstant(-1);
		MCOpenVR.texType0.setAutoSynch(false);
		MCOpenVR.texType0.setAutoRead(false);
		MCOpenVR.texType0.setAutoWrite(false);
		MCOpenVR.texType0.write();


		// texture type
		MCOpenVR.texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		MCOpenVR.texType1.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		MCOpenVR.texType1.handle = Pointer.createConstant(-1);
		MCOpenVR.texType1.setAutoSynch(false);
		MCOpenVR.texType1.setAutoRead(false);
		MCOpenVR.texType1.setAutoWrite(false);
		MCOpenVR.texType1.write();

		MCOpenVR.LOGGER.info("OpenVR Compositor initialized OK.");
	}
}
