package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.OpenVrInput;

public enum ControllerType {
	RIGHT,
	LEFT;

	public TrackedController getController() {
		return OpenVrInput.controllers[this.ordinal()];
	}
}
