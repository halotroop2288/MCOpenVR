package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MinecraftOpenVR;
import jopenvr.JOpenVRLibrary;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HapticScheduler {
	private final ScheduledExecutorService executor;
	
	public HapticScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
		int error = MinecraftOpenVR.vrInput.TriggerHapticVibrationAction.apply(MinecraftOpenVR.getHapticHandle(controller), 0, durationSeconds, frequency, amplitude, JOpenVRLibrary.k_ulInvalidInputValueHandle);
		if (error != 0)
			System.out.println("Error triggering haptic: " + MinecraftOpenVR.getInputError(error));
	}
	
	public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
		executor.schedule(() -> triggerHapticPulse(controller, durationSeconds, frequency, amplitude), (long) (delaySeconds * 1000000), TimeUnit.MICROSECONDS);
	}
}
