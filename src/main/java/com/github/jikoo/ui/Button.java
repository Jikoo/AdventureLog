package com.github.jikoo.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Button {

	private static Button empty;

	public static Button empty() {
		if (empty == null) {
			empty = new Button(new ItemStack(Material.AIR), event -> {});
		}
		return empty;
	}

	private final Supplier<ItemStack> item;
	private final Consumer<InventoryClickEvent> consumer;

	public Button(@NotNull ItemStack item, @NotNull Consumer<InventoryClickEvent> consumer) {
		this.item = () -> item;
		this.consumer = consumer;
	}

	public Button(@NotNull Supplier<ItemStack> item, @NotNull Consumer<InventoryClickEvent> consumer) {
		this.item = item;
		this.consumer = consumer;
	}

	public @NotNull ItemStack getItem(){
		return item.get();
	}

	public @NotNull Consumer<InventoryClickEvent> getConsumer(){
		return consumer;
	}

	public static ItemStack createIcon(@NotNull Material material, String @NotNull ... lines) {
		return modifyIcon(new ItemStack(material), lines);
	}

	public static ItemStack modifyIcon(@NotNull ItemStack icon, String @NotNull ... lines) {
		if (lines.length == 0) {
			return icon;
		}
		ItemMeta itemMeta = icon.getItemMeta();
		if (itemMeta != null) {
			String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
			List<String> oldLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
			itemMeta.setDisplayName(lines[0]);
			List<String> lore = new ArrayList<>(Arrays.asList(lines).subList(1, lines.length));
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
		icon.setItemMeta(itemMeta);
		return icon;
	}

}
