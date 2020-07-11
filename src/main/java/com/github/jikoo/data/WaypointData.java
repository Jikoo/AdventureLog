package com.github.jikoo.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public class WaypointData extends YamlData {

	public WaypointData(File file) {
		super(file);
	}

	public List<String> getDefaultWaypoints() {
		return getStringList("defaults");
	}

	public void setDefaultWaypoints(Set<String> defaultWaypoints) {
		this.set("defaults", new ArrayList<>(defaultWaypoints));
	}

	public Collection<String> getWaypoints() {
		Object waypoints = this.get("waypoints");
		if (!(waypoints instanceof ConfigurationSection)) {
			return Collections.emptyList();
		}
		return ((ConfigurationSection) waypoints).getKeys(false);
	}

}
