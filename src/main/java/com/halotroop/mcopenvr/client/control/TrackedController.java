package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MinecraftOpenVR;

public class TrackedController {
	protected final ControllerType type;
	
	public TrackedController(ControllerType type) {
		this.type = type;
	}
	
	public int getDeviceIndex() {
		return MinecraftOpenVR.controllerDeviceIndex[type.ordinal()];
	}
	
	public ControllerType getType() {
		return type;
	}
	
	public boolean isTracking() {
		return MinecraftOpenVR.isControllerTracking(type);
	}
	
	public void triggerHapticPulse(float durationSeconds, float frequency, float amplitude) {
		MinecraftOpenVR.triggerHapticPulse(this.type, durationSeconds, frequency, amplitude);
	}
}
