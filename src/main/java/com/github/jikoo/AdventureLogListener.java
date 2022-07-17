package com.github.jikoo;

import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.ui.impl.ServerWaypointUI;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.EquipmentSlot;
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
		if (!(event.getView().getTopInventory().getHolder() instanceof SimpleUI)) {
			return;
		}

		if (((SimpleUI) event.getView().getTopInventory().getHolder()).isActionBlocking()) {
			event.setCancelled(true);
			return;
		}

		int size = event.getView().getTopInventory().getSize();
		for (int slot : event.getRawSlots()) {
			// TODO: InventoryDragEvent#getRawSlots is mutable, InventoryDragEvent#getNewItems is not
			//  - possible to remove top slots instead of cancelling? Needs testing
			if (slot < size) {
				event.setCancelled(true);
				return;
			}
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

		event.getPlayer().openInventory(new ServerWaypointUI(plugin, event.getPlayer().getUniqueId(), event.getPlayer()).getInventory());

	}

	@EventHandler
	public void onPlayerRecipeDiscover(@NotNull PlayerRecipeDiscoverEvent event) {
		if (plugin.waypointRecipeKey.equals(event.getRecipe())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
		if (!plugin.getConfig().getBoolean("general.keep-book-on-death") || event.getKeepInventory()) {
			return;
		}

		event.getDrops().removeIf(drop -> {
			if (plugin.isWaypointBook(drop)) {
				event.getItemsToKeep().add(drop);
				return true;
			}
			return false;
		});
	}

}
