package com.halotroop.mcopenvr.client;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

/**
 * Usually, I'd use Cotton Config,
 * which makes this a little easier,
 * but Cotton is a little heavy for this.
 *
 * @author halotroop2288
 */
@Config(name = "MCOpenVR")
public class McOpenVrConfig implements ConfigData {
	@ConfigEntry.Category("control")
	@ConfigEntry.Gui.TransitiveObject
	public ControlSettings controlSettings = new ControlSettings();

	@ConfigEntry.Category("overrides")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 2)
	@Comment("Force detection of hardware: 0 = off, 1 = Vive, 2 = Oculus")
	public int forceHardwareDetection = 0;

	public static final class ControlSettings {
		@ConfigEntry.Category("control")
		@Comment("Disable controller input")
		public boolean disableControllerInput = false;
		@Comment("Swap primary and secondary controllers")
		public boolean reverseHands = false;
		public boolean reverseShootingEye = false;
		public float worldScale = 1.0f;
		public float worldRotation = 0f;
		@ConfigEntry.Gui.Excluded
		public float worldRotationCached;
		@ConfigEntry.Gui.Excluded
		public float worldRotationIncrement = 45f;
		public float xSensitivity = 1f;
		public float ySensitivity = 1f;
		public float keyholeX = 15;
		public double headToHmdLength = 0.10f;
		public float autoCalibration = -1;
		public float manualCalibration = -1;
		public boolean alwaysSimulateKeyboard = false;
		public BowMode bowMode = BowMode.ON;
		public int hrtfSelection = 0;
		public boolean firstRun = true;
		public int rightClickDelay = 6;

		private enum BowMode {
			ON, VANILLA, OFF
		}
	}
}
