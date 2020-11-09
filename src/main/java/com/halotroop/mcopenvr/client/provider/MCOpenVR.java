package com.halotroop.mcopenvr.client.provider;

import com.halotroop.mcopenvr.client.MCOpenVRConfig;
import com.halotroop.mcopenvr.client.api.Vec3History;
import com.halotroop.mcopenvr.client.control.ControllerType;
import com.halotroop.mcopenvr.client.control.HapticScheduler;
import com.halotroop.mcopenvr.client.control.TrackedController;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jopenvr.*;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.IntBuffer;

// Checkstyle doesn't like abbreviations.
public final class MCOpenVR implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("MCOpenVR");
	public static MCOpenVR instance;
	public static final Matrix4f hmdRotation = new Matrix4f();
	final static VRTextureBounds_t texBounds = new VRTextureBounds_t();
	final static Texture_t texType0 = new Texture_t();
	final static Texture_t texType1 = new Texture_t();
	// position/orientation of headset and eye offsets
	private static final Matrix4f hmdPose = new Matrix4f();
	private static final IntByReference hmdErrorStore = new IntByReference();
	private static final boolean TPose = false;
	public static MCOpenVRConfig modConfig;
	public static VR_IVRSystem_FnTable vrSystem;
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
	protected static boolean tried = false;
	static String initStatus;
	static VR_IVROverlay_FnTable vrOverlay;
	static Matrix4f hmdPoseLeftEye = new Matrix4f();
	static Matrix4f hmdPoseRightEye = new Matrix4f();
	static boolean initSuccess = false, flipEyes = false;
	static IntBuffer hmdErrorStoreBuf;
	private static boolean initialized;
	private static TrackedDevicePose_t.ByReference hmdTrackedDevicePoseReference;
	private static TrackedDevicePose_t[] hmdTrackedDevicePoses;
	private static Matrix4f[] poseMatrices;
	private static Vector3d[] deviceVelocity;
	private static IntBuffer hmdDisplayFrequency;
	private static float vsyncToPhotons;
	private static double timePerFrame, frameCountRun;
	private static long frameCount;
	private static InputPoseActionData_t.ByReference poseData;
	private static InputOriginInfo_t.ByReference originInfo;
	private static long leftPoseHandle;
	private static long rightPoseHandle;
	private static long externalCameraPoseHandle;
	/**
	 * Do not make this public and reference it! Call the {@link #getHardwareType()} method instead!
	 */
	private static HardwareType detectedHardware = HardwareType.VIVE;
	private final LongByReference oHandle = new LongByReference();
	private MCOpenVR() {
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

	static IntByReference getHmdErrorStore() {
		return hmdErrorStore;
	}

	public static float getVsyncToPhotons() {
		return vsyncToPhotons;
	}

	public static void setVsyncToPhotons(float vsyncToPhotons) {
		MCOpenVR.vsyncToPhotons = vsyncToPhotons;
	}

	static void setDetectedHardware(HardwareType detectedHardware) {
		MCOpenVR.detectedHardware = detectedHardware;
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
			initJOpenVR();
			OpenVRCompositor.initOpenVRCompositor();
			OpenVRSettings.initOpenVRSettings();
			OpenVRRenderModels.initOpenVRRenderModels();
			OpenVRChaperone.initOpenVRChaperone();
			OpenVRApplications.initOpenVRApplications();
			OpenVRInput.init();
			OpenComposite.init();
		} catch (Exception e) {
			e.printStackTrace();
			initSuccess = false;
			initStatus = e.getLocalizedMessage();
			return false;
		}

		if (OpenVRInput.instance == null) {
			LOGGER.warn("Controller input not available.");
		}

		LOGGER.info("OpenVR initialized & VR connected.");

		OpenVRInput.controllers[OpenVRInput.RIGHT_CONTROLLER] = new TrackedController(ControllerType.RIGHT);
		OpenVRInput.controllers[OpenVRInput.LEFT_CONTROLLER] = new TrackedController(ControllerType.LEFT);

		deviceVelocity = new Vector3d[JOpenVRLibrary.k_unMaxTrackedDeviceCount];

		for (int i = 0; i < poseMatrices.length; i++) {
			poseMatrices[i] = new Matrix4f();
			deviceVelocity[i] = new Vector3d(0, 0, 0);
		}

		OpenVRInput.hapticScheduler = new HapticScheduler();

		initialized = true;

		return true;
	}

	private static void initJOpenVR() {
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

			LOGGER.info("OpenVR System Initialized OK.");

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
		return modConfig.forceHardwareDetection > 0 ? HardwareType.values()[modConfig.forceHardwareDetection - 1] : detectedHardware;
	}

	static int getError() {
		return hmdErrorStore.getValue() != 0 ? hmdErrorStore.getValue() : hmdErrorStoreBuf.get(0);
	}

	public static boolean isError() {
		return hmdErrorStore.getValue() != 0 || hmdErrorStoreBuf.get(0) != 0;
	}

	@Override
	public void onInitializeClient() {
		if (initialized) {
			LOGGER.info("Creating MCOpenVR instance.");
			instance = new MCOpenVR();
		}
		AutoConfig.register(MCOpenVRConfig.class, JanksonConfigSerializer::new);
		modConfig = AutoConfig.getConfigHolder(MCOpenVRConfig.class).getConfig();
	}
}
