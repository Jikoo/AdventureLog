package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.ui.TeleportButton;
import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ServerWaypointUI extends SimpleUI {

	public ServerWaypointUI(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(Component.text("Adventure Log").color(NamedTextColor.DARK_PURPLE));

		plugin.getDataManager().getUserData(owner).getAvailableWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		ServerWaypointEditor.addIfEligible(this, viewer, plugin, owner);
		ServerWaypointUI.addIfEligible(this, plugin, owner);
		UserWaypointUI.addIfEligible(this, viewer, plugin, owner);
		UserWaypointEditor.addIfEligible(this, viewer, plugin, owner);
	}

	public static void addIfEligible(
			@NotNull SimpleUI targetUI,
			@NotNull AdventureLogPlugin plugin,
			@NotNull UUID owner) {
		if (!(targetUI instanceof ServerWaypointUI)) {
			targetUI.setNavButton(1, getButton(plugin, owner));
		}
	}

	private static @NotNull Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(
				TextUtil.getTextItem(Material.KNOWLEDGE_BOOK, TextUtil.itemText("Server Waypoints").color(NamedTextColor.GOLD)),
				event -> {
					if (event.getWhoClicked() instanceof Player player) {
						player.openInventory(new ServerWaypointUI(plugin, owner, player).getInventory());
					}
				});
	}

}
