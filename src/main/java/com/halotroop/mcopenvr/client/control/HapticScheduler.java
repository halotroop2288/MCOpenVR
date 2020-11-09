package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MCOpenVR;
import com.halotroop.mcopenvr.client.provider.OpenVRInput;
import jopenvr.JOpenVRLibrary;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HapticScheduler {
	private static final Logger LOGGER = MCOpenVR.LOGGER;

	private final ScheduledExecutorService executor;

	public HapticScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
		int error = OpenVRInput.get().TriggerHapticVibrationAction.apply(OpenVRInput.getHapticHandle(controller),
				0, durationSeconds, frequency, amplitude, JOpenVRLibrary.k_ulInvalidInputValueHandle);
		if (error != 0)
			LOGGER.info("Error triggering haptic: " + OpenVRInput.getInputError(error));
	}

	public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		executor.schedule(() -> triggerHapticPulse(controller, durationSeconds, frequency, amplitude), (long) (delaySeconds * 1000000), TimeUnit.MICROSECONDS);
	}
}
