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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

	public enum Result {
		SUCCESS,
		FAILURE,
		UNNECESSARY
	}

	private final AdventureLogPlugin plugin;
	private final LoadingCache<UUID, YamlConfiguration> playerCache;
	private YamlConfiguration waypoints;

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
		this.waypoints = YamlConfiguration.loadConfiguration(getWaypointsFile());
	}

	void save() {
		try {
			this.waypoints.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.playerCache.invalidateAll();
		this.playerCache.cleanUp();
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
		this.waypoints.set(String.format(WAYPOINT_LOCATION, waypoint.getName()), waypoint.getLocation());
		this.waypoints.set(String.format(WAYPOINT_ICON, waypoint.getName()), waypoint.getIcon());
		this.waypoints.set(String.format(WAYPOINT_PRIORITY, waypoint.getName()), waypoint.getPriority());
		try {
			this.waypoints.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * Remove a Waypoint.
	 *
	 * @param waypointName the name of the Waypoint to remove
	 * @return the Result of the operation
	 */
	public Result removeWaypoint(@NotNull String waypointName) {
		this.waypoints.set(String.format(WAYPOINT_LOCATION, waypointName), null);
		this.waypoints.set(String.format(WAYPOINT_ICON, waypointName), null);
		this.waypoints.set(String.format(WAYPOINT_PRIORITY, waypointName), null);
		try {
			this.waypoints.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * Get a Waypoint by name.
	 *
	 * @param waypointName the name of the Waypoint
	 * @return the Waypoint if it exists
	 */
	public @Nullable Waypoint getWaypoint(@NotNull String waypointName) {
		Location location = this.waypoints.getSerializable(String.format(WAYPOINT_LOCATION, waypointName), Location.class);
		ItemStack icon = this.waypoints.getSerializable(String.format(WAYPOINT_ICON, waypointName), ItemStack.class);
		if (icon == null || icon.getType() == Material.AIR || location == null) {
			return null;
		}
		return new Waypoint.Builder(waypointName).setIcon(icon).setLocation(location)
				.setPriority(waypoints.getInt(String.format(WAYPOINT_PRIORITY, waypointName), 0)).build();
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
		List<String> defaults = waypoints.getStringList("defaults");
		if (defaults.contains(waypointName)) {
			return Result.UNNECESSARY;
		}
		defaults.add(waypointName);
		waypoints.set("defaults", defaults);
		try {
			this.waypoints.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * Remove a default Waypoint.
	 *
	 * @param waypointName the name of the Waypoint to remove
	 * @return the Result of the operation
	 */
	public Result removeDefault(@NotNull String waypointName) {
		List<String> defaults = waypoints.getStringList("defaults");
		if (!defaults.remove(waypointName)) {
			return Result.UNNECESSARY;
		}
		waypoints.set("defaults", defaults);
		try {
			this.waypoints.save(getWaypointsFile());
		} catch (IOException e) {
			e.printStackTrace();
			return Result.FAILURE;
		}
		return Result.SUCCESS;
	}

	/**
	 * Get a Collection of all Waypoints that are unlocked for everyone.
	 *
	 * @return the default Waypoints
	 */
	@NotNull
	public Collection<Waypoint> getDefaultWaypoints() {
		return waypoints.getStringList("defaults").stream().map(this::getWaypoint).filter(Objects::nonNull)
				.distinct().sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
	}

	/**
	 * Get a Collection of all Waypoints.
	 *
	 * @return all Waypoints
	 */
	@NotNull
	public Collection<Waypoint> getWaypoints() {
		if (!this.waypoints.isConfigurationSection("waypoints")) {
			return Collections.emptyList();
		}
		ConfigurationSection waypointSection = this.waypoints.getConfigurationSection("waypoints");
		if (waypointSection == null) {
			return Collections.emptyList();
		}
		return waypointSection.getKeys(false).stream().map(this::getWaypoint).filter(Objects::nonNull)
				.distinct().sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
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
