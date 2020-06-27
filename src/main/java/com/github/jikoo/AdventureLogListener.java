package com.github.jikoo;

import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.SimpleUI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class AdventureLogListener implements Listener {

	private final AdventureLogPlugin plugin;

	AdventureLogListener(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(@NotNull InventoryClickEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
			((SimpleUI) event.getView().getTopInventory().getHolder()).handleClick(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(@NotNull InventoryDragEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (event.getHand() == EquipmentSlot.OFF_HAND) {
			// Cancel if main hand should be opening GUI
			if (plugin.isWaypointBook(event.getPlayer().getInventory().getItemInMainHand())) {
				event.setUseInteractedBlock(Event.Result.DENY);
				event.setUseItemInHand(Event.Result.DENY);
				return;
			}
			if (!plugin.isWaypointBook(event.getPlayer().getInventory().getItemInOffHand())) {
				return;
			}
		} else if (!plugin.isWaypointBook(event.getPlayer().getInventory().getItemInMainHand())) {
			return;
		}

		event.setUseInteractedBlock(Event.Result.DENY);
		event.setUseItemInHand(Event.Result.DENY);

		SimpleUI ui = new SimpleUI(ChatColor.DARK_PURPLE + "Adventure Log");
		plugin.getDataStore().getWaypoints(event.getPlayer().getUniqueId()).forEach(waypoint -> {
			Button button = new Button(waypoint.getIcon(), inventoryClickEvent -> {
				inventoryClickEvent.setCancelled(true);
				Bukkit.getScheduler().runTask(plugin, () -> inventoryClickEvent.getWhoClicked().closeInventory());
				if (!(inventoryClickEvent.getWhoClicked() instanceof Player)) {
					return;
				}
				Player player = (Player) inventoryClickEvent.getWhoClicked();
				if (!waypoint.getLocation().isWorldLoaded()) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("World not loaded!", ChatColor.RED));
					return;
				}
				new DelayedTeleport(player, waypoint.getLocation(), 3)
						.runTaskTimer(plugin, 0L, 2L);
			});
			ui.addButton(button);
		});

		event.getPlayer().openInventory(ui.getInventory());

	}

	@EventHandler
	public void onPlayerRecipeDiscover(@NotNull PlayerRecipeDiscoverEvent event) {
		NamespacedKey recipe = event.getRecipe();

		if (recipe.getNamespace().equals(plugin.getName().toLowerCase()) && recipe.getKey().equals("adventure_log")) {
			event.setCancelled(true);
		}
	}

	private class DelayedTeleport extends BukkitRunnable {
		private final BossBar bossBar;
		private final Player target;
		private final Vector originalLocation;
		private final Location teleportTo;
		private final int maxCycles;
		private int count = 0;

		DelayedTeleport(Player target, Location teleportTo, int seconds) {
			this.target = target;
			this.teleportTo = teleportTo;
			bossBar = plugin.getServer().createBossBar("Concentrating...", BarColor.PURPLE, BarStyle.SEGMENTED_20);
			bossBar.setProgress(0);
			bossBar.addPlayer(target);
			originalLocation = target.getLocation().toVector();
			maxCycles = seconds * 10;
		}

		@Override
		public void run() {
			if (moved()) {
				target.sendMessage("You have to maintain concentration to remember the way!");
				bossBar.removeAll();
				cancel();
				return;
			}

			if (count >= maxCycles) {
				target.teleport(teleportTo);
				bossBar.removeAll();
				cancel();
				return;
			}
			this.bossBar.setProgress(count * 1D / maxCycles);
			++count;
		}

		private boolean moved() {
			if (!target.isOnline()) {
				return true;
			}
			Location newLocation = target.getLocation();
			double min = Math.min(originalLocation.getX(), newLocation.getX());
			double max = originalLocation.getX() == min ? newLocation.getX() : originalLocation.getX();
			if (Math.abs(max - min) > 1) {
				return true;
			}
			min = Math.min(originalLocation.getY(), newLocation.getY());
			max = originalLocation.getY() == min ? newLocation.getY() : originalLocation.getY();
			if (Math.abs(max - min) > 1) {
				return true;
			}
			min = Math.min(originalLocation.getZ(), newLocation.getZ());
			max = originalLocation.getZ() == min ? newLocation.getZ() : originalLocation.getZ();
			return Math.abs(max - min) > 1;
		}
	}

}
