package com.github.jikoo.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class TextUtil {

	public static @NotNull String getDisplay(@NotNull World world) {
		String name = world.getName();
		if ("world".equals(name)) {
			return "Overworld";
		}
		if ("world_nether".equals(name)) {
			return "The Nether";
		}
		if ("world_the_end".equals(name)) {
			return "The End";
		}
		StringBuilder builder = new StringBuilder();
		for (String word : name.split("[_ ]")) {
			if (word.length() == 0) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return builder.toString();
	}

	public static @NotNull String getDisplay(@NotNull Location location) {
		return String.format("%s: %sx, %sy, %sz",
				location.getWorld() == null ? "Unloaded World" : getDisplay(location.getWorld()),
				location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static @NotNull TextComponent.Builder itemText() {
		return Component.text().decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
	}

	public static @NotNull TextComponent itemText(String content) {
		return Component.text(content).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
	}

	@Contract("_, _ -> new")
	public static @NotNull ItemStack getTextItem(@NotNull Material material, @NotNull Component @NotNull ... text) {
		return getTextItem(material, List.of(text));
	}

	@Contract("_, _ -> new")
	public static @NotNull ItemStack getTextItem(@NotNull Material material, @NotNull List<Component> text) {
		ItemStack itemStack = new ItemStack(material);
		return appendText(itemStack, text);
	}

	@Contract("_, _ -> param1")
	public static @NotNull ItemStack insertText(@NotNull ItemStack itemStack, @NotNull Component @NotNull ... text) {
		return insertText(itemStack, List.of(text));
	}

	@Contract("_, _ -> param1")
	public static @NotNull ItemStack insertText(ItemStack itemStack, @NotNull List<Component> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta == null) {
			return itemStack;
		}

		// Only use ItemStack display name if there is lore; it will use the default material name component if unset.
		// If there is no lore and no custom name, we do not want to display the vanilla text.
		Supplier<Component> displayName = () -> itemMeta.hasLore() ? itemStack.displayName() : itemMeta.displayName();
		List<Component> components = getItemText(itemMeta, displayName);

		List<Component> newText;
		if (components.isEmpty()) {
			// If there is nothing to insert in front of, copy list to a mutable list for setting text.
			newText = new ArrayList<>(text);
		} else {
			newText = new ArrayList<>(text.size() + components.size() + 1);
			newText.addAll(text);
			// Add a spacer for existing text.
			newText.add(Component.text(""));
			newText.addAll(components);
		}

		setItemText(itemMeta, newText, true);

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	@Contract("_, _ -> param1")
	public static @NotNull ItemStack appendText(@NotNull ItemStack itemStack, @NotNull Component @NotNull ... text) {
		return appendText(itemStack, List.of(text));
	}

	@Contract("_, _ -> param1")
	public static @NotNull ItemStack appendText(@NotNull ItemStack itemStack, @NotNull List<Component> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta == null) {
			return itemStack;
		}

		List<Component> components = getItemText(itemMeta, itemMeta::displayName);
		// As this is an append, only use display name if it is already set or there is no lore.
		// This prevents moving the first line of lore into the display name slot during merges.
		boolean useDisplayName = components.isEmpty() || itemMeta.hasDisplayName();
		if (!components.isEmpty()) {
			// If there are existing components, add a spacer.
			components.add(Component.text(""));
		}
		components.addAll(text);

		setItemText(itemMeta, components, useDisplayName);

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	private static @NotNull List<Component> getItemText(
			@NotNull ItemMeta itemMeta,
			@NotNull Supplier<@Nullable Component> displayName) {
		// Following the Bukkit model, ItemMeta#lore returns a mutable clone via PaperAdventure#asAdventureFromJson.
		// This is implementation-specific, but it is generally safe.
		List<Component> components = itemMeta.lore();
		if (components == null) {
			components = new ArrayList<>();
		}

		Component name = displayName.get();
		if (name != null) {
			components.add(0, name);
		}

		return components;
	}

	private static void setItemText(@NotNull ItemMeta itemMeta, @NotNull List<Component> components, boolean useName) {
		if (components.isEmpty()) {
			itemMeta.displayName(null);
			itemMeta.lore(null);
			return;
		}

		if (useName) {
			// Set display name to first component and remove.
			itemMeta.displayName(components.remove(0));
		}

		// Set lore to remaining components.
		if (components.isEmpty()) {
			itemMeta.lore(null);
		} else {
			itemMeta.lore(components);
		}
	}

	private TextUtil() {}

}
