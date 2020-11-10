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
public class OpenVrCompositor {
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Compositor");

	protected static VR_IVRCompositor_FnTable instance;

	public static void initOpenVRCompositor() throws Exception {
		if (McOpenVr.vrSystem != null) {
			instance = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, McOpenVr.hmdErrorStoreBuf));
			if (!McOpenVr.isError()) {
				LOGGER.info("OpenVR Compositor initialized OK.");
				instance.setAutoSynch(false);
				instance.read();
				instance.SetTrackingSpace.apply(JOpenVRLibrary.ETrackingUniverseOrigin.ETrackingUniverseOrigin_TrackingUniverseStanding);

				int buffSize = 20;
				Pointer s = new Memory(buffSize);

				LOGGER.info("TrackingSpace: " + instance.GetTrackingSpace.apply());

				McOpenVr.vrSystem.GetStringTrackedDeviceProperty.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd,
						JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_ManufacturerName_String,
						s, buffSize, McOpenVr.getHmdErrorStore());
				String id = s.getString(0);
				LOGGER.debug("Device manufacturer is: " + id);

				McOpenVr.setDetectedHardware(HardwareType.fromManufacturer(id));
//				VRHotkeys.loadExternalCameraConfig(); // TODO, is this even needed?
			} else {
				throw new Exception(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(McOpenVr.getError()).getString(0));
			}
		}

		if (instance == null) {
			System.out.println("Skipping VR Compositor...");
			if (McOpenVr.vrSystem != null) {
				McOpenVr.setVsyncToPhotons(McOpenVr.vrSystem.GetFloatTrackedDeviceProperty
						.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd, JOpenVRLibrary.ETrackedDeviceProperty
										.ETrackedDeviceProperty_Prop_SecondsFromVsyncToPhotons_Float,
								McOpenVr.getHmdErrorStore()));
			} else {
				McOpenVr.setVsyncToPhotons(0f);
			}
		}

		// left eye
		McOpenVr.texBounds.uMax = 1f;
		McOpenVr.texBounds.uMin = 0f;
		McOpenVr.texBounds.vMax = 1f;
		McOpenVr.texBounds.vMin = 0f;
		McOpenVr.texBounds.setAutoSynch(false);
		McOpenVr.texBounds.setAutoRead(false);
		McOpenVr.texBounds.setAutoWrite(false);
		McOpenVr.texBounds.write();


		// texture type
		McOpenVr.texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		McOpenVr.texType0.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		McOpenVr.texType0.handle = Pointer.createConstant(-1);
		McOpenVr.texType0.setAutoSynch(false);
		McOpenVr.texType0.setAutoRead(false);
		McOpenVr.texType0.setAutoWrite(false);
		McOpenVr.texType0.write();


		// texture type
		McOpenVr.texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		McOpenVr.texType1.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		McOpenVr.texType1.handle = Pointer.createConstant(-1);
		McOpenVr.texType1.setAutoSynch(false);
		McOpenVr.texType1.setAutoRead(false);
		McOpenVr.texType1.setAutoWrite(false);
		McOpenVr.texType1.write();

		McOpenVr.LOGGER.info("OpenVR Compositor initialized OK.");
	}
}
