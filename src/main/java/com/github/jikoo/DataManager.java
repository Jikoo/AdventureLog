package com.github.jikoo;

import com.github.jikoo.data.ServerData;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DataManager {

	private final @NotNull AdventureLogPlugin plugin;
	private final @NotNull ServerData serverData;
	private final @NotNull LoadingCache<UUID, UserData> playerCache;

	DataManager(@NotNull AdventureLogPlugin plugin) {
		this.plugin = plugin;
		this.serverData = new ServerData(plugin, this::tryDiscovery);
		this.playerCache = CacheBuilder.newBuilder()
				.expireAfterAccess(5, TimeUnit.MINUTES)
				.build(CacheLoader.from(uuid -> {
					if (uuid == null) {
						throw new NullPointerException("UUID cannot be null!");
					}
					return new UserData(plugin, uuid, () -> serverData);
				}));
		serverData.scheduleDiscovery(plugin);
	}

	void save() {
		this.playerCache.asMap().forEach((key, value) -> {
			try {
				value.forceSave();
			} catch (IOException exception) {
				plugin.getLogger().log(Level.WARNING, exception, () -> String.format("Could not save data for %s", key));
			}
		});

		try {
			this.serverData.forceSave();
		} catch (IOException exception) {
			plugin.getLogger().log(Level.WARNING, "Could not save waypoint data", exception);
		}
	}

	private void tryDiscovery(@NotNull Collection<String> waypointNames) {
		var waypoints = waypointNames.stream().map(serverData::getWaypoint).filter(Objects::nonNull).toList();

		if (waypoints.isEmpty()) {
			return;
		}

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (discoveryBlocked(player)) {
				continue;
			}

			for (ServerWaypoint waypoint : waypoints) {
				if (waypoint.getRangeSquared() > -1 && player.getWorld().equals(waypoint.getLocation().getWorld())
						&& player.getLocation().distanceSquared(waypoint.getLocation()) <= waypoint.getRangeSquared()
						&& playerCache.getUnchecked(player.getUniqueId()).unlockWaypoint(waypoint.getName())) {
					player.showTitle(Title.title(
							Component.text("Waypoint discovered!"),
							Component.text("Check your Adventure Log."),
							Times.times(Duration.ofMillis(500L), Duration.ofMillis(2_500L), Duration.ofMillis(1_000L))));
				}
			}
		}
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

	public @NotNull ServerData getServerData() {
		return this.serverData;
	}

	public @NotNull UserData getUserData(@NotNull UUID uuid) {
		return this.playerCache.getUnchecked(uuid);
	}

}
