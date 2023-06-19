package com.github.jikoo.data;

import com.github.jikoo.event.WaypointUnlockEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class UserData extends YamlData {

	private final Map<String, UserWaypoint> waypoints;
	private final Supplier<ServerData> serverDataSupplier;
	private final UUID uuid;

	public UserData(Plugin plugin, UUID uuid, Supplier<ServerData> supplier) {
		super(plugin, new File(new File(plugin.getDataFolder(), "playerdata"), uuid.toString() + ".yml"));
		this.waypoints = new HashMap<>();
		this.serverDataSupplier = supplier;
		this.uuid = uuid;

		// Migrate old unlocked waypoints
		if (raw().isList("waypoints")) {
			this.setUnlocked(getStringList("waypoints"));
			this.set("waypoints", null);
		}

		ConfigurationSection waypointSection = raw().getConfigurationSection("waypoints");
		if (waypointSection == null) {
			return;
		}
		for (String key : waypointSection.getKeys(false)) {
			UserWaypoint waypoint = new UserWaypoint(this, key);
			if (waypoint.isInvalid()) {
				waypoint.delete();
				continue;
			}
			this.waypoints.put(key, waypoint);
		}
	}

	public @NotNull Collection<UserWaypoint> getUserWaypoints() {
		return this.waypoints.values().stream().sorted(UserWaypoint.COMPARATOR).toList();
	}

	public @NotNull UserWaypoint createWaypoint(@NotNull Location location) {
		int nextAvailable = 1;
		while (raw().isSet("waypoints." + nextAvailable)) {
			++nextAvailable;
		}
		String name = String.valueOf(nextAvailable);
		UserWaypoint waypoint = new UserWaypoint(this, name);
		waypoint.setLocation(location);
		this.waypoints.put(name, waypoint);
		return waypoint;
	}

	public @NotNull Collection<ServerWaypoint> getUnlockedWaypoints() {
		ServerData serverData = this.serverDataSupplier.get();
		return this.getUnlocked().stream()
				.map(serverData::getWaypoint)
				.filter(Objects::nonNull)
				.sorted(ServerWaypoint.COMPARATOR)
				.toList();
	}

	public @NotNull Collection<ServerWaypoint> getAvailableWaypoints() {
		ServerData serverData = this.serverDataSupplier.get();
		return Stream.concat(this.getUnlocked().stream(), serverData.getDefaultWaypointNames().stream())
				.distinct()
				.map(serverData::getWaypoint)
				.filter(Objects::nonNull)
				.sorted(ServerWaypoint.COMPARATOR)
				.toList();
	}

	void notifyDelete(@NotNull UserWaypoint waypoint) {
		this.waypoints.remove(waypoint.getId());
	}

	public boolean unlockWaypoint(@NotNull String waypointName) {
		List<String> unlocked = this.getUnlocked();
		if (unlocked.contains(waypointName)) {
			return false;
		}
		ServerWaypoint waypoint = serverDataSupplier.get().getWaypoint(waypointName);
		if (waypoint == null || waypoint.isDefault()) {
			return false;
		}

		WaypointUnlockEvent event = new WaypointUnlockEvent(uuid, waypoint);
		event.fire();
		if (event.isCancelled()) {
			return false;
		}

		unlocked.add(waypointName);
		setUnlocked(unlocked);
		return true;
	}

	public boolean lockWaypoint(@NotNull String waypointName) {
		List<String> unlocked = getUnlocked();
		if (!unlocked.remove(waypointName)) {
			return false;
		}
		setUnlocked(unlocked);
		return true;
	}

	public List<String> getUnlocked() {
		return this.getStringList("server_waypoints");
	}

	public void setUnlocked(List<String> unlocked) {
		this.set("server_waypoints", unlocked);
	}

}
