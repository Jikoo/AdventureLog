package com.github.jikoo.data;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.lucko.helper.bucket.Bucket;
import me.lucko.helper.bucket.BucketPartition;
import me.lucko.helper.bucket.factory.BucketFactory;
import me.lucko.helper.bucket.partitioning.PartitioningStrategies;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerData extends YamlData {

	private final Map<String, ServerWaypoint> loadedWaypoints;
	private final Bucket<ServerWaypoint> discoverableWaypoints; // TODO -> name?
	private final Set<String> defaultWaypoints;

	public ServerData(@NotNull Plugin plugin) {
		super(plugin, new File(plugin.getDataFolder(), "waypoints.yml"));
		this.discoverableWaypoints = BucketFactory.newHashSetBucket(60, PartitioningStrategies.lowestSize());
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
			if (loaded.getRangeSquared() > 0) {
				discoverableWaypoints.add(loaded);
			}
		}
	}

	private @NotNull Collection<String> getWaypointNames() {
		Object waypoints = this.get("waypoints");
		if (!(waypoints instanceof ConfigurationSection)) {
			return Collections.emptyList();
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
		return new ServerWaypoint(this, name, icon, location);
	}

	public @Nullable ServerWaypoint getWaypoint(@NotNull String name) {
		return this.loadedWaypoints.get(name);
	}

	public @NotNull Collection<ServerWaypoint> getWaypoints() {
		return this.loadedWaypoints.values().stream().sorted(ServerWaypoint.COMPARATOR).collect(Collectors.toList());
	}

	@NotNull Collection<String> getDefaultWaypointNames() {
		return defaultWaypoints;
	}

	public @NotNull Collection<ServerWaypoint> getDefaultWaypoints() {
		return this.defaultWaypoints.stream().map(this::getWaypoint).filter(Objects::nonNull)
				.sorted(ServerWaypoint.COMPARATOR).collect(Collectors.toList());
	}

	public BucketPartition<ServerWaypoint> next() {
		return this.discoverableWaypoints.asCycle().next();
	}

	void notifyRangeUpdate(@NotNull ServerWaypoint waypoint) {
		if (waypoint.getRangeSquared() < 1) {
			this.discoverableWaypoints.remove(waypoint);
		}
		if (this.defaultWaypoints.contains(waypoint.getName())) {
			return;
		}
		this.discoverableWaypoints.add(waypoint);
	}

	void notifyDefaultUpdate(@NotNull ServerWaypoint waypoint) {
		if (waypoint.isDefault()) {
			this.discoverableWaypoints.remove(waypoint);
			this.defaultWaypoints.add(waypoint.getName());
			return;
		}
		if (waypoint.getRangeSquared() > 0) {
			this.discoverableWaypoints.add(waypoint);
		}
		this.defaultWaypoints.remove(waypoint.getName());
	}

	void notifyDelete(@NotNull ServerWaypoint waypoint) {
		this.loadedWaypoints.remove(waypoint.getName());
		this.defaultWaypoints.remove(waypoint.getName());
		this.discoverableWaypoints.remove(waypoint);
	}

}
