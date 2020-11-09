package com.halotroop.mcopenvr.client.provider;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum HardwareType {
	VIVE(true, false, "HTC"),
	OCULUS(false, true, "Oculus"),
	WINDOWSMR(true, true, "WindowsMR");
	
	private static final Map<String, HardwareType> map = new HashMap<>();

	static {
		for (HardwareType hw : values()) {
			for (String str : hw.manufacturers) {
				assert !map.containsKey(str) : "Duplicate manufacturer: " + str;
				map.put(str, hw);
			}
		}
	}

	public final List<String> manufacturers;
	public final boolean hasTouchpad;
	public final boolean hasStick;
	
	HardwareType(boolean hasTouchpad, boolean hasStick, String... manufacturers) {
		this.hasTouchpad = hasTouchpad;
		this.hasStick = hasStick;
		this.manufacturers = ImmutableList.copyOf(manufacturers);
	}
	
	public static HardwareType fromManufacturer(String name) {
		return map.getOrDefault(name, VIVE);
	}
}
