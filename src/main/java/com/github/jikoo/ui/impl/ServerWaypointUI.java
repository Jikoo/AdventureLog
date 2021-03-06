package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.ui.TeleportButton;
import com.github.jikoo.util.ItemUtil;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServerWaypointUI extends SimpleUI {

	public ServerWaypointUI(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner, @NotNull Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log");

		plugin.getDataManager().getUserData(owner).getAvailableWaypoints()
				.forEach(waypoint -> addButton(new TeleportButton(plugin, waypoint)));

		ServerWaypointEditor.addIfEligible(this, viewer, plugin, owner);
		ServerWaypointUI.addIfEligible(this, plugin, owner);
		UserWaypointUI.addIfEligible(this, viewer, plugin, owner);
		UserWaypointEditor.addIfEligible(this, viewer, plugin, owner);
	}

	public static void addIfEligible(@NotNull SimpleUI targetUI, @NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		if (!(targetUI instanceof ServerWaypointUI)) {
			targetUI.setNavButton(1, getButton(plugin, owner));
		}
	}

	private static Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull UUID owner) {
		return new Button(ItemUtil.getItem(Material.KNOWLEDGE_BOOK, ChatColor.GOLD + "Server Waypoints"), event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				player.openInventory(new ServerWaypointUI(plugin, owner, player).getInventory());
			}
		});
	}

}
