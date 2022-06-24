package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.UserWaypoint;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.util.ItemUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserWaypointEditor extends SimpleUI {

	UserWaypointEditor(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log Editor");

		plugin.getDataManager().getUserData(owner).getUserWaypoints().forEach(waypoint ->
				addButton(IndividualWaypointEditor.getButton(plugin, UserWaypoint.class, waypoint, owner)));

		setNavButton(1, new Button(plugin.getWaypointBook(), event -> {
			if (event.getWhoClicked() instanceof Player player) {
				player.openInventory(new ServerWaypointUI(plugin, player.getUniqueId(), player).getInventory());
			}
		}));

		if (viewer.getUniqueId().equals(owner) && this.getHighestButton() + 1 < plugin.getPermittedPersonalWarps(viewer)
			|| !viewer.getUniqueId().equals(owner) && viewer.hasPermission("adventurelog.manage.other")) {
			setNavButton(2, IndividualWaypointEditor.getButton(plugin, UserWaypoint.class, null, owner));
		}

		ServerWaypointEditor.addIfEligible(this, viewer, plugin, owner);
		ServerWaypointUI.addIfEligible(this, plugin, owner);
		UserWaypointUI.addIfEligible(this, viewer, plugin, owner);
		UserWaypointEditor.addIfEligible(this, viewer, plugin, owner);

	}

	public static void addIfEligible(
			@NotNull SimpleUI targetUI,
			@NotNull Player viewer,
			@NotNull AdventureLogPlugin plugin,
			@NotNull UUID owner) {
		if (!(targetUI instanceof UserWaypointEditor) && (!viewer.getUniqueId().equals(owner)
				&& viewer.hasPermission("adventurelog.manage.other") || viewer.getUniqueId().equals(owner)
				&& (plugin.getPermittedPersonalWarps(viewer) > 0
				|| plugin.getDataManager().getUserData(owner).getUserWaypoints().size() > 0))) {
			targetUI.setNavButton(4, getButton(plugin, owner));
		}
	}

	private static Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(
				ItemUtil.getItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "Edit Personal Waypoints"),
				event -> {
					if (event.getWhoClicked() instanceof Player player) {
						player.openInventory(new UserWaypointEditor(plugin, owner, player).getInventory());
					}
				});
	}

}
