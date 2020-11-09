package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MCOpenVR;
import com.halotroop.mcopenvr.client.provider.OpenVRInput;

public class TrackedController {
	protected final ControllerType type;

	public TrackedController(ControllerType type) {
		this.type = type;
	}

	public int getDeviceIndex() {
		return MCOpenVR.controllerDeviceIndex[type.ordinal()];
	}

	public ControllerType getType() {
		return type;
	}

	public boolean isTracking() {
		return OpenVRInput.isControllerTracking(type);
	}

	public void triggerHapticPulse(float durationSeconds, float frequency, float amplitude) {
		OpenVRInput.triggerHapticPulse(this.type, durationSeconds, frequency, amplitude);
	}
}
