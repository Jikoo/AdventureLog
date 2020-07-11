package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.Waypoint;
import com.github.jikoo.ui.BooleanButton;
import com.github.jikoo.ui.Button;
import com.github.jikoo.ui.IntegerButton;
import com.github.jikoo.ui.SimpleUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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

public class ManageWaypointsCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public ManageWaypointsCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Waypoints can only be edited by a player!");
			return true;
		}

		openEditor((Player) sender);

		return true;
	}

	private void openEditor(Player player) {
		SimpleUI ui = new SimpleUI(ChatColor.DARK_RED + "Adventure Log Editor", false);

		plugin.getDataStore().getWaypoints().forEach(waypoint -> {
			Button button = createOrEdit(waypoint);
			ui.addButton(button);
		});

		ui.setNavButton(2, createOrEdit(null));

		player.openInventory(ui.getInventory());
	}

	private Button createOrEdit(@Nullable Waypoint waypoint) {
		ItemStack itemStack;
		if (waypoint != null) {
			if (plugin.getDataStore().getServerWaypoint(waypoint.getName()) == null) {
				// Waypoint has been deleted.
				return Button.empty();
			}

			itemStack = waypoint.getIcon().clone();
			Location location = waypoint.getLocation();
			modifyIcon(itemStack, ChatColor.WHITE + "Edit waypoint: " + ChatColor.GOLD + waypoint.getName(),
					ChatColor.WHITE + "Location: " + ChatColor.GOLD + String.format("%s: %sx, %sy, %sz",
							location.getWorld() == null ? "invalid_world" : location.getWorld().getName(),
							location.getBlockX(), location.getBlockY(), location.getBlockZ()),
					ChatColor.WHITE + "Priority: " + ChatColor.GOLD + waypoint.getPriority(),
					ChatColor.WHITE + "Discovery range: " + ChatColor.GOLD + (!(waypoint instanceof ServerWaypoint) ? -1 : ((ServerWaypoint) waypoint).getRangeSquared() < 1 ? -1 : (int) Math.sqrt(((ServerWaypoint) waypoint).getRangeSquared())),
					ChatColor.WHITE + "Always Discovered: " + ChatColor.GOLD + (!(waypoint instanceof ServerWaypoint) || ((ServerWaypoint) waypoint).isDefault()),
					"", ChatColor.RED + "Ctrl+drop to delete.");
		} else {
			itemStack = new ItemStack(Material.WRITABLE_BOOK);
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta != null) {
				itemMeta.setDisplayName(ChatColor.GOLD + "New Waypoint Editor");
			}
			itemStack.setItemMeta(itemMeta);
		}

		return new Button(itemStack, event -> {
			// Ignore deleted waypoints
			if (itemStack.getType() == Material.AIR) {
				return;
			}

			if (waypoint != null) {
				// Delete waypoint on ctrl+drop
				if (event.getClick() == ClickType.CONTROL_DROP) {
					plugin.getDataStore().removeWaypoint(waypoint.getName());
					itemStack.setType(Material.AIR);

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

			SimpleUI ui = new SimpleUI(ChatColor.DARK_RED + (waypoint != null ? "Waypoint Editor: " + waypoint.getName() : "New Waypoint Creator"), false);

			// BUTTON: set icon item
			ItemStack waypointIcon;
			ItemStack waypointItem;
			if (waypoint != null) {
				waypointIcon = waypoint.getIcon().clone();
				modifyIcon(waypointIcon, ChatColor.GOLD + "Set Icon",
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
			ui.addButton(new Button(waypointIcon, event1 -> {
				if (waypoint != null && event1.getClick() == ClickType.MIDDLE && event1.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
					//noinspection deprecation - works fine due to inventory updates on cancellation
					event1.setCursor(waypoint.getIcon());
					return;
				}

				ItemStack newItem = event1.getCursor();
				if (newItem == null || newItem.getType() == Material.AIR) {
					return;
				}
				waypointIcon.setType(newItem.getType());
				waypointIcon.setAmount(newItem.getAmount());
				ItemMeta newMeta = newItem.getItemMeta();
				waypointIcon.setItemMeta(newMeta != null ? newMeta.clone() : null);
				modifyIcon(waypointIcon, ChatColor.GOLD + "Set Waypoint Icon",
						ChatColor.WHITE + "Click with an item", ChatColor.WHITE + "to set waypoint item");
				if (waypoint != null) {
					waypoint.setIcon(newItem.clone());
				} else {
					waypointItem.setType(newItem.getType());
					waypointItem.setAmount(newItem.getAmount());
					waypointItem.setItemMeta(newMeta != null ? newMeta.clone() : null);
				}

				ui.draw(event1.getView().getTopInventory());
			}));

			// BUTTON: Set location
			if (waypoint != null) {
				ItemStack locationItem = new ItemStack(Material.ARMOR_STAND);
				ItemMeta locationMeta = locationItem.getItemMeta();
				if (locationMeta != null) {
					locationMeta.setDisplayName(ChatColor.GOLD + "Set Location");
					locationMeta.setLore(Arrays.asList(ChatColor.WHITE + "Middle click to set", ChatColor.WHITE + "to current position."));
				}
				locationItem.setItemMeta(locationMeta);
				ui.addButton(new Button(locationItem, event1 -> {
					if (event1.getClick() == ClickType.MIDDLE) {
						waypoint.setLocation(event1.getWhoClicked().getLocation());
					}
				}));
			}

			// BUTTON: Set priority in list
			AtomicInteger priority = new AtomicInteger(waypoint != null ? waypoint.getPriority() : 0);
			ui.addButton(new IntegerButton(priority, Material.EMERALD, "Priority"));

			AtomicBoolean defaultDiscovered;
			AtomicInteger range;
			if (waypoint instanceof ServerWaypoint) {
				ServerWaypoint serverWaypoint = (ServerWaypoint) waypoint;
				range = new AtomicInteger(serverWaypoint.getRangeSquared() < 1 ? -1 : (int) Math.sqrt(serverWaypoint.getRangeSquared()));
				ui.addButton(new IntegerButton(range, -1, Integer.MAX_VALUE, Material.LEAD, "Discovery Range", ChatColor.GOLD + "-1 = Disable discovery"));

				// BUTTON: Set default discovered
				defaultDiscovered = new AtomicBoolean(serverWaypoint.isDefault());
				ui.addButton(new BooleanButton(defaultDiscovered, Material.GREEN_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, "Always Discovered"));
			} else {
				defaultDiscovered = null;
				range = null;
			}

			// BUTTON: Finalize waypoint
			ui.setButton(8, new Button(() -> {
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
			}, event1 -> {
				if (waypoint != null && event1.getWhoClicked() instanceof Player) {
					openEditor((Player) event1.getWhoClicked());
					// TODO move into buttons?
					waypoint.setPriority(priority.get());
					if (waypoint instanceof ServerWaypoint) {
						ServerWaypoint serverWaypoint = (ServerWaypoint) waypoint;
						serverWaypoint.setRange(range.get());
						serverWaypoint.setDefault(defaultDiscovered.get());
					}
					return;
				}
				if (waypointItem.getType() != Material.AIR && event1.getWhoClicked() instanceof Player) {
					event1.getWhoClicked().closeInventory();
					requestWaypointName((Player) event1.getWhoClicked(), waypointItem, priority.get(), range, defaultDiscovered);
				}
			}));

			event.getWhoClicked().openInventory(ui.getInventory());
		});
	}

	private void modifyIcon(ItemStack itemStack, String name, String... additionalInfo) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
			List<String> oldLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
			itemMeta.setDisplayName(name);
			List<String> lore = new ArrayList<>();
			Collections.addAll(lore, additionalInfo);
			if (displayName != null || oldLore != null) {
				lore.add("");
			}
			if (displayName != null) {
				lore.add(displayName);
			}
			if (oldLore != null) {
				lore.addAll(oldLore);
			}
			itemMeta.setLore(lore);
		}
		itemStack.setItemMeta(itemMeta);
	}

	private void requestWaypointName(Player player, ItemStack waypointItem, int priority,
			@Nullable AtomicInteger range, @Nullable AtomicBoolean defaultDiscovered) {
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
				if (plugin.getDataStore().getServerWaypoint(input) != null) {
					player.sendMessage(ChatColor.RED + "A waypoint by the name \"" + ChatColor.AQUA + input + ChatColor.RED + "\" already exists! Please edit it instead.");
					return Prompt.END_OF_CONVERSATION;
				}

				// TODO rework for individual waypoints
				ServerWaypoint waypoint = plugin.getDataStore().addServerWaypoint(input, waypointItem, player.getLocation());
				waypoint.setPriority(priority);
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

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return Collections.emptyList();
	}

}
