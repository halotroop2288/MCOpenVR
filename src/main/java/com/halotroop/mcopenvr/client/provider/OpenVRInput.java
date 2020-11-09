package com.halotroop.mcopenvr.client.provider;

import com.halotroop.mcopenvr.client.control.ControllerType;
import com.halotroop.mcopenvr.client.control.HapticScheduler;
import com.halotroop.mcopenvr.client.control.TrackedController;
import jopenvr.JOpenVRLibrary;
import jopenvr.VR_IVRInput_FnTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenVR Input object provider
 * and VR controller manager
 *
 * @author halotroop2288
 */
public final class OpenVRInput {
	static final int RIGHT_CONTROLLER = 0;
	static final int LEFT_CONTROLLER = 1;
	private static final Logger LOGGER = LogManager.getLogger("OpenVR Input");
	private static final boolean[] controllerTracking = new boolean[3];
	public static TrackedController[] controllers = new TrackedController[2];
	protected static VR_IVRInput_FnTable instance;
	static HapticScheduler hapticScheduler;
	private static long leftHapticHandle;
	private static long rightHapticHandle;
	private static boolean inputInitialized;

	public static VR_IVRInput_FnTable get() {
		return instance;
	}

	static void init() {
		instance = new VR_IVRInput_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary
				.IVRInput_Version, MCOpenVR.hmdErrorStoreBuf));
		if (!MCOpenVR.isError()) {
			instance.setAutoSynch(false);
			instance.read();
			LOGGER.info("OpenVR Input initialized OK");
		} else {
			LOGGER.error("VRInput init failed: " + JOpenVRLibrary
					.VR_GetVRInitErrorAsEnglishDescription(MCOpenVR.getError()).getString(0));
			instance = null;
		}
	}

	public static void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		if (MCOpenVR.modConfig.disableControllerInput || !inputInitialized) return;
		if (MCOpenVR.modConfig.reverseHands) {
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
