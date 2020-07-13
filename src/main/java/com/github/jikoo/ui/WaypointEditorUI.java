package com.github.jikoo.ui;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.Waypoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaypointEditorUI extends SimpleUI {

	public WaypointEditorUI(@NotNull AdventureLogPlugin plugin, @Nullable UUID owner, @NotNull Player viewer) {
		super(ChatColor.DARK_PURPLE + "Adventure Log Editor", true);

		if (owner != null) {
			plugin.getDataManager().getUserData(owner).getUserWaypoints().forEach(waypoint ->
					addButton(getWaypointEditorButton(plugin, waypoint, owner, viewer.getUniqueId())));
		} else {
			plugin.getDataManager().getServerData().getWaypoints().forEach(waypoint ->
					addButton(getWaypointEditorButton(plugin, waypoint, null, viewer.getUniqueId())));

			if (viewer.hasPermission("adventurelog.manage.other")) {
				// TODO add nav button for other editor
				setNavButton(2, Button.empty());
			}
		}

		if (owner == null || viewer.getUniqueId().equals(owner) && this.getHighestButton() + 1 < plugin.getPermittedPersonalWarps(viewer)
			|| !viewer.getUniqueId().equals(owner) && viewer.hasPermission("adventurelog.manage.other")) {
			addButton(getWaypointEditorButton(plugin, null, owner, viewer.getUniqueId()));
		}
	}

	private Button getWaypointEditorButton(@NotNull AdventureLogPlugin plugin, @Nullable Waypoint waypoint,
			@Nullable UUID owner, @NotNull UUID creator) {
		return new Button(() -> generateItem(plugin, waypoint, owner, creator), event -> {
			ItemStack clicked = event.getCurrentItem();
			if (clicked == null || clicked.getType().isAir()) {
				return;
			}

			if (waypoint != null) {
				if (waypoint.isInvalid()) {
					clicked.setType(Material.AIR);
					return;
				}
				if (event.getClick() == ClickType.CONTROL_DROP) {
					waypoint.delete();
					clicked.setType(Material.AIR);

					if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
						((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
					}
					return;
				}

				// Allow creative middle click copying
				if (event.getClick() == ClickType.MIDDLE && event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
					//noinspection deprecation - works fine due to inventory updates on cancellation
					event.setCursor(waypoint.getIcon());
					return;
				}
			}

			if (waypoint == null && owner != null) {
				plugin.getDataManager().getUserData(owner).createWaypoint(event.getWhoClicked().getLocation());
				if (event.getWhoClicked() instanceof Player) {
					event.getWhoClicked().openInventory(new WaypointEditorUI(plugin, owner, (Player) event.getWhoClicked()).getInventory());
				} else {
					event.getWhoClicked().closeInventory();
				}
				return;
			}

			event.getWhoClicked().openInventory(new WaypointModificationUI(plugin, waypoint, owner).getInventory());
		});
	}

	private static ItemStack generateItem(@NotNull AdventureLogPlugin plugin, @Nullable Waypoint waypoint, @Nullable UUID owner, @NotNull UUID creator) {
		ItemStack itemStack;
		if (waypoint != null) {
			if (waypoint.isInvalid()) {
				// Waypoint has been deleted.
				return new ItemStack(Material.AIR);
			}

			List<String> list = new ArrayList<>();
			list.add(org.bukkit.ChatColor.WHITE + "Edit waypoint: " + org.bukkit.ChatColor.GOLD + waypoint.getName());
			Location location = waypoint.getLocation();
			list.add(org.bukkit.ChatColor.WHITE + "Location: " + org.bukkit.ChatColor.GOLD + String.format("%s: %sx, %sy, %sz",
					location.getWorld() == null ? "invalid_world" : location.getWorld().getName(),
					location.getBlockX(), location.getBlockY(), location.getBlockZ()));
			if (waypoint instanceof ServerWaypoint) {
				ServerWaypoint serverWaypoint = (ServerWaypoint) waypoint;
				list.add(org.bukkit.ChatColor.WHITE + "Priority: " + org.bukkit.ChatColor.GOLD + serverWaypoint.getPriority());
				list.add(org.bukkit.ChatColor.WHITE + "Discovery range: " + org.bukkit.ChatColor.GOLD
						+ (serverWaypoint.getRangeSquared() < 1 ? -1 : (int) Math.sqrt(serverWaypoint.getRangeSquared())));
				list.add(org.bukkit.ChatColor.WHITE + "Always Discovered: " + org.bukkit.ChatColor.GOLD + serverWaypoint.isDefault());
			}
			list.add("");
			list.add(org.bukkit.ChatColor.RED + "Ctrl+drop to delete.");
			itemStack = waypoint.getIcon().clone();
			Button.modifyIcon(itemStack, list.toArray(new String[0]));
			return itemStack;
		}

		if (owner != null) {
			Player editor = plugin.getServer().getPlayer(creator);
			if (editor == null || !owner.equals(creator) && !editor.hasPermission("adventurelog.manage.other")) {
				return new ItemStack(Material.AIR);
			}
			return Button.createIcon(Material.WRITABLE_BOOK, org.bukkit.ChatColor.GOLD + "Create New Waypoint");
		}

		return Button.createIcon(Material.WRITABLE_BOOK, org.bukkit.ChatColor.GOLD + "New Waypoint Editor");
	}

}
