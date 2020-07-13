package com.github.jikoo.ui;

import com.github.jikoo.AdventureLogPlugin;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AdventureLogUI extends SimpleUI {

	public AdventureLogUI(AdventureLogPlugin plugin, UUID owner, Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log");

		plugin.getDataManager().getUserData(owner).getAvailableWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		if (!plugin.getConfig().getBoolean("personal.spawn-as-waypoint")
				&& !plugin.getConfig().getBoolean("personal.respawn-point.as-waypoint")
				&& plugin.getPermittedPersonalWarps(viewer) == 0) {
			return;
		}

		setNavButton(2, new Button(Button.createIcon(Material.RED_BED, ChatColor.GOLD + "Personal Waypoints"),
				event -> event.getWhoClicked().openInventory(new UserWaypointUI(plugin, owner, viewer).getInventory())));
		// TODO set up a standard - back on right, edit on left, deeper in middle?
	}

}
