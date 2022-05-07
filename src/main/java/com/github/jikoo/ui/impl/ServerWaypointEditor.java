package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.util.ItemUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ServerWaypointEditor extends SimpleUI {

	ServerWaypointEditor(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log Editor");

		plugin.getDataManager().getServerData().getWaypoints().forEach(waypoint ->
				addButton(IndividualWaypointEditor.getButton(plugin, ServerWaypoint.class, waypoint, owner)));

		setNavButton(2, IndividualWaypointEditor.getButton(plugin, ServerWaypoint.class, null, owner));

		ServerWaypointEditor.addIfEligible(this, viewer, plugin, owner);
		ServerWaypointUI.addIfEligible(this, plugin, owner);
		UserWaypointUI.addIfEligible(this, viewer, plugin, owner);
		UserWaypointEditor.addIfEligible(this, viewer, plugin, owner);
	}

	public static void addIfEligible(@NotNull SimpleUI targetUI, @NotNull Player viewer,
			@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		if (!(targetUI instanceof  ServerWaypointEditor) && viewer.hasPermission("adventurelog.manage.server")) {
			targetUI.setNavButton(0, getButton(plugin, owner));
		}
	}

	private static Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(ItemUtil.getItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "Edit Server Waypoints"), event -> {
			if (event.getWhoClicked() instanceof Player player) {
				player.openInventory(new ServerWaypointEditor(plugin, owner, player).getInventory());
			}
		});
	}

}
