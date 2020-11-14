package com.halotroop.mcopenvr.client.control;

import com.halotroop.mcopenvr.client.provider.McOpenVr;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.openvr.VRInput;

import java.nio.LongBuffer;

/**
 * I'm pretty sure we can technically have up to 64 controllers.<br>
 * I'll look into expanding this system later.
 */
public enum Controller {
	RIGHT("/actions/global/out/righthaptic"),
	LEFT("/actions/global/out/lefthaptic");
	// THIRD();
	private boolean tracking;
	private Matrix4f pose;
	private Matrix4f rotation;
	private Matrix4f handRotation;

	private int deviceIndex;

	private final long hapticHandle;
	private TrackedController controller;

	Controller(String hapticName) {
		LongBuffer longRef = BufferUtils.createLongBuffer(1);
		int errorCode = VRInput.VRInput_GetActionHandle(hapticName, longRef);
		if (errorCode != 0) throw new RuntimeException("Error getting action handle for '" + hapticName + "': "
				+ McOpenVr.getInputError(errorCode));
		this.hapticHandle = longRef.get(0);
	}

	public void init() {
		this.controller = new TrackedController(this);
	}

	public boolean isTracking() {
		return tracking;
	}

	public Matrix4f getPose() {
		return pose;
	}

	public Matrix4f getRotation() {
		return rotation;
	}

	public Matrix4f getHandRotation() {
		return handRotation;
	}

	public int getDeviceIndex() {
		return deviceIndex;
	}

	public TrackedController getController() {
		return controller;
	}

	public long getHapticHandle() {
		return this.hapticHandle;
	}
}
