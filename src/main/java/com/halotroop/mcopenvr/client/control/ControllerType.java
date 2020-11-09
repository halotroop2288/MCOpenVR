package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MCOpenVR;

public enum ControllerType {
	RIGHT,
	LEFT;
	
	public TrackedController getController() {
		return MCOpenVR.controllers[this.ordinal()];
	}
}
