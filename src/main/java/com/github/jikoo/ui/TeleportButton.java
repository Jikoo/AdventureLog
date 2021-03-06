package com.github.jikoo.ui;

import com.github.jikoo.data.IWaypoint;
import com.github.jikoo.util.DelayedTeleport;
import com.github.jikoo.util.ItemUtil;
import com.github.jikoo.util.TextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class TeleportButton extends Button {

	public TeleportButton(@NotNull Plugin plugin, @NotNull IWaypoint waypoint) {
		super(getItem(waypoint), event -> {
			Bukkit.getScheduler().runTask(plugin, () -> event.getWhoClicked().closeInventory());
			if (!(event.getWhoClicked() instanceof Player)) {
				return;
			}
			Player player = (Player) event.getWhoClicked();
			Location destination = waypoint.getLocation();
			if (!destination.isWorldLoaded()) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("World not loaded!", ChatColor.RED));
				return;
			}
			new DelayedTeleport(plugin, player, destination, 3)
					.runTaskTimer(plugin, 0L, 2L);
		});
	}

	private static ItemStack getItem(IWaypoint waypoint) {
		return ItemUtil.appendText(waypoint.getIcon().clone(), ChatColor.GOLD + TextUtil.getDisplay(waypoint.getLocation()), ChatColor.GOLD + "Click to teleport");
	}

}
