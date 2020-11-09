package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.MinecraftOpenVR;

public enum ControllerType {
	RIGHT,
	LEFT;
	
	public TrackedController getController() {
		return MinecraftOpenVR.controllers[this.ordinal()];
	}
}
