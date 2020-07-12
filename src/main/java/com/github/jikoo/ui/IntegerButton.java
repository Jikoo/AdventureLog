package com.github.jikoo.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class IntegerButton extends Button {

	private final AtomicInteger value;
	private final String name;

	public IntegerButton(AtomicInteger value, Material type, String name, String... additionalInfo) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, type, name, additionalInfo);
	}

	public IntegerButton(AtomicInteger value, int minValue, int maxValue, Material type, String name, String... additionalInfo) {
		super(() -> getItem(value, type, name, additionalInfo), event -> {
			int diff;
			switch (event.getClick()) {
				case LEFT:
					diff = 1;
					break;
				case DOUBLE_CLICK: // In case of click spamming, still do something
					diff = 2;
					break;
				case SHIFT_LEFT:
					diff = 10;
					break;
				case RIGHT:
					diff = -1;
					break;
				case SHIFT_RIGHT:
					diff = -10;
					break;
				case DROP:
					diff = 100;
					break;
				case CONTROL_DROP:
					diff = -100;
					break;
				default:
					diff = 0;
					break;
			}
			int newValue = value.addAndGet(diff);
			if (newValue < minValue) {
				value.set(minValue);
			} else if (newValue >= maxValue) {
				value.set(maxValue);
			}

			if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
				((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
			}
		});
		this.name = name;
		this.value = value;
	}

	@Override
	public @NotNull ItemStack getItem() {
		ItemStack item = super.getItem();
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta != null) {
			String displayName = ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get();
			if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(displayName)) {
				return item;
			}
			itemMeta.setDisplayName(displayName);
			item.setItemMeta(itemMeta);
		}
		return item;
	}

	private static ItemStack getItem(AtomicInteger value, Material type, String name, String... additionalInfo) {
		ItemStack item = new ItemStack(type);
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get());
			List<String> lore = new ArrayList<>();
			lore.add(ChatColor.WHITE + "Left click: " + ChatColor.GOLD + "+1");
			lore.add(ChatColor.WHITE + "Right click: " + ChatColor.GOLD + "-1");
			lore.add(ChatColor.WHITE + "Shift+click: " + ChatColor.GOLD + "±10");
			lore.add(ChatColor.WHITE + "Drop (default Q): " + ChatColor.GOLD + "+100");
			lore.add(ChatColor.WHITE + "Drop stack (ctrl+Q): " + ChatColor.GOLD + "-100");
			Collections.addAll(lore, additionalInfo);
			itemMeta.setLore(lore);
		}
		item.setItemMeta(itemMeta);
		return item;
	}

}
