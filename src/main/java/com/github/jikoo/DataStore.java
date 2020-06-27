package com.github.jikoo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataStore {

	private static final String WAYPOINT_LOCATION = "waypoints.%s.location";
	private static final String WAYPOINT_ICON = "waypoints.%s.icon";
	private static final String WAYPOINT_PRIORITY = "waypoints.%s.priority";
	private static final String WAYPOINT_RANGE_SQUARED = "waypoints.%s.range_squared";

	public enum Result {
		SUCCESS,
		FAILURE,
		UNNECESSARY
	}

	private final AdventureLogPlugin plugin;
	private final LoadingCache<UUID, YamlConfiguration> playerCache;
	private Map<String, Waypoint> loadedWaypoints;
	private Set<String> defaultWaypoints;

	DataStore(AdventureLogPlugin plugin) {
		this.plugin = plugin;
		this.playerCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
				.removalListener((RemovalListener<UUID, YamlConfiguration>) removalNotification -> {
					try {
						removalNotification.getValue().save(getUserFile(removalNotification.getKey()));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}).build(CacheLoader.from(uuid -> {
					if (uuid == null) {
						throw new NullPointerException("UUID cannot be null!");
					}
					return YamlConfiguration.loadConfiguration(getUserFile(uuid));
				}));
		reloadWaypoints();
	}

	private File getUserFile(UUID uuid) {
		return new File(plugin.getDataFolder().getPath() + File.separator + "playerdata", uuid.toString() + ".yml");
	}

	private File getWaypointsFile() {
		return new File(plugin.getDataFolder(), "waypoints.yml");
	}

	private void reloadWaypoints() {
		loadedWaypoints = new HashMap<>();
		defaultWaypoints = new HashSet<>();
		YamlConfiguration waypointStorage = YamlConfiguration.loadConfiguration(getWaypointsFile());
		if (!waypointStorage.isConfigurationSection("waypoints")) {
			return;
		}
		ConfigurationSection waypointSection = waypointStorage.getConfigurationSection("waypoints");
		if (waypointSection == null) {
			return;
		}
		loadedWaypoints = waypointSection.getKeys(false).stream().map(waypointName -> loadWaypoint(waypointStorage, waypointName))
				.filter(Objects::nonNull).distinct()
				.collect(Collectors.toMap(Waypoint::getName, Function.identity(), (oldWaypoint, newWaypoint) -> newWaypoint, () -> loadedWaypoints));
		defaultWaypoints.addAll(waypointStorage.getStringList("defaults"));
	}

	private @Nullable Waypoint loadWaypoint(YamlConfiguration storage, String waypointName) {
		Location location = storage.getSerializable(String.format(WAYPOINT_LOCATION, waypointName), Location.class);
		ItemStack icon = storage.getSerializable(String.format(WAYPOINT_ICON, waypointName), ItemStack.class);
		if (icon == null || icon.getType() == Material.AIR || location == null) {
			return null;
		}
		return new Waypoint.Builder(waypointName).setIcon(icon).setLocation(location)
				.setPriority(storage.getInt(String.format(WAYPOINT_PRIORITY, waypointName), 0))
				.setRangeSquared(storage.getInt(String.format(WAYPOINT_RANGE_SQUARED, waypointName), 900)).build();
	}

	Result save() {
		this.playerCache.invalidateAll();
		this.playerCache.cleanUp();
		YamlConfiguration storage = new YamlConfiguration();
		for (Waypoint waypoint : loadedWaypoints.values()) {
			storage.set(String.format(WAYPOINT_LOCATION, waypoint.getName()), waypoint.getLocation());
			storage.set(String.format(WAYPOINT_ICON, waypoint.getName()), waypoint.getIcon());
			storage.set(String.format(WAYPOINT_PRIORITY, waypoint.getName()), waypoint.getPriority());
			storage.set(String.format(WAYPOINT_RANGE_SQUARED, waypoint.getName()), waypoint.getRangeSquared());
		}
		try {
			storage.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * Add a Waypoint.
	 *
	 * @param waypoint the Waypoint to add
	 * @return the Result of the operation
	 */
	public Result addWaypoint(@NotNull Waypoint waypoint) {
		Waypoint existing = getWaypoint(waypoint.getName());
		if (waypoint.equals(existing)) {
			return Result.UNNECESSARY;
		}
		this.loadedWaypoints.put(waypoint.getName(), waypoint);
		return save(); // TODO better saving
	}

	/**
	 * Remove a Waypoint.
	 *
	 * @param waypointName the name of the Waypoint to remove
	 * @return the Result of the operation
	 */
	public Result removeWaypoint(@NotNull String waypointName) {
		Result removeDefault = removeDefault(waypointName);
		if (this.loadedWaypoints.remove(waypointName) == null) {
			return Result.SUCCESS;
			// TODO better saving
		}
		return removeDefault;
	}

	/**
	 * Get a Waypoint by name.
	 *
	 * @param waypointName the name of the Waypoint
	 * @return the Waypoint if it exists
	 */
	public @Nullable Waypoint getWaypoint(@NotNull String waypointName) {
		return loadedWaypoints.get(waypointName);
	}

	/**
	 * Add a default Waypoint.
	 *
	 * @param waypointName the name of the Waypoint
	 * @return the Result of the operation
	 */
	public Result addDefault(@NotNull String waypointName) {
		if (getWaypoint(waypointName) == null) {
			return Result.FAILURE;
		}
		if (defaultWaypoints.add(waypointName)) {
			return Result.SUCCESS;
			// TODO better saving
		}
		return Result.UNNECESSARY;
	}

	/**
	 * Remove a default Waypoint.
	 *
	 * @param waypointName the name of the Waypoint to remove
	 * @return the Result of the operation
	 */
	public Result removeDefault(@NotNull String waypointName) {
		if (this.defaultWaypoints.remove(waypointName)) {
			// TODO better saving
			return Result.SUCCESS;
		}
		return Result.UNNECESSARY;
	}

	/**
	 * Check if a waypoint is discovered by default.
	 *
	 * @param waypoint the waypoint
	 * @return true if the waypoint is discovered by default
	 */
	public boolean isDefault(@NotNull Waypoint waypoint) {
		return this.defaultWaypoints.contains(waypoint.getName());
	}

	/**
	 * Get a Collection of all Waypoints that are unlocked for everyone.
	 *
	 * @return the default Waypoints
	 */
	@NotNull
	public Collection<Waypoint> getDefaultWaypoints() {
		return defaultWaypoints.stream().map(this::getWaypoint).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
	}

	/**
	 * Get a Collection of all Waypoints.
	 *
	 * @return all Waypoints
	 */
	@NotNull
	public Collection<Waypoint> getWaypoints() {
		return loadedWaypoints.values().stream().sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
	}

	/**
	 * Unlock a Waypoint for a Player.
	 *
	 * @param uuid the UUID of the Player for whom to unlock a Waypoint
	 * @param waypointName the name of the Waypoint
	 * @return the Result of the operation
	 */
	public Result unlockWaypoint(@NotNull UUID uuid, @NotNull String waypointName) {
		if (getWaypoint(waypointName) == null) {
			return Result.FAILURE;
		}
		try {
			YamlConfiguration playerData = this.playerCache.get(uuid);
			List<String> unlockedWaypoints = playerData.getStringList("waypoints");
			if (unlockedWaypoints.contains(waypointName)) {
				return Result.UNNECESSARY;
			}
			unlockedWaypoints.add(waypointName);
			playerData.set("waypoints", unlockedWaypoints);
			return Result.SUCCESS;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
	}

	/**
	 * Lock a Waypoint for a Player.
	 *
	 * @param uuid the UUID of the Player for whom to lock a Waypoint
	 * @param waypointName the name of the Waypoint
	 * @return the Result of the operation
	 */
	public Result lockWaypoint(@NotNull UUID uuid, @NotNull String waypointName) {
		try {
			YamlConfiguration playerData = this.playerCache.get(uuid);
			List<String> unlockedWaypoints = playerData.getStringList("waypoints");
			if (!unlockedWaypoints.remove(waypointName)) {
				return Result.UNNECESSARY;
			}
			playerData.set("waypoints", unlockedWaypoints);
			return Result.SUCCESS;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
	}

	/**
	 * Get all Waypoints unlocked for a Player.
	 * <p>
	 * Includes default waypoints!
	 *
	 * @param uuid the UUID of the Player
	 * @return the Waypoints available
	 */
	@NotNull
	public Collection<Waypoint> getWaypoints(UUID uuid) {
		YamlConfiguration playerData;
		try {
			playerData = this.playerCache.get(uuid);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		if (!playerData.isList("waypoints")) {
			return getDefaultWaypoints();
		}

		return Stream.concat(getDefaultWaypoints().stream(),
				playerData.getStringList("waypoints").stream().map(this::getWaypoint).filter(Objects::nonNull))
				.distinct().sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
	}

}
