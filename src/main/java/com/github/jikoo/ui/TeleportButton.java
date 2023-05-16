package com.github.jikoo.ui;

import com.github.jikoo.data.IWaypoint;
import com.github.jikoo.event.DelayedTeleportEvent;
import com.github.jikoo.util.DelayedTeleport;
import com.github.jikoo.util.ItemUtil;
import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
			if (!(event.getWhoClicked() instanceof Player player)) {
				return;
			}
			Location destination = waypoint.getLocation();
			if (!destination.isWorldLoaded()) {
				player.sendActionBar(Component.text("World not loaded!").color(NamedTextColor.RED));
				return;
			}
			DelayedTeleportEvent delayedTeleportEvent = new DelayedTeleportEvent(player, waypoint, 8);
			delayedTeleportEvent.fire();
			new DelayedTeleport(plugin, player, destination, delayedTeleportEvent.getDelaySeconds())
					.runTaskTimer(plugin, 0L, 2L);
		});
	}

	private static @NotNull ItemStack getItem(@NotNull IWaypoint waypoint) {
		return ItemUtil.appendText(
				waypoint.getIcon().clone(),
				TextUtil.itemText(TextUtil.getDisplay(waypoint.getLocation())).color(NamedTextColor.GOLD),
				TextUtil.itemText("Click to teleport").color(NamedTextColor.GOLD));
	}

}
