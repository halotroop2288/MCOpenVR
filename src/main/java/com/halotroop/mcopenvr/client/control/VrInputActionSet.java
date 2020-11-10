package com.halotroop.mcopenvr.client.control;

public enum VrInputActionSet {
	INGAME("/actions/ingame", "vivecraft.actionset.ingame", "leftright", false),
	GUI("/actions/gui", "vivecraft.actionset.gui", "leftright", false),
	GLOBAL("/actions/global", "vivecraft.actionset.global", "leftright", false),
	MOD("/actions/mod", "vivecraft.actionset.mod", "leftright", false),
	CONTEXTUAL("/actions/contextual", "vivecraft.actionset.contextual", "single", false),
	KEYBOARD("/actions/keyboard", "vivecraft.actionset.keyboard", "single", true),
	MIXED_REALITY("/actions/mixedreality", "vivecraft.actionset.mixedReality", "single", true),
	TECHNICAL("/actions/technical", "vivecraft.actionset.technical", "leftright", true);

	public final String name;
	public final String localizedName;
	public final String usage;
	public final boolean advanced;

	VrInputActionSet(String name, String localizedName, String usage, boolean advanced) {
		this.name = name;
		this.localizedName = localizedName;
		this.usage = usage;
		this.advanced = advanced;
	}
}

