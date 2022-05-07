package com.github.jikoo.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemUtil {

	public static ItemStack getItem(Material material, String... text) {
		return getItem(material, Arrays.asList(text));
	}

	public static ItemStack getItem(Material material, List<String> text) {
		ItemStack itemStack = new ItemStack(material);
		return appendText(itemStack, text);
	}

	public static ItemStack insertText(ItemStack itemStack, String... text) {
		return insertText(itemStack, Arrays.asList(text));
	}

	public static ItemStack insertText(ItemStack itemStack, List<String> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
			List<String> oldLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
			itemMeta.setDisplayName(text.get(0));
			List<String> lore = new ArrayList<>(text.subList(1, text.size()));
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
		return itemStack;
	}

	public static ItemStack appendText(ItemStack itemStack, String... text) {
		return appendText(itemStack, Arrays.asList(text));
	}

	public static ItemStack appendText(ItemStack itemStack, List<String> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			if (!itemMeta.hasDisplayName() && !itemMeta.hasLore()) {
				itemMeta.setDisplayName(text.get(0));
				itemMeta.setLore(text.subList(1, text.size()));
			} else {
				ArrayList<String> lore = new ArrayList<>();
				if (itemMeta.hasLore()) {
					lore.addAll(itemMeta.getLore());
				}
				lore.add("");
				lore.addAll(text);
				itemMeta.setLore(lore);
			}
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}


	private ItemUtil() {}

}
