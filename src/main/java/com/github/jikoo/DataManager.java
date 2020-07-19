package com.github.jikoo;

import com.github.jikoo.data.ServerData;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DataManager {

	private final AdventureLogPlugin plugin;
	private final ServerData serverData;
	private final LoadingCache<UUID, UserData> playerCache;

	DataManager(AdventureLogPlugin plugin) {
		this.plugin = plugin;
		this.serverData = new ServerData(plugin);
		this.playerCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(CacheLoader.from(uuid -> {
			if (uuid == null) {
				throw new NullPointerException("UUID cannot be null!");
			}
			return new UserData(plugin, uuid, () -> serverData);
		}));
		startDiscovery();
	}

	private void startDiscovery() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			Collection<ServerWaypoint> nextDiscoverySegment = serverData.next();

			if (nextDiscoverySegment.isEmpty()) {
				// Quick return for few waypoints
				return;
			}

			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (discoveryBlocked(player)) {
					continue;
				}
				for (ServerWaypoint waypoint : nextDiscoverySegment) {
					if (waypoint.getRangeSquared() > -1 && player.getWorld().equals(waypoint.getLocation().getWorld())
							&& player.getLocation().distanceSquared(waypoint.getLocation()) <= waypoint.getRangeSquared()
							&& playerCache.getUnchecked(player.getUniqueId()).unlockWaypoint(waypoint.getName())) {
						player.sendTitle("Waypoint discovered!", "Check your Adventure Log.", 10, 50, 20);
					}
				}
			}

		}, 60L, 1L);
	}

	private boolean discoveryBlocked(@NotNull Player player) {
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

	public ServerData getServerData() {
		return this.serverData;
	}

	public UserData getUserData(@NotNull UUID uuid) {
		return this.playerCache.getUnchecked(uuid);
	}

}
