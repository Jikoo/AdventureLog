package com.github.jikoo.data;

import java.io.File;
import java.util.List;

public class UserData extends YamlData {

	public UserData(File file) {
		super(file);
	}

	public List<String> getUnlocked() {
		return this.getStringList("waypoints");
	}

	public void setUnlocked(List<String> unlocked) {
		this.set("waypoints", unlocked);
	}

}
