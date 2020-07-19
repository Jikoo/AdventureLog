package com.github.jikoo.ui.impl;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import com.github.jikoo.data.UserWaypoint;
import com.github.jikoo.data.Waypoint;
import com.github.jikoo.ui.BooleanButton;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.IntegerButton;
import com.github.jikoo.ui.SimpleUI;
import com.github.jikoo.util.ItemUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndividualWaypointEditor extends SimpleUI {

	public IndividualWaypointEditor(@NotNull AdventureLogPlugin plugin, @NotNull Class<? extends Waypoint> waypointClazz,
			@Nullable Waypoint waypoint, @NotNull UUID owner) {
		super(ChatColor.DARK_RED + (waypoint != null ? "Modify " + waypoint.getName() : "New Waypoint Creator"), false);

		// BUTTON: set icon item
		ItemStack waypointIcon;
		ItemStack waypointItem;
		if (waypoint != null) {
			waypointIcon = waypoint.getIcon().clone();
			ItemUtil.insertText(waypointIcon, ChatColor.GOLD + "Set Icon",
					ChatColor.WHITE + "Click with an item", ChatColor.WHITE + "to set waypoint item");
			waypointItem = waypoint.getIcon().clone();
		} else {
			waypointIcon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			ItemMeta waypointMeta = waypointIcon.getItemMeta();
			if (waypointMeta != null) {
				waypointMeta.setDisplayName(ChatColor.RED + "Icon Item Unset");
				waypointMeta.setLore(Arrays.asList(ChatColor.WHITE + "Click with an item", ChatColor.WHITE + "to set waypoint item"));
			}
			waypointIcon.setItemMeta(waypointMeta);
			waypointItem = new ItemStack(Material.AIR);
		}
		addButton(new Button(waypointIcon, event -> {
			if (waypoint != null && event.getClick() == ClickType.MIDDLE && event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
				//noinspection deprecation - works fine due to inventory updates on cancellation
				event.setCursor(waypoint.getIcon());
				return;
			}

			ItemStack newItem = event.getCursor();
			if (newItem == null || newItem.getType() == Material.AIR) {
				return;
			}
			waypointIcon.setType(newItem.getType());
			waypointIcon.setAmount(newItem.getAmount());
			ItemMeta newMeta = newItem.getItemMeta();
			waypointIcon.setItemMeta(newMeta != null ? newMeta.clone() : null);
			ItemUtil.insertText(waypointIcon, ChatColor.GOLD + "Set Waypoint Icon",
					ChatColor.WHITE + "Click with an item", ChatColor.WHITE + "to set waypoint item");
			if (waypoint != null) {
				waypoint.setIcon(newItem);
			} else {
				waypointItem.setType(newItem.getType());
				waypointItem.setAmount(newItem.getAmount());
				waypointItem.setItemMeta(newMeta != null ? newMeta.clone() : null);
			}

			draw(event.getView().getTopInventory());
		}));

		// BUTTON: Set location
		if (waypoint != null) {
			addButton(new Button(ItemUtil.getItem(Material.ARMOR_STAND, ChatColor.GOLD + "Set Location",
					ChatColor.WHITE + "Middle click to set", ChatColor.WHITE + "to current position."), event -> {
				if (event.getClick() == ClickType.MIDDLE) {
					waypoint.setLocation(event.getWhoClicked().getLocation());
				}
			}));
		}

		// Server waypoint exclusives: discoverability and priority
		AtomicInteger priority;
		AtomicInteger range;
		AtomicBoolean defaultDiscovered;
		if (ServerWaypoint.class.isAssignableFrom(waypointClazz)) {
			ServerWaypoint serverWP = waypoint == null ? null : (ServerWaypoint) waypoint;

			// BUTTON: Set priority in list
			priority = new AtomicInteger(serverWP != null ? serverWP.getPriority() : 0);
			addButton(new IntegerButton(priority, Material.EMERALD, value -> {
				if (serverWP != null) {
					serverWP.setPriority(value.get());
				}
			}, "Priority"));

			// BUTTON: set range for discovery
			range = new AtomicInteger(serverWP != null ? serverWP.getRangeSquared() < 1 ? -1 : (int) Math.sqrt(serverWP.getRangeSquared()) : 10);
			addButton(new IntegerButton(range, 0, Integer.MAX_VALUE, Material.LEAD, value -> {
				if (serverWP != null) {
					serverWP.setRange(value.get());
				}
			}, "Discovery Range", ChatColor.GOLD + "0 = Disable discovery"));

			// BUTTON: Set default discovered
			defaultDiscovered = new AtomicBoolean(serverWP != null && serverWP.isDefault());
			addButton(new BooleanButton(defaultDiscovered, Material.GREEN_STAINED_GLASS_PANE,
					Material.RED_STAINED_GLASS_PANE, value -> {
				if (serverWP != null) {
					serverWP.setDefault(value.get());
				}
			},"Always Discovered"));
		} else {
			priority = null;
			defaultDiscovered = null;
			range = null;
		}

		// BUTTON: Finalize waypoint
		setButton(8, new Button(() -> {
			ItemStack finalizeItem = new ItemStack(Material.END_CRYSTAL);
			ItemMeta finalizeItemMeta = finalizeItem.getItemMeta();
			if (finalizeItemMeta != null) {
				finalizeItemMeta.setDisplayName(ChatColor.GREEN + "Finish " + (waypoint != null ? "Editing" : "Creation"));
				if (waypoint == null && waypointItem.getType() == Material.AIR) {
					finalizeItemMeta.setLore(Collections.singletonList(ChatColor.RED + "Waypoint item not set!"));
				}
			}
			finalizeItem.setItemMeta(finalizeItemMeta);
			return finalizeItem;
		}, event -> {
			if (waypoint != null) {
				if (!(event.getWhoClicked() instanceof Player)) {
					event.getWhoClicked().closeInventory();
					return;
				}
				Player player = (Player) event.getWhoClicked();
				SimpleUI ui = UserWaypoint.class.isAssignableFrom(waypointClazz) ? new UserWaypointEditor(plugin, owner, player)
						: new ServerWaypointEditor(plugin, owner, player);
				player.openInventory(ui.getInventory());
				return;
			}
			if (waypointItem.getType() != Material.AIR && event.getWhoClicked() instanceof Player) {
				event.getWhoClicked().closeInventory();
				requestWaypointName(plugin, (Player) event.getWhoClicked(), waypointItem, priority, range, defaultDiscovered);
			}
		}));
	}

	private static void requestWaypointName(@NotNull AdventureLogPlugin plugin, @NotNull Player player, @NotNull ItemStack waypointItem,
			@Nullable AtomicInteger priority, @Nullable AtomicInteger range, @Nullable AtomicBoolean defaultDiscovered) {
		Conversation conversation = new ConversationFactory(plugin).withLocalEcho(false).withModality(false).withFirstPrompt(new Prompt() {
			@NotNull
			@Override
			public String getPromptText(@NotNull ConversationContext context) {
				return ChatColor.DARK_AQUA + "Enter name. Valid characters: "
						+ ChatColor.GOLD + "a" + ChatColor.WHITE + "-" + ChatColor.GOLD + "z" + ChatColor.WHITE + ", "
						+ ChatColor.GOLD + "_" + ChatColor.WHITE + ", "
						+ ChatColor.GOLD + "0" + ChatColor.WHITE + "-" + ChatColor.GOLD + "0";
			}

			@Override
			public boolean blocksForInput(@NotNull ConversationContext context) {
				return true;
			}

			@Nullable
			@Override
			public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
				if (input == null || !input.matches("[a-z_0-9]+")) {
					return this;
				}
				if (plugin.getDataManager().getServerData().getWaypoint(input) != null) {
					player.sendMessage(ChatColor.RED + "A waypoint by the name \"" + ChatColor.AQUA + input + ChatColor.RED + "\" already exists! Please edit it instead.");
					return Prompt.END_OF_CONVERSATION;
				}

				ServerWaypoint waypoint = plugin.getDataManager().getServerData().addWaypoint(input, waypointItem, player.getLocation());
				if (priority != null) {
					waypoint.setPriority(priority.get());
				}
				if (range != null) {
					waypoint.setRange(range.get());
				}
				if (defaultDiscovered != null) {
					waypoint.setDefault(defaultDiscovered.get());
				}
				player.sendTitle("",ChatColor.GREEN + "Waypoint created successfully!", 10, 50, 20);
				return Prompt.END_OF_CONVERSATION;
			}
		}).buildConversation(player);

		player.sendTitle(ChatColor.DARK_AQUA + "Enter Waypoint Name", ChatColor.RED + "Valid characters: "
				+ ChatColor.AQUA + "a" + ChatColor.RED + "-" + ChatColor.AQUA + "z" + ChatColor.RED + ", "
				+ ChatColor.AQUA + "_" + ChatColor.RED + ", "
				+ ChatColor.AQUA + "0" + ChatColor.RED + "-" + ChatColor.AQUA + "0", 10, 50, 20);

		player.beginConversation(conversation);
	}

	public static Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull Class<? extends Waypoint> waypointClazz,
			@Nullable Waypoint waypoint, @NotNull UUID owner) {
		return new Button(() -> getIcon(plugin, waypointClazz, waypoint, owner), event -> {
			ItemStack clicked = event.getCurrentItem();
			if (clicked == null || clicked.getType().isAir()) {
				return;
			}

			if (waypoint == null) {

				if (!(event.getWhoClicked() instanceof Player)) {
					event.getWhoClicked().closeInventory();
					return;
				}

				Player player = (Player) event.getWhoClicked();

				if (waypointClazz.equals(UserWaypoint.class)) {
					UserWaypoint created = plugin.getDataManager().getUserData(owner).createWaypoint(event.getWhoClicked().getLocation());
					player.openInventory(new IndividualWaypointEditor(plugin, UserWaypoint.class, created, owner).getInventory());
					return;
				}

				event.getWhoClicked().openInventory(new IndividualWaypointEditor(plugin, ServerWaypoint.class, null, owner).getInventory());
				return;
			}

			if (waypoint.isInvalid()) {
				clicked.setType(Material.AIR);
				return;
			}

			// Action: Delete waypoint
			if (event.getClick() == ClickType.CONTROL_DROP) {
				waypoint.delete();
				clicked.setType(Material.AIR);

				if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
					((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
				}
				return;
			}

			// Action: Toggle unlock
			if (event.getClick() == ClickType.SHIFT_RIGHT && waypoint instanceof ServerWaypoint) {
				UserData userData = plugin.getDataManager().getUserData(owner);
				List<String> unlocked = userData.getUnlocked();
				if (unlocked.contains(waypoint.getName())) {
					unlocked.remove(waypoint.getName());
				} else {
					unlocked.add(waypoint.getName());
				}
				userData.setUnlocked(unlocked);

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

			event.getWhoClicked().openInventory(new IndividualWaypointEditor(plugin, waypointClazz, waypoint, owner).getInventory());
		});
	}

	private static ItemStack getIcon(@NotNull AdventureLogPlugin plugin,
			@NotNull Class<? extends Waypoint> waypointClazz, @Nullable Waypoint waypoint, @NotNull UUID owner) {
		if (waypoint == null) {
			if (UserWaypoint.class.equals(waypointClazz)) {
				return ItemUtil.getItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "Create New Waypoint");
			}

			return ItemUtil.getItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "New Waypoint Editor");
		}

		if (waypoint.isInvalid()) {
			// Waypoint has been deleted.
			return new ItemStack(Material.AIR);
		}

		List<String> list = new ArrayList<>();
		list.add(ChatColor.WHITE + "Edit waypoint: " + ChatColor.GOLD + waypoint.getName());
		Location location = waypoint.getLocation();
		list.add(ChatColor.WHITE + "Location: " + ChatColor.GOLD + String.format("%s: %sx, %sy, %sz",
				location.getWorld() == null ? "invalid_world" : location.getWorld().getName(),
				location.getBlockX(), location.getBlockY(), location.getBlockZ()));
		if (waypoint instanceof ServerWaypoint) {
			ServerWaypoint serverWaypoint = (ServerWaypoint) waypoint;
			list.add(ChatColor.WHITE + "Priority: " + ChatColor.GOLD + serverWaypoint.getPriority());
			list.add(ChatColor.WHITE + "Discovery range: " + ChatColor.GOLD
					+ (serverWaypoint.getRangeSquared() < 1 ? -1 : (int) Math.sqrt(serverWaypoint.getRangeSquared())));
			list.add(ChatColor.WHITE + "Always Discovered: " + ChatColor.GOLD + serverWaypoint.isDefault());
			list.add("");
			list.add(ChatColor.WHITE + "Unlocked for " + getName(owner) + ": " + ChatColor.GOLD
					+ plugin.getDataManager().getUserData(owner).getUnlocked().contains(waypoint.getName()));
			list.add(ChatColor.GOLD + "  (Shift + Rclick to toggle)");
		}
		list.add("");
		list.add(ChatColor.RED + "Drop stack to delete.");
		list.add(ChatColor.RED + "  (Default Ctrl+Q)");
		return ItemUtil.insertText(waypoint.getIcon().clone(), list);
	}

	private static String getName(UUID uuid) {
		String name = Bukkit.getOfflinePlayer(uuid).getName();
		return name == null ? uuid.toString() : name;
	}

}
