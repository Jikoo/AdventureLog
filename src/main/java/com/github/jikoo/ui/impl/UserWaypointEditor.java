package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.UserWaypoint;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.util.ItemUtil;
import com.github.jikoo.util.AdvLogPerms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserWaypointEditor extends SimpleUI {

	UserWaypointEditor(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(Component.text("Adventure Log Editor").color(NamedTextColor.DARK_PURPLE));

		plugin.getDataManager().getUserData(owner).getUserWaypoints().forEach(waypoint ->
				addButton(IndividualWaypointEditor.getButton(plugin, UserWaypoint.class, waypoint, owner)));

		setNavButton(1, new Button(plugin.getWaypointBook(), event -> {
			if (event.getWhoClicked() instanceof Player player) {
				player.openInventory(new ServerWaypointUI(plugin, player.getUniqueId(), player).getInventory());
			}
		}));

		if (viewer.getUniqueId().equals(owner) && this.getHighestButton() + 1 < AdvLogPerms.getPermittedPersonalWarps(plugin.getConfig(), viewer)
			|| !viewer.getUniqueId().equals(owner) && viewer.hasPermission(AdvLogPerms.MANAGE_PLAYER)) {
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
				&& viewer.hasPermission(AdvLogPerms.MANAGE_PLAYER) || viewer.getUniqueId().equals(owner)
				&& (AdvLogPerms.getPermittedPersonalWarps(plugin.getConfig(), viewer) > 0
				|| plugin.getDataManager().getUserData(owner).getUserWaypoints().size() > 0))) {
			targetUI.setNavButton(4, getButton(plugin, owner));
		}
	}

	private static @NotNull Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(
				ItemUtil.getItem(Material.WRITABLE_BOOK, ItemUtil.text("Edit Personal Waypoints").color(NamedTextColor.GOLD)),
				event -> {
					if (event.getWhoClicked() instanceof Player player) {
						player.openInventory(new UserWaypointEditor(plugin, owner, player).getInventory());
					}
				});
	}

}
