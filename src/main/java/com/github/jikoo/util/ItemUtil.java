package com.github.jikoo.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {

	public static ItemStack getItem(Material material, Component... text) {
		return getItem(material, List.of(text));
	}

	public static ItemStack getItem(Material material, List<Component> text) {
		ItemStack itemStack = new ItemStack(material);
		return appendText(itemStack, text);
	}

	public static ItemStack insertText(ItemStack itemStack, Component... text) {
		return insertText(itemStack, List.of(text));
	}

	public static ItemStack insertText(ItemStack itemStack, List<Component> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			Component displayName = itemMeta.hasDisplayName() ? itemMeta.displayName() : null;
			List<Component> oldLore = itemMeta.hasLore() ? itemMeta.lore() : null;
			itemMeta.displayName(text.get(0));
			List<Component> lore = new ArrayList<>(text.subList(1, text.size()));
			if (displayName != null || oldLore != null) {
				lore.add(Component.text(""));
			}
			if (displayName != null) {
				lore.add(displayName);
			}
			if (oldLore != null) {
				lore.addAll(oldLore);
			}
			itemMeta.lore(lore);
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack appendText(ItemStack itemStack, Component... text) {
		return appendText(itemStack, List.of(text));
	}

	public static ItemStack appendText(ItemStack itemStack, List<Component> text) {
		if (text.isEmpty()) {
			return itemStack;
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			if (!itemMeta.hasDisplayName() && !itemMeta.hasLore()) {
				itemMeta.displayName(text.get(0));
				itemMeta.lore(text.subList(1, text.size()));
			} else {
				ArrayList<Component> lore = new ArrayList<>();
				if (itemMeta.hasLore()) {
					lore.addAll(itemMeta.lore());
				}
				lore.add(Component.text(""));
				lore.addAll(text);
				itemMeta.lore(lore);
			}
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}


	private ItemUtil() {}

}
