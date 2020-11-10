package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.McOpenVr;
import com.halotroop.mcopenvr.client.provider.OpenVrInput;

public class TrackedController {
	protected final ControllerType type;

	public TrackedController(ControllerType type) {
		this.type = type;
	}

	public int getDeviceIndex() {
		return McOpenVr.controllerDeviceIndex[type.ordinal()];
	}

	public ControllerType getType() {
		return type;
	}

	public boolean isTracking() {
		return OpenVrInput.isControllerTracking(type);
	}

	public void triggerHapticPulse(float durationSeconds, float frequency, float amplitude) {
		OpenVrInput.triggerHapticPulse(this.type, durationSeconds, frequency, amplitude);
	}
}
