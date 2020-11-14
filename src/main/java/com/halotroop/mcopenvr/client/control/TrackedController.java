package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.McOpenVr;

public class TrackedController {
	protected final Controller type;

	public TrackedController(Controller type) {
		this.type = type;
	}

	@Deprecated
	public int getDeviceIndex() {
		return Controller.values()[type.ordinal()].getDeviceIndex();
	}

	public Controller getType() {
		return type;
	}

	@Deprecated
	public boolean isTracking() {
		return Controller.values()[type.ordinal()].isTracking();
	}

	public void triggerHapticPulse(float durationSeconds, float frequency, float amplitude) {
		McOpenVr.triggerHapticPulse(this.type, durationSeconds, frequency, amplitude);
	}

	@Deprecated
	public void triggerHapticPulse(int duration) {
		McOpenVr.triggerHapticPulse(this.type, duration);
	}
}
