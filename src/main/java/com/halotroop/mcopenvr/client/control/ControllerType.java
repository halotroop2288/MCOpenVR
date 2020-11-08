package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.api.MCOpenVR;

public enum ControllerType {
	RIGHT,
	LEFT;
	
	public TrackedController getController() {
		return MCOpenVR.controllers[this.ordinal()];
	}
}
