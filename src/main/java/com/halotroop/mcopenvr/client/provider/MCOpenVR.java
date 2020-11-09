package com.halotroop.mcopenvr.client.provider;

import com.halotroop.mcopenvr.client.VRSettings;
import com.halotroop.mcopenvr.client.api.Vec3History;
import com.halotroop.mcopenvr.client.control.ControllerType;
import com.halotroop.mcopenvr.client.control.HapticScheduler;
import com.halotroop.mcopenvr.client.control.TrackedController;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jopenvr.*;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.IntBuffer;

public class MCOpenVR {
	public static final Logger LOGGER = LogManager.getLogger("MCOpenVR");
	public static final Matrix4f hmdRotation = new Matrix4f();
	final static VRTextureBounds_t texBounds = new VRTextureBounds_t();
	final static Texture_t texType0 = new Texture_t();
	final static Texture_t texType1 = new Texture_t();
	// position/orientation of headset and eye offsets
	private static final Matrix4f hmdPose = new Matrix4f();
	private static final int RIGHT_CONTROLLER = 0;
	private static final int LEFT_CONTROLLER = 1;
	public static VRSettings settings;
	public static VR_IVRSystem_FnTable vrSystem;
	public static VR_IVROCSystem_FnTable vrOpenComposite;
	public static VR_IVRInput_FnTable vrInput;
	public static Vec3History hmdHistory = new Vec3History();
	public static Vec3History hmdPivotHistory = new Vec3History();
	public static Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
	public static Vec3History[] controllerForwardHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
	public static Vec3History[] controllerUpHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
	public static Vector3d[] aimSource = new Vector3d[3];
	public static Matrix4f[] controllerPose = new Matrix4f[3];
	public static Matrix4f[] controllerRotation = new Matrix4f[3];
	public static Matrix4f[] handRotation = new Matrix4f[3];
	public static int[] controllerDeviceIndex = new int[3];
	public static TrackedController[] controllers = new TrackedController[2];
	protected static boolean tried = false;
	static String initStatus;
	static VR_IVRCompositor_FnTable vrCompositor;
	static VR_IVROverlay_FnTable vrOverlay;
	static VR_IVRSettings_FnTable vrSettings;
	static VR_IVRRenderModels_FnTable vrRenderModels;
	static VR_IVRChaperone_FnTable vrChaperone;
	static VR_IVRApplications_FnTable vrApplications;
	static Matrix4f hmdPoseLeftEye = new Matrix4f();
	static Matrix4f hmdPoseRightEye = new Matrix4f();
	static boolean initSuccess = false, flipEyes = false;
	private static boolean initialized;
	private static boolean inputInitialized;
	private static final IntByReference hmdErrorStore = new IntByReference();
	private static IntBuffer hmdErrorStoreBuf;
	private static TrackedDevicePose_t.ByReference hmdTrackedDevicePoseReference;
	private static TrackedDevicePose_t[] hmdTrackedDevicePoses;
	private static Matrix4f[] poseMatrices;
	private static Vector3d[] deviceVelocity;
	private static IntBuffer hmdDisplayFrequency;
	private static float vsyncToPhotons;
	private static double timePerFrame, frameCountRun;
	private static long frameCount;
	private static final boolean TPose = false;
	private static HapticScheduler hapticScheduler;
	private static InputPoseActionData_t.ByReference poseData;
	private static InputOriginInfo_t.ByReference originInfo;
	private static long leftPoseHandle;
	private static long rightPoseHandle;
	private static long leftHapticHandle;
	private static long rightHapticHandle;
	private static long externalCameraPoseHandle;
	private static final boolean[] controllerTracking = new boolean[3];
	/** Do not make this public and reference it! Call the {@link #getHardwareType()} method instead! */
	private static HardwareType detectedHardware = HardwareType.VIVE;
	private final LongByReference oHandle = new LongByReference();
	public MCOpenVR() {
		for (int i = 0; i < 3; i++) {
			aimSource[i] = new Vector3d(0, 0, 0);
			controllerPose[i] = new Matrix4f();
			controllerRotation[i] = new Matrix4f();
			handRotation[i] = new Matrix4f();
			controllerDeviceIndex[i] = -1;
		}
		
		poseData = new InputPoseActionData_t.ByReference();
		poseData.setAutoRead(false);
		poseData.setAutoWrite(false);
		poseData.setAutoSynch(false);
		
		originInfo = new InputOriginInfo_t.ByReference();
		originInfo.setAutoRead(false);
		originInfo.setAutoWrite(false);
		originInfo.setAutoSynch(false);
	}

	public static boolean init() {
		if (initialized) return true;
		if (tried) return false;
		tried = true;
		
		if (JOpenVRLibrary.VR_IsHmdPresent() == 0) {
			initStatus = "VR Headset not detected.";
			return false;
		}
		
		try {
			initializeJOpenVR();
			initOpenVRCompositor();
			initOpenVRSettings();
			initOpenVRRenderModels();
			initOpenVRChaperone();
			initOpenVRApplications();
			initOpenVRInput();
			initOpenComposite();
		} catch (Exception e) {
			e.printStackTrace();
			initSuccess = false;
			initStatus = e.getLocalizedMessage();
			return false;
		}
		
		if (vrInput == null) {
			LOGGER.warn("Controller input not available.");
		}
		
		LOGGER.info("OpenVR initialized & VR connected.");
		
		controllers[RIGHT_CONTROLLER] = new TrackedController(ControllerType.RIGHT);
		controllers[LEFT_CONTROLLER] = new TrackedController(ControllerType.LEFT);
		
		deviceVelocity = new Vector3d[JOpenVRLibrary.k_unMaxTrackedDeviceCount];
		
		for (int i = 0; i < poseMatrices.length; i++) {
			poseMatrices[i] = new Matrix4f();
			deviceVelocity[i] = new Vector3d(0, 0, 0);
		}
		
		hapticScheduler = new HapticScheduler();
		
		initialized = true;
		
		return true;
	}
	
	private static void initOpenComposite() {
		vrOpenComposite = new VR_IVROCSystem_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(VR_IVROCSystem_FnTable.Version, hmdErrorStoreBuf));
		if (!isError()) {
			vrOpenComposite.setAutoSynch(false);
			vrOpenComposite.read();
			LOGGER.info("OpenComposite initialized.");
		} else {
			LOGGER.error("OpenComposite not found: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			vrOpenComposite = null;
		}
	}
	
	private static void initOpenVRChaperone() {
		vrChaperone = new VR_IVRChaperone_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRChaperone_Version, hmdErrorStoreBuf));
		if (!isError()) {
			vrChaperone.setAutoSynch(false);
			vrChaperone.read();
			System.out.println("OpenVR chaperone initialized.");
		} else {
			System.out.println("VRChaperone init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			vrChaperone = null;
		}
	}
	
	private static void initOpenVRRenderModels() {
		vrRenderModels = new VR_IVRRenderModels_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRRenderModels_Version, hmdErrorStoreBuf));
		if (!isError()) {
			vrRenderModels.setAutoSynch(false);
			vrRenderModels.read();
			LOGGER.info("OpenVR RenderModels initialized OK");
		} else {
			LOGGER.error("VRRenderModels init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			vrRenderModels = null;
		}
	}
	
	private static void initOpenVRSettings() {
		vrSettings = new VR_IVRSettings_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSettings_Version,
				hmdErrorStoreBuf));
		if (!isError()) {
			vrSettings.setAutoSynch(false);
			vrSettings.read();
			LOGGER.info("OpenVR Settings initialized OK");
		} else {
			LOGGER.error("VRSettings init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(getError())
					.getString(0));
			vrSettings = null;
		}
	}
	
	static void initOpenVRApplications() {
		vrApplications = new VR_IVRApplications_FnTable(JOpenVRLibrary
				.VR_GetGenericInterface(JOpenVRLibrary.IVRApplications_Version, hmdErrorStoreBuf));
		if (!isError()) {
			vrApplications.setAutoSynch(false);
			vrApplications.read();
			LOGGER.info("OpenVR Applications initialized OK");
		} else {
			LOGGER.error("VRApplications init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			vrApplications = null;
		}
	}
	
	private static void initOpenVRInput() {
		vrInput = new VR_IVRInput_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary
				.IVRInput_Version, hmdErrorStoreBuf));
		if (!isError()) {
			vrInput.setAutoSynch(false);
			vrInput.read();
			LOGGER.info("OpenVR Input initialized OK");
		} else {
			LOGGER.error("VRInput init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			vrInput = null;
		}
	}
	
	private static void initializeJOpenVR() {
		hmdErrorStoreBuf = IntBuffer.allocate(1);
		vrSystem = null;
		JOpenVRLibrary.VR_InitInternal(hmdErrorStoreBuf, JOpenVRLibrary
				.EVRApplicationType.EVRApplicationType_VRApplication_Scene);
		
		if (!isError()) {
			// ok, try and get the vrsystem pointer..
			vrSystem = new VR_IVRSystem_FnTable(JOpenVRLibrary
					.VR_GetGenericInterface(JOpenVRLibrary.IVRSystem_Version, hmdErrorStoreBuf));
		}
		
		if (vrSystem == null || isError()) {
			throw new RuntimeException(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(getError())
					.getString(0));
		} else {
			vrSystem.setAutoSynch(false);
			vrSystem.read();
			
			System.out.println("OpenVR System Initialized OK.");
			
			hmdDisplayFrequency = IntBuffer.allocate(1);
			hmdDisplayFrequency.put(JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_DisplayFrequency_Float);
			hmdTrackedDevicePoseReference = new TrackedDevicePose_t.ByReference();
			hmdTrackedDevicePoses = (TrackedDevicePose_t[]) hmdTrackedDevicePoseReference.toArray(JOpenVRLibrary.k_unMaxTrackedDeviceCount);
			poseMatrices = new Matrix4f[JOpenVRLibrary.k_unMaxTrackedDeviceCount];
			for (int i = 0; i < poseMatrices.length; i++) poseMatrices[i] = new Matrix4f();
			
			timePerFrame = 1.0 / hmdDisplayFrequency.get(0);
			
			// disable all this stuff which kills performance
			hmdTrackedDevicePoseReference.setAutoRead(false);
			hmdTrackedDevicePoseReference.setAutoWrite(false);
			hmdTrackedDevicePoseReference.setAutoSynch(false);
			
			for (int i = 0; i < JOpenVRLibrary.k_unMaxTrackedDeviceCount; i++) {
				hmdTrackedDevicePoses[i].setAutoRead(false);
				hmdTrackedDevicePoses[i].setAutoWrite(false);
				hmdTrackedDevicePoses[i].setAutoSynch(false);
			}
			
			initSuccess = true;
		}
	}
	
	public static HardwareType getHardwareType() {
		return settings.forceHardwareDetection > 0 ? HardwareType.values()[settings.forceHardwareDetection - 1] : detectedHardware;
	}
	
	public static void initOpenVRCompositor() throws Exception {
		if (vrSystem != null) {
			vrCompositor = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, hmdErrorStoreBuf));
			if (!isError()) {
				System.out.println("OpenVR Compositor initialized OK.");
				vrCompositor.setAutoSynch(false);
				vrCompositor.read();
				vrCompositor.SetTrackingSpace.apply(
						JOpenVRLibrary.ETrackingUniverseOrigin.ETrackingUniverseOrigin_TrackingUniverseStanding);
				
				int buffSize = 20;
				Pointer s = new Memory(buffSize);
				
				System.out.println("TrackingSpace: " + vrCompositor.GetTrackingSpace.apply());
				
				vrSystem.GetStringTrackedDeviceProperty.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd,
						JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_ManufacturerName_String,
						s, buffSize, hmdErrorStore);
				String id = s.getString(0);
				System.out.println("Device manufacturer is: " + id);
				
				detectedHardware = HardwareType.fromManufacturer(id);
//				VRHotkeys.loadExternalCameraConfig(); // TODO, is this even needed?
			} else {
				throw new Exception(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(getError()).getString(0));
			}
		}
		
		if (vrCompositor == null) {
			System.out.println("Skipping VR Compositor...");
			if (vrSystem != null) {
				vsyncToPhotons = vrSystem.GetFloatTrackedDeviceProperty.apply(JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd, JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_SecondsFromVsyncToPhotons_Float, hmdErrorStore);
			} else {
				vsyncToPhotons = 0f;
			}
		}
		
		// left eye
		texBounds.uMax = 1f;
		texBounds.uMin = 0f;
		texBounds.vMax = 1f;
		texBounds.vMin = 0f;
		texBounds.setAutoSynch(false);
		texBounds.setAutoRead(false);
		texBounds.setAutoWrite(false);
		texBounds.write();
		
		
		// texture type
		texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		texType0.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		texType0.handle = Pointer.createConstant(-1);
		texType0.setAutoSynch(false);
		texType0.setAutoRead(false);
		texType0.setAutoWrite(false);
		texType0.write();
		
		
		// texture type
		texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		texType1.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		texType1.handle = Pointer.createConstant(-1);
		texType1.setAutoSynch(false);
		texType1.setAutoRead(false);
		texType1.setAutoWrite(false);
		texType1.write();
		
		LOGGER.info("OpenVR Compositor initialized OK.");
	}
	
	private static int getError() {
		return hmdErrorStore.getValue() != 0 ? hmdErrorStore.getValue() : hmdErrorStoreBuf.get(0);
	}
	
	public static boolean isError() {
		return hmdErrorStore.getValue() != 0 || hmdErrorStoreBuf.get(0) != 0;
	}
	
	
	public static void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		if (settings.vrDisableControllerInput || !inputInitialized) return;
		if (settings.vrReverseHands) {
			if (controller == ControllerType.RIGHT) controller = ControllerType.LEFT;
			else controller = ControllerType.RIGHT;
		}
		
		hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
	}
	
	public static void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
		triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0);
	}
	
	public static long getHapticHandle(ControllerType hand) {
		return hand == ControllerType.RIGHT ? rightHapticHandle : leftHapticHandle;
	}
	
	// this is a super dumb method. Surely there must be a better way than this?
	public static String getInputError(int code) {
		switch (code) {
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_BufferTooSmall:
				return "BufferTooSmall";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidBoneCount:
				return "InvalidBoneCount";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidBoneIndex:
				return "InvalidBoneIndex";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidCompressedData:
				return "InvalidCompressedData";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidDevice:
				return "InvalidDevice";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidHandle:
				return "InvalidHandle";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidParam:
				return "InvalidParam";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_InvalidSkeleton:
				return "InvalidSkeleton";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_IPCError:
				return "IPCError";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_MaxCapacityReached:
				return "MaxCapacityReached";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_MismatchedActionManifest:
				return "MismatchedActionManifest";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_MissingSkeletonData:
				return "MissingSkeletonData";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_NameNotFound:
				return "NameNotFound";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_NoActiveActionSet:
				return "NoActiveActionSet";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_NoData:
				return "NoData";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_None:
				return "wat";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_NoSteam:
				return "NoSteam";
			case JOpenVRLibrary.EVRInputError.EVRInputError_VRInputError_WrongType:
				return "WrongType";
			default:
				return "Unknown";
		}
	}
	
	public static boolean isControllerTracking(int controller) {
		return controllerTracking[controller];
	}
	
	public static boolean isControllerTracking(ControllerType controller) {
		return isControllerTracking(controller.ordinal());
	}
}
