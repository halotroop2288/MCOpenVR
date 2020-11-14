package com.halotroop.mcopenvr.client;

import com.halotroop.mcopenvr.client.provider.HardwareType;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

/**
 * Most of this is probably not going to stay here permanently.<br>
 * A lot of it probably applies more to VRCraft than MCOpenVR.
 *
 * @author halotroop2288
 */
@Config(name = "MCOpenVR")
public class McOpenVrConfig implements ConfigData {
	@ConfigEntry.Category("control")
	@ConfigEntry.Gui.TransitiveObject
	public ControlSettings controlSettings = new ControlSettings();

	/**
	 * I am pretty sure this belongs here.
	 */
	@ConfigEntry.Gui.RequiresRestart
	@ConfigEntry.Category("overrides")
	@Comment("Force detection of hardware [Choices: 'NULL' = off, 'VIVE', 'OCULUS', 'WINDOWS_MR']")
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	public HardwareType forcedHardwareDetection = HardwareType.NULL;

	/**
	 * Like, I actually don't think any of this needs to be here at all.
	 */
	public static final class ControlSettings {
		@ConfigEntry.Category("control")
		@Comment("Disable controller input")
		public boolean disableControllerInput = false;
		@Comment("Swap primary and secondary controllers")
		public boolean reverseHands = false;
		@ConfigEntry.Gui.Excluded
		public float worldRotationCached;
		public boolean firstRun = true;
	}

	public enum ForcedHardware {
		OFF, VIVE, OCULUS
	}
}
