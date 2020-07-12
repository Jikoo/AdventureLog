package com.github.jikoo.ui;

import com.github.jikoo.AdventureLogPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdventureLogUI extends SimpleUI {

	public AdventureLogUI(AdventureLogPlugin plugin, Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log");

		plugin.getDataManager().getUserData(viewer.getUniqueId()).getAvailableWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		if (!plugin.getConfig().getBoolean("personal.spawn-as-waypoint")
				&& !plugin.getConfig().getBoolean("personal.respawn-point.as-waypoint")
				&& plugin.getPermittedPersonalWarps(viewer) == 0) {
			return;
		}

		ItemStack itemStack = new ItemStack(Material.RED_BED);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(ChatColor.GOLD + "Personal Waypoints");

			itemStack.setItemMeta(itemMeta);
		}

		setNavButton(2, new Button(itemStack, event ->
				event.getWhoClicked().openInventory(new UserWaypointUI(plugin, viewer.getUniqueId(), viewer).getInventory())));
	}

}
