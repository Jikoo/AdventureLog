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
import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IndividualWaypointEditor extends SimpleUI {

	public IndividualWaypointEditor(
			@NotNull AdventureLogPlugin plugin,
			@NotNull Class<? extends Waypoint> waypointClazz,
			@Nullable Waypoint waypoint,
			@NotNull UUID owner) {
		super(Component.text(waypoint != null ? "Modify " + waypoint.getName() : "New Waypoint Creator").color(NamedTextColor.DARK_RED), false);

		// BUTTON: set icon item
		ItemStack waypointIcon;
		ItemStack waypointItem;
		if (waypoint != null) {
			waypointIcon = waypoint.getIcon().clone();
			ItemUtil.insertText(
					waypointIcon,
					Component.text("Set Icon").color(NamedTextColor.GOLD),
					Component.text("Click with an item").color(NamedTextColor.WHITE),
					Component.text("to set waypoint item").color(NamedTextColor.WHITE));
			waypointItem = waypoint.getIcon().clone();
		} else {
			waypointIcon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			ItemMeta waypointMeta = waypointIcon.getItemMeta();
			if (waypointMeta != null) {
				waypointMeta.displayName(Component.text("Icon Item Unset").color(NamedTextColor.RED));
				waypointMeta.lore(
						List.of(
								Component.text("Click with an item").color(NamedTextColor.WHITE),
								Component.text("to set waypoint item").color(NamedTextColor.WHITE)));
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
			ItemUtil.insertText(
					waypointIcon,
					Component.text("Set Waypoint Icon").color(NamedTextColor.GOLD),
					Component.text("Click with an item").color(NamedTextColor.WHITE),
					Component.text("to set waypoint item").color(NamedTextColor.WHITE));
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
			addButton(new Button(() -> ItemUtil.getItem(
					Material.ARMOR_STAND,
					Component.text("Set Location").color(NamedTextColor.GOLD),
					Component.text("Right click to set").color(NamedTextColor.WHITE),
					Component.text("to current position.").color(NamedTextColor.WHITE),
					Component.text(""),
					Component.text().append(
							Component.text("Current: ").color(NamedTextColor.WHITE),
							Component.text(TextUtil.getDisplay(waypoint.getLocation())).color(NamedTextColor.GOLD)).build()),
					event -> {
						if (event.getClick() == ClickType.RIGHT) {
							waypoint.setLocation(event.getWhoClicked().getLocation());
							draw(event.getView().getTopInventory());
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
			}, "Discovery Range", Component.text("0 = Disable discovery").color(NamedTextColor.GOLD)));

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
				finalizeItemMeta.displayName(Component.text("Finish " + (waypoint != null ? "Editing" : "Creation")).color(NamedTextColor.GREEN));
				if (waypoint == null && waypointItem.getType() == Material.AIR) {
					finalizeItemMeta.lore(List.of(Component.text("Waypoint item not set!").color(NamedTextColor.RED)));
				}
			}
			finalizeItem.setItemMeta(finalizeItemMeta);
			return finalizeItem;
		}, event -> {
			if (waypoint != null) {
				if (!(event.getWhoClicked() instanceof Player player)) {
					event.getWhoClicked().closeInventory();
					return;
				}
				SimpleUI ui = UserWaypoint.class.isAssignableFrom(waypointClazz)
						? new UserWaypointEditor(plugin, owner, player)
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
				return LegacyComponentSerializer.legacySection().serialize(
						Component.text()
								.append(getValidChars("Enter name. ", NamedTextColor.DARK_AQUA, NamedTextColor.GOLD))
								.build());
			}

			@Override
			public boolean blocksForInput(@NotNull ConversationContext context) {
				return true;
			}

			@Nullable
			@Override
			public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
				if (input == null || !input.matches("[a-z_\\d]+")) {
					return this;
				}
				if (plugin.getDataManager().getServerData().getWaypoint(input) != null) {
					player.sendMessage(
							Component.text().color(NamedTextColor.RED).append(
									Component.text("A waypoint by the name \""),
									Component.text(input).color(NamedTextColor.AQUA),
									Component.text("\" already exists! Please edit it instead."))
									.build());
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
				player.showTitle(
						Title.title(
								Component.text("Waypoint created successfully!").color(NamedTextColor.GREEN),
								Component.text(""),
								Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(2_500L), Duration.ofMillis(1_000L))));
				return Prompt.END_OF_CONVERSATION;
			}
		}).buildConversation(player);

		player.showTitle(
				Title.title(
						Component.text("Enter Waypoint Name").color(NamedTextColor.DARK_AQUA),
						getValidChars(null, NamedTextColor.RED, NamedTextColor.AQUA),
						Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(2_500L), Duration.ofMillis(1_000L))));

		player.beginConversation(conversation);
	}

	private static @NotNull Component getValidChars(
			@Nullable String prefix,
			@NotNull TextColor primary,
			@NotNull TextColor secondary) {
		String validChars = "Valid characters: ";
		if (prefix != null) {
			validChars = prefix + validChars;
		}
		Component hyphen = Component.text('-');
		Component separator = Component.text(", ");
		return Component.text().color(primary)
				.append(
						Component.text(validChars),
						Component.text('a').color(secondary),
						hyphen,
						Component.text('z').color(secondary),
						separator,
						Component.text('_'),
						separator,
						Component.text('0').color(secondary),
						hyphen,
						Component.text('9').color(secondary))
				.build();
	}

	@Contract(value = "_, _, _, _ -> new", pure = true)
	public static @NotNull Button getButton(@NotNull AdventureLogPlugin plugin, @NotNull Class<? extends Waypoint> waypointClazz,
			@Nullable Waypoint waypoint, @NotNull UUID owner) {
		return new Button(() -> getIcon(plugin, waypointClazz, waypoint, owner), event -> {
			ItemStack clicked = event.getCurrentItem();
			if (clicked == null || clicked.getType().isAir()) {
				return;
			}

			if (waypoint == null) {

				if (!(event.getWhoClicked() instanceof Player player)) {
					event.getWhoClicked().closeInventory();
					return;
				}

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
			if (event.getClick() == ClickType.DROP) {
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

	private static @NotNull ItemStack getIcon(
			@NotNull AdventureLogPlugin plugin,
			@NotNull Class<? extends Waypoint> waypointClazz,
			@Nullable Waypoint waypoint,
			@NotNull UUID owner) {
		if (waypoint == null) {
			if (UserWaypoint.class.equals(waypointClazz)) {
				return ItemUtil.getItem(Material.WRITABLE_BOOK, Component.text("Create New Waypoint").color(NamedTextColor.GOLD));
			}

			return ItemUtil.getItem(Material.WRITABLE_BOOK, Component.text("New Waypoint Editor").color(NamedTextColor.GOLD));
		}

		if (waypoint.isInvalid()) {
			// Waypoint has been deleted.
			return new ItemStack(Material.AIR);
		}

		List<Component> list = new ArrayList<>();
		list.add(Component.text()
				.append(
						Component.text("Edit waypoint: ").color(NamedTextColor.WHITE),
						Component.text(waypoint.getName()).color(NamedTextColor.GOLD))
				.build());
		list.add(Component.text(TextUtil.getDisplay(waypoint.getLocation())).color(NamedTextColor.GOLD));
		if (waypoint instanceof ServerWaypoint serverWaypoint) {
			list.add(Component.text()
					.append(
							Component.text("Priority: ").color(NamedTextColor.WHITE),
							Component.text(serverWaypoint.getPriority()).color(NamedTextColor.GOLD))
					.build());
			list.add(Component.text()
					.append(
							Component.text("Discovery range: ").color(NamedTextColor.WHITE),
							Component.text(serverWaypoint.getRangeSquared() < 1 ? -1 : (int) Math.sqrt(serverWaypoint.getRangeSquared()))
									.color(NamedTextColor.GOLD))
					.build());
			list.add(Component.text()
					.append(
							Component.text("Always Discovered: ").color(NamedTextColor.WHITE),
							Component.text(serverWaypoint.isDefault()).color(NamedTextColor.GOLD))
					.build());
			list.add(Component.text(""));
			list.add(Component.text()
					.append(
							Component.text("Unlocked for " + getName(owner) + ": ").color(NamedTextColor.WHITE),
							Component.text(plugin.getDataManager().getUserData(owner).getUnlocked().contains(waypoint.getName()))
									.color(NamedTextColor.GOLD))
					.build());
			list.add(Component.text("  (Shift + right click to toggle)").color(NamedTextColor.GOLD));
		}
		list.add(Component.text(""));
		list.add(Component.text().color(NamedTextColor.RED)
				.append(
						Component.text("Drop to delete ("),
						Component.translatable("key.drop"),
						Component.text(')'))
				.build());
		return ItemUtil.insertText(waypoint.getIcon().clone(), list);
	}

	private static String getName(UUID uuid) {
		String name = Bukkit.getOfflinePlayer(uuid).getName();
		return name == null ? uuid.toString() : name;
	}

}
