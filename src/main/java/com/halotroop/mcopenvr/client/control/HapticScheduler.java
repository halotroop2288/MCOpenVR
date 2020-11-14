package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.McOpenVr;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRInput;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HapticScheduler {
	private static final Logger LOGGER = McOpenVr.LOGGER;
	private final ScheduledExecutorService executor;
	private static long leftHapticHandle;
	private static long rightHapticHandle;

	public HapticScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	private void triggerHapticPulse(Controller controller, float durationSeconds, float frequency, float amplitude) {
		if (OpenVR.VRInput == null) return;
		int error = VRInput.VRInput_TriggerHapticVibrationAction(controller.getHapticHandle(), 0, durationSeconds,
				frequency, amplitude, VR.k_ulInvalidInputValueHandle);
		if (error != 0) LOGGER.error("Error triggering haptic: " + McOpenVr.getInputError(error));
	}

	public void queueHapticPulse(Controller controller, float durationSeconds, float frequency, float amplitude,
	                             float delaySeconds) {
		executor.schedule(() -> triggerHapticPulse(controller, durationSeconds, frequency, amplitude),
				(long) (delaySeconds * 1000000), TimeUnit.MICROSECONDS);
	}
}
