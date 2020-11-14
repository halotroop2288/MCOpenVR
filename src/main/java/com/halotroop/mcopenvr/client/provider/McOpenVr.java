package com.halotroop.mcopenvr.client.provider;

import com.halotroop.mcopenvr.client.McOpenVrConfig;
import com.halotroop.mcopenvr.client.api.Vec3History;
import com.halotroop.mcopenvr.client.control.Controller;
import com.halotroop.mcopenvr.client.control.HapticScheduler;
import com.halotroop.mcopenvr.client.control.TrackedController;
import com.halotroop.mcopenvr.client.control.VrInputAction;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openvr.*;

import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Variables that are assigned after Minecraft
 * launches should not be static. <br>
 * Get the instance to access these variables.
 *
 * @author halotroop2288
 */
public final class McOpenVr implements ClientModInitializer {
	public  static final Logger LOGGER = LogManager.getLogger("MCOpenVR");

	public  static String  initStatus = "";
	public  static boolean initSuccess = false;
	private static boolean initialized;
	private static boolean inputInitialized;

	private final  MinecraftClient mcClient;
	public  static McOpenVr instance;
	public  static McOpenVrConfig config;
	private static final Jankson JANKSON = new Jankson.Builder().build();

	private static IntBuffer hmdErrorStoreBuf;

	private static TrackedDevicePose.Buffer hmdTrackedDevicePoses;

	private static Matrix4f[] poseMatrices = new Matrix4f[VR.k_unMaxTrackedDeviceCount];
	private static Vec3d[] deviceVelocity = new Vec3d[VR.k_unMaxTrackedDeviceCount];

	private static final Matrix4f hmdPose = new Matrix4f();
	private static final Matrix4f hmdRotation = new Matrix4f();

	private static Matrix4f hmdPoseLeftEye = new Matrix4f();
	private static Matrix4f hmdPoseRightEye = new Matrix4f();

	private static float vsyncToPhotons;

	// Device history
	public static Vec3History hmdHistory = new Vec3History();
	public static Vec3History hmdPivotHistory = new Vec3History();
	public static Vec3History[] controllerHistory = new Vec3History[] {new Vec3History(), new Vec3History()};
	public static Vec3History[] controllerForwardHistory = new Vec3History[] {new Vec3History(), new Vec3History()};
	public static Vec3History[] controllerUpHistory = new Vec3History[] {new Vec3History(), new Vec3History()};

	/**
	 * Do not access this directly! Call the {@link #getHardwareType()} method instead!
	 */
	private static HardwareType detectedHardware = HardwareType.VIVE;

	// Texture IDs of the framebuffers for each eye
	private int leftEyeTextureId;
	private int rightEyeTextureId;

	private static final VRTextureBounds texBounds = VRTextureBounds.create();
	private static final Texture texType0 = Texture.create();
	private static final Texture texType1 = Texture.create();

	// Aiming
	private static Vec3d[] aimSource = new Vec3d[3];

	public static Vec3d offset = new Vec3d(0,0,0);

	// this being separate from the list below is very sus...
	public static TrackedController[] controllers = new TrackedController[2];

	private static Queue<VREvent> vrEvents = new LinkedList<>();

	public static boolean hudPopup = true;

	private static boolean headIsTracking;

	private static int moveModeSwitchCount = 0; // ???

	public  static boolean isWalkingAbout;
	private static boolean isFreeRotate;
	private static Controller walkaboutController;
	private static Controller freeRotateController;
	private static float walkaboutYawStart;
	private static float hmdForwardYaw;
	public  static boolean ignorePressesNextFrame = false;

	private static Map<String, VrInputAction> trackInputSamplers = new HashMap<>();
	private static Map<String, Boolean> axisUseTracker = new HashMap<>();

	private static InputPoseActionData poseData;
	private static InputOriginInfo originInfo;
	private static VRActiveActionSet.Buffer activeActionSetsReference;

	private static final HapticScheduler hapticScheduler = new HapticScheduler();

	public static boolean mrMovingCamActive;
	public static Vec3d mrControllerPos = Vec3d.ZERO;
	public static Vec3d mrControllerRotation = Vec3d.ZERO;

	private static int token;

	/**
	 * Sets up VR after Minecraft launches
	 */
	private McOpenVr() {
		mcClient = MinecraftClient.getInstance();
		// TODO post-initialization setup
	}

	private static boolean tried = false;

	/**
	 * Runs before Minecraft is started to initialize OpenVR.<br>
	 * @return true if initialization was successful
	 */
	public static boolean initialize() {
		if (initialized) return true;
		if (tried) return false;
		tried = true;

		if (!VR.VR_IsHmdPresent()) {
			initStatus = "mcopenvr.messages.nosteamvr";
			return false;
		}

		try {
			hmdErrorStoreBuf = BufferUtils.createIntBuffer(1);
			token = VR.VR_InitInternal(hmdErrorStoreBuf, VR.EVRApplicationType_VRApplication_Scene);
			OpenVR.create(token);
			for (int i = 0; i < poseMatrices.length; ++i) poseMatrices[i] = new Matrix4f();
		} catch (Exception e) {
			e.printStackTrace();
			initStatus = e.getLocalizedMessage();
			initSuccess = false;
			return false;
		}

		if (OpenVR.VRInput == null) {
			LOGGER.info("Controller input not available. Forcing seated mode.");
			config.seated = true;
		}

		Controller.RIGHT.init();
		Controller.LEFT.init();

		for (int i = 0; i < poseMatrices.length; ++i) {
			poseMatrices[i] = new Matrix4f();
			deviceVelocity[i] = Vec3d.ZERO;
		}

		initialized = true;
		// v initialize treadmills here v

		//

		return true;
	}

	@Override
	public void onInitializeClient() {
		AutoConfig.register(McOpenVrConfig.class, JanksonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(McOpenVrConfig.class).getConfig();

		if (initialized) {
			LOGGER.info("Creating MCOpenVR instance.");
			instance = new McOpenVr();
		}

		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> OpenVR.destroy());
	}

	/**
	 * @return the detected hardware, unless MCOpenVR config is forcing detection of other hardware.
	 */
	public static HardwareType getHardwareType() {
		return config.forcedHardwareDetection != HardwareType.NULL ? config.forcedHardwareDetection : detectedHardware;
	}

	public static Pointer pointerFromString(String s) {
		Pointer p = new Memory(s.getBytes(StandardCharsets.UTF_8).length + 1);
		p.setString(0, s, StandardCharsets.UTF_8.name());
		return p;
	}

	public static void triggerHapticPulse(Controller controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		if (config.seated || !inputInitialized) return;
		if (config.reverseHands) {
			switch (controller) {
				case RIGHT:
					controller = Controller.LEFT;
					break;
				case LEFT:
					controller = Controller.RIGHT;
					break;
				default:
					break;
			}
		}

		hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
	}

	public static void triggerHapticPulse(Controller controller, float durationSeconds, float frequency, float amplitude) {
		triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0);
	}

	@Deprecated
	public static void triggerHapticPulse(Controller controller, int strength) {
		if (strength < 1) return;
		// Through careful analysis of the haptics in the legacy API (read: I put the controller to
		// my ear, listened to the vibration, and reproduced the frequency in Audacity), I have determined
		// that the old haptics used 160Hz. So, these parameters will match the "feel" of the old haptics.
		triggerHapticPulse(controller, strength / 1000000f, 160, 1);
	}

	@Deprecated
	public static void triggerHapticPulse(int controller, int strength) {
		if (controller < 0 || controller >= Controller.values().length) return;
		triggerHapticPulse(Controller.values()[controller], strength);
	}

	public static String getInputError(int code) {
		switch (code) {
			case VR.EVRInputError_VRInputError_BufferTooSmall:
				return "BufferTooSmall";
			case VR.EVRInputError_VRInputError_InvalidBoneCount:
				return "InvalidBoneCount";
			case VR.EVRInputError_VRInputError_InvalidBoneIndex:
				return "InvalidBoneIndex";
			case VR.EVRInputError_VRInputError_InvalidCompressedData:
				return "InvalidCompressedData";
			case VR.EVRInputError_VRInputError_InvalidDevice:
				return "InvalidDevice";
			case VR.EVRInputError_VRInputError_InvalidHandle:
				return "InvalidHandle";
			case VR.EVRInputError_VRInputError_InvalidParam:
				return "InvalidParam";
			case VR.EVRInputError_VRInputError_InvalidSkeleton:
				return "InvalidSkeleton";
			case VR.EVRInputError_VRInputError_IPCError:
				return "IPCError";
			case VR.EVRInputError_VRInputError_MaxCapacityReached:
				return "MaxCapacityReached";
			case VR.EVRInputError_VRInputError_MismatchedActionManifest:
				return "MismatchedActionManifest";
			case VR.EVRInputError_VRInputError_MissingSkeletonData:
				return "MissingSkeletonData";
			case VR.EVRInputError_VRInputError_NameNotFound:
				return "NameNotFound";
			case VR.EVRInputError_VRInputError_NoActiveActionSet:
				return "NoActiveActionSet";
			case VR.EVRInputError_VRInputError_NoData:
				return "NoData";
			case VR.EVRInputError_VRInputError_None:
				return "wat";
			case VR.EVRInputError_VRInputError_NoSteam:
				return "NoSteam";
			case VR.EVRInputError_VRInputError_WrongType:
				return "WrongType";
			default:
				return "Unknown";
		}
	}
}
