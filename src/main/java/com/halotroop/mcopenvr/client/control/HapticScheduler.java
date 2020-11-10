package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.McOpenVr;
import com.halotroop.mcopenvr.client.provider.OpenVrInput;
import jopenvr.JOpenVRLibrary;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HapticScheduler {
	private static final Logger LOGGER = McOpenVr.LOGGER;

	private final ScheduledExecutorService executor;

	public HapticScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
		int error = OpenVrInput.get().TriggerHapticVibrationAction.apply(OpenVrInput.getHapticHandle(controller),
				0, durationSeconds, frequency, amplitude, JOpenVRLibrary.k_ulInvalidInputValueHandle);
		if (error != 0)
			LOGGER.info("Error triggering haptic: " + OpenVrInput.getInputError(error));
	}

	public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		executor.schedule(() -> triggerHapticPulse(controller, durationSeconds, frequency, amplitude), (long) (delaySeconds * 1000000), TimeUnit.MICROSECONDS);
	}
}
