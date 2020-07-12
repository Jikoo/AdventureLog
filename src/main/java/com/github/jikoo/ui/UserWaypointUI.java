package com.github.jikoo.ui;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.SimpleWaypoint;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UserWaypointUI extends SimpleUI {

	public UserWaypointUI(AdventureLogPlugin plugin, UUID owner, Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log/" + owner);

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
				ItemStack itemStack = new ItemStack(Material.BARRIER);
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta != null) {
					itemMeta.setDisplayName(ChatColor.RED + "No Respawn Point");
					itemStack.setItemMeta(itemMeta);
				}
				addButton(new Button(itemStack, event -> event.setCancelled(true)));
			} else {
				// TODO charge respawn anchor, ensure safe teleport
				addButton(new TeleportButton(plugin, new SimpleWaypoint("Respawn Location", Material.RED_BED, () -> {
					if (destination == null) {
						return plugin.getServer().getWorlds().get(0).getSpawnLocation();
					}
					return destination;
				})));
			}
		}

		plugin.getDataManager().getUserData(owner).getUserWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		// TODO EditorUI (possibly shared with ManageWaypointsCommand) and navbar button

	}

}
