package com.github.jikoo.data;

import com.github.jikoo.planarwrappers.scheduler.DistributedTask;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerData extends YamlData {

	private final @NotNull Map<@NotNull String, @NotNull ServerWaypoint> loadedWaypoints;
	private final @NotNull DistributedTask<@NotNull String> discoverableWaypoints;
	private final @NotNull Set<@NotNull String> defaultWaypoints;

	public ServerData(@NotNull Plugin plugin, Consumer<Collection<String>> waypointDiscovery) {
		super(plugin, new File(plugin.getDataFolder(), "waypoints.yml"));
		this.discoverableWaypoints = new DistributedTask<>(15, TimeUnit.SECONDS, waypointDiscovery);
		this.loadedWaypoints = new HashMap<>();
		this.defaultWaypoints = new HashSet<>();

		// Migrate old default data
		if (raw().isList("defaults")) {
			List<String> defaults = getStringList("defaults");
			for (String waypointName : defaults) {
				ConfigurationSection waypointSection = raw().getConfigurationSection("waypoints." + waypointName);
				if (waypointSection != null) {
					waypointSection.set("default", true);
				}
			}
			set("defaults", null);
		}

		for (String waypointName : getWaypointNames()) {
			ServerWaypoint loaded = new ServerWaypoint(this, waypointName);
			// Ensure waypoint has location
			if (loaded.isInvalid()) {
				loaded.delete();
				return;
			}
			loadedWaypoints.put(waypointName, loaded);
			this.notifyDefaultUpdate(loaded);
		}
	}

	public void scheduleDiscovery(@NotNull Plugin plugin) {
		this.discoverableWaypoints.schedule(plugin);
	}

	private @NotNull Collection<String> getWaypointNames() {
		Object waypoints = this.get("waypoints");
		if (!(waypoints instanceof ConfigurationSection)) {
			return List.of();
		}
		return ((ConfigurationSection) waypoints).getKeys(false);
	}

	public @NotNull ServerWaypoint addWaypoint(@NotNull String name, @NotNull ItemStack icon, @NotNull Location location) {
		ServerWaypoint existing = this.getWaypoint(name);
		if (existing != null) {
			existing.setIcon(icon);
			existing.setLocation(location);
			return existing;
		}
		ServerWaypoint waypoint = new ServerWaypoint(this, name, icon, location);
		this.loadedWaypoints.put(name, waypoint);
		notifyDefaultUpdate(waypoint);
		return waypoint;
	}

	public @Nullable ServerWaypoint getWaypoint(@NotNull String name) {
		return this.loadedWaypoints.get(name);
	}

	public @NotNull Collection<@NotNull ServerWaypoint> getWaypoints() {
		return this.loadedWaypoints.values().stream().sorted(ServerWaypoint.COMPARATOR).toList();
	}

	@NotNull Collection<@NotNull String> getDefaultWaypointNames() {
		return defaultWaypoints;
	}

	public @NotNull Collection<ServerWaypoint> getDefaultWaypoints() {
		return this.defaultWaypoints.stream().map(this::getWaypoint).filter(Objects::nonNull)
				.sorted(ServerWaypoint.COMPARATOR).toList();
	}

	void notifyRangeUpdate(@NotNull ServerWaypoint waypoint) {
		if (waypoint.getRangeSquared() < 1) {
			this.discoverableWaypoints.remove(waypoint.getId());
		}
		this.discoverableWaypoints.add(waypoint.getId());
	}

	void notifyDefaultUpdate(@NotNull ServerWaypoint waypoint) {
		if (waypoint.isDefault()) {
			this.discoverableWaypoints.remove(waypoint.getId());
			this.defaultWaypoints.add(waypoint.getId());
			return;
		}
		if (waypoint.getRangeSquared() > 0) {
			this.discoverableWaypoints.add(waypoint.getId());
		}
		this.defaultWaypoints.remove(waypoint.getId());
	}

	void notifyDelete(@NotNull ServerWaypoint waypoint) {
		this.loadedWaypoints.remove(waypoint.getId());
		this.defaultWaypoints.remove(waypoint.getId());
		this.discoverableWaypoints.remove(waypoint.getId());
	}

}
