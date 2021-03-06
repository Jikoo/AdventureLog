package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.SimpleWaypoint;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.ui.TeleportButton;
import com.github.jikoo.util.ItemUtil;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UserWaypointUI extends SimpleUI {

	private UserWaypointUI(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(getName(owner));

		if (plugin.getConfig().getBoolean("personal.spawn-as-waypoint")) {
			World world = plugin.getServer().getWorlds().get(0);
			addButton(new TeleportButton(plugin, new SimpleWaypoint("Spawn", Material.COMPASS, world.getSpawnLocation())));
		}

		if (plugin.getConfig().getBoolean("personal.respawn-point.as-waypoint")) {
			OfflinePlayer target = plugin.getServer().getOfflinePlayer(owner);
			Location destination;
			if (!target.isOnline() && !target.hasPlayedBefore()
					|| (destination = target.getBedSpawnLocation()) == null
					&& !plugin.getConfig().getBoolean("personal.respawn-point.default-to-spawn")) {
				addButton(new Button(ItemUtil.getItem(Material.BARRIER, ChatColor.RED + "No Respawn Point"), event -> event.setCancelled(true)));
			} else {
				// TODO charge respawn anchor
				addButton(new TeleportButton(plugin, new SimpleWaypoint("Respawn Location", Material.RESPAWN_ANCHOR, () -> {
					if (destination == null) {
						return plugin.getServer().getWorlds().get(0).getSpawnLocation();
					}
					return destination;
				})));
			}
		}

		plugin.getDataManager().getUserData(owner).getUserWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		ServerWaypointEditor.addIfEligible(this, viewer, plugin, owner);
		ServerWaypointUI.addIfEligible(this, plugin, owner);
		UserWaypointUI.addIfEligible(this, viewer, plugin, owner);
		UserWaypointEditor.addIfEligible(this, viewer, plugin, owner);

	}

	private static String getName(UUID uuid) {
		OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
		if (offline.getName() != null) {
			return ChatColor.DARK_PURPLE + "Personal/" + offline.getName();
		}
		return ChatColor.DARK_PURPLE.toString() + uuid;
	}

	public static void addIfEligible(@NotNull SimpleUI targetUI, @NotNull Player viewer,
			@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		if (!(targetUI instanceof UserWaypointUI)
				&& (viewer.getUniqueId().equals(owner) || viewer.hasPermission("adventurelog.view.other"))
				&& (plugin.getConfig().getBoolean("personal.spawn-as-waypoint")
						|| plugin.getConfig().getBoolean("personal.respawn-point.as-waypoint")
						|| plugin.getDataManager().getUserData(owner).getUserWaypoints().size() > 0)) {
			targetUI.setNavButton(3, getButton(plugin, owner));
		}
	}

	private static Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(ItemUtil.getItem(Material.RED_BED, ChatColor.GOLD + "Personal Waypoints"), event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				player.openInventory(new UserWaypointUI(plugin, owner, player).getInventory());
			}
		});
	}

}
