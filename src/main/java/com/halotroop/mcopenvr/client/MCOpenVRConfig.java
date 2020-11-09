package com.halotroop.mcopenvr.client;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.math.MathHelper;

/**
 * Usually, I'd use Cotton Config,
 * which makes this a little easier,
 * but Cotton is a little heavy for this.
 *
 * @author halotroop2288
 */
@Config(name = "MCOpenVR")
public class MCOpenVRConfig implements ConfigData {
	@ConfigEntry.Category("control")
	@Comment("Disable controller input")
	public boolean disableControllerInput = false;
	@ConfigEntry.Category("control")
	@Comment("Swap primary and secondary controllers")
	public boolean reverseHands = false;

	@ConfigEntry.Category("overrides")
	@Comment("Force detection of hardware: 0 = off, 1 = Vive, 2 = Oculus")
	public int forceHardwareDetection = 0;

	@Override
	public void validatePostLoad() {
		forceHardwareDetection = MathHelper.clamp(forceHardwareDetection, 0, 2);
	}
}
