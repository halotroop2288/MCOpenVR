package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.OpenVRInput;

public enum ControllerType {
	RIGHT,
	LEFT;

	public TrackedController getController() {
		return OpenVRInput.controllers[this.ordinal()];
	}
}
