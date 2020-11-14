package com.halotroop.mcopenvr.client;

import com.halotroop.mcopenvr.client.provider.HardwareType;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "MCOpenVR")
public class McOpenVrConfig implements ConfigData {
	@ConfigEntry.Gui.RequiresRestart
	@ConfigEntry.Category("overrides")
	@Comment("Force detection of hardware [Choices: 'NULL' = off, 'VIVE', 'OCULUS', 'WINDOWS_MR']")
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	public HardwareType forcedHardwareDetection = HardwareType.NULL;
	public boolean seated;
	public boolean reverseHands;
}
