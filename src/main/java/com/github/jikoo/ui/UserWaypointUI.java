package com.github.jikoo.ui;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.SimpleWaypoint;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class UserWaypointUI extends SimpleUI {

	public UserWaypointUI(AdventureLogPlugin plugin, UUID owner, Player viewer) {
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
				addButton(new Button(Button.createIcon(Material.BARRIER, ChatColor.RED + "No Respawn Point"), event -> event.setCancelled(true)));
			} else {
				// TODO charge respawn anchor, ensure safe teleport
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

		if (!viewer.getUniqueId().equals(owner)) {
			if (!viewer.hasPermission("adventurelog.manage.other")) {
				return;
			}
		} else if (plugin.getPermittedPersonalWarps(viewer) == 0) {
			return;
		}

		setNavButton(2, new Button(
				Button.createIcon(Material.WRITABLE_BOOK, ChatColor.DARK_PURPLE + "Open Editor"),
				event -> {
					event.setCancelled(true);
					if (!(event.getWhoClicked() instanceof Player)) {
						return;
					}
					event.getWhoClicked().openInventory(new WaypointEditorUI(plugin, owner, (Player) event.getWhoClicked()).getInventory());
		}));

	}

	private static String getName(UUID uuid) {
		OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
		if (offline.getName() != null) {
			return ChatColor.DARK_PURPLE + "Personal/" + offline.getName();
		}
		return ChatColor.DARK_PURPLE.toString() + uuid;
	}

}
