package com.github.jikoo;

import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import com.github.jikoo.data.Waypoint;
import com.github.jikoo.data.WaypointData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.lucko.helper.bucket.Bucket;
import me.lucko.helper.bucket.BucketPartition;
import me.lucko.helper.bucket.factory.BucketFactory;
import me.lucko.helper.bucket.partitioning.PartitioningStrategies;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO better save system - probably trigger BukkitTask on edit
public class DataStore {

	public enum Result {
		SUCCESS,
		FAILURE,
		UNNECESSARY
	}

	private final AdventureLogPlugin plugin;
	private final File playerFolder;
	private final LoadingCache<UUID, UserData> playerCache;
	private final Bucket<ServerWaypoint> discoverableWaypoints;
	private final Set<String> defaultWaypoints;
	private final Map<String, ServerWaypoint> loadedWaypoints;
	private WaypointData waypointData;

	DataStore(AdventureLogPlugin plugin) {
		this.plugin = plugin;
		this.playerFolder = new File(plugin.getDataFolder(), "playerdata");
		this.playerCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
				.removalListener((RemovalListener<UUID, UserData>) removalNotification -> {
					try {
						removalNotification.getValue().save();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}).build(CacheLoader.from(uuid -> {
					if (uuid == null) {
						throw new NullPointerException("UUID cannot be null!");
					}
					return new UserData(new File(playerFolder, uuid.toString() + ".yml"));
				}));
		this.discoverableWaypoints = BucketFactory.newHashSetBucket(60, PartitioningStrategies.lowestSize());
		this.loadedWaypoints = new HashMap<>();
		this.defaultWaypoints = new HashSet<>();
		reloadWaypoints();
		startDiscovery();
	}

	private void startDiscovery() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			BucketPartition<ServerWaypoint> partition = discoverableWaypoints.asCycle().next();

			if (partition.isEmpty()) {
				// Quick return for few waypoints
				return;
			}

			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (discoveryBlocked(player)) {
					continue;
				}
				for (ServerWaypoint waypoint : partition) {
					if (waypoint.getRangeSquared() > -1 && player.getWorld().equals(waypoint.getLocation().getWorld())
							&& player.getLocation().distanceSquared(waypoint.getLocation()) <= waypoint.getRangeSquared()
							&& unlockWaypoint(player.getUniqueId(), waypoint.getName()) == DataStore.Result.SUCCESS) {
						player.sendTitle("Waypoint discovered!", "Check your Adventure Log.", 10, 50, 20);
					}
				}
			}

		}, 60L, 1L);
	}

	private boolean discoveryBlocked(Player player) {
		// Must be in acceptable game mode to discover waypoints
		if (!plugin.getConfig().getStringList("discovery.gamemodes").contains(player.getGameMode().name())) {
			return true;
		}

		// Is book required?
		if (!plugin.getConfig().getBoolean("discovery.requires-book")) {
			return false;
		}

		// Check inventory for book
		for (ItemStack itemStack : player.getInventory().getContents()) {
			if (plugin.isWaypointBook(itemStack)) {
				return false;
			}
		}

		return true;
	}

	private void reloadWaypoints() {
		this.waypointData = new WaypointData(new File(plugin.getDataFolder(), "waypoints.yml"));
		this.waypointData.getWaypoints().forEach(waypointName -> {
			ServerWaypoint loaded = new ServerWaypoint(waypointData, waypointName);
			// Ensure waypoint has location
			if (!loaded.isValid()) {
				loaded.delete();
				return;
			}
			loadedWaypoints.put(waypointName, loaded);
			if (loaded.getRangeSquared() > 0) {
				discoverableWaypoints.add(loaded);
			}
		});
		defaultWaypoints.addAll(waypointData.getDefaultWaypoints());
	}

	void save() {
		this.playerCache.invalidateAll();
		this.playerCache.cleanUp();
		try {
			this.waypointData.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a ServerWaypoint.
	 *
	 * @param name the name of the waypoint
	 * @param icon the item representation of the waypoint
	 * @param location the location of the waypoint
	 */
	public ServerWaypoint addServerWaypoint(@NotNull String name, @NotNull ItemStack icon, @NotNull Location location) {
		ServerWaypoint existing = getServerWaypoint(name);
		if (existing != null) {
			existing.setIcon(icon);
			existing.setLocation(location);
			return existing;
		}
		ServerWaypoint waypoint = new ServerWaypoint(this.waypointData, name, icon, location);
		try {
			this.waypointData.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return waypoint;
	}

	/**
	 * Remove a Waypoint.
	 *
	 * @param waypointName the name of the Waypoint to remove
	 */
	public void removeWaypoint(@NotNull String waypointName) {
		Waypoint removed = this.loadedWaypoints.remove(waypointName);
		if (removed != null) {
			removed.delete();
		}
		try {
			this.waypointData.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a Waypoint by name.
	 *
	 * @param waypointName the name of the Waypoint
	 * @return the Waypoint if it exists
	 */
	public @Nullable ServerWaypoint getServerWaypoint(@NotNull String waypointName) {
		return loadedWaypoints.get(waypointName);
	}

	/**
	 * Get a Collection of all Waypoints that are unlocked for everyone.
	 *
	 * @return the default Waypoints
	 */
	@NotNull
	public Collection<ServerWaypoint> getDefaultWaypoints() {
		return defaultWaypoints.stream().map(this::getServerWaypoint).filter(Objects::nonNull)
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
		if (getServerWaypoint(waypointName) == null) {
			return Result.FAILURE;
		}
		try {
			UserData playerData = this.playerCache.get(uuid);
			List<String> unlockedWaypoints = playerData.getUnlocked();
			if (unlockedWaypoints.contains(waypointName)) {
				return Result.UNNECESSARY;
			}
			unlockedWaypoints.add(waypointName);
			playerData.setUnlocked(unlockedWaypoints);
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
			UserData playerData = this.playerCache.get(uuid);
			List<String> unlockedWaypoints = playerData.getUnlocked();
			if (!unlockedWaypoints.remove(waypointName)) {
				return Result.UNNECESSARY;
			}
			playerData.setUnlocked(unlockedWaypoints);
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
	public Collection<? extends Waypoint> getWaypoints(UUID uuid) {
		UserData playerData;
		try {
			playerData = this.playerCache.get(uuid);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return getDefaultWaypoints();
		}

		return Stream.concat(getDefaultWaypoints().stream(),
				playerData.getUnlocked().stream().map(this::getServerWaypoint).filter(Objects::nonNull))
				.distinct().sorted(Comparator.comparing(Waypoint::getPriority).reversed()).collect(Collectors.toList());
	}

}
