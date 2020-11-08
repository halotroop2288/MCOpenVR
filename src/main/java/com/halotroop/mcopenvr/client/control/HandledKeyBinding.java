package com.halotroop.mcopenvr.client.control;


import net.minecraft.client.options.KeyBinding;

import java.util.Arrays;

public class HandledKeyBinding extends KeyBinding {
	private boolean[] pressed = new boolean[ControllerType.values().length];
	private int[] pressTime = new int[ControllerType.values().length];
	
	public HandledKeyBinding(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}
	
	@Override
	public boolean isPressed() {
		return Arrays.stream(ControllerType.values()).map(this::isPressed).reduce(false, (a, b) -> a || b);
	}
	
	@Override
	public boolean wasPressed() {
		return Arrays.stream(ControllerType.values()).map(this::isKeyDown).reduce(false, (a, b) -> a || b);
	}
	
	public boolean isPressed(ControllerType hand) {
		if (this.pressTime[hand.ordinal()] > 0) {
			--this.pressTime[hand.ordinal()];
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isKeyDown(ControllerType hand) {
		return this.pressed[hand.ordinal()];
	}
	
	public void pressKey(ControllerType hand) {
		this.pressed[hand.ordinal()] = true;
		++this.pressTime[hand.ordinal()];
	}
	
	public void unpressKey(ControllerType hand) {
		this.pressTime[hand.ordinal()] = 0;
		this.pressed[hand.ordinal()] = false;
	}
	
	public boolean isPriorityOnController(ControllerType type) {
		return true;
	}
}
