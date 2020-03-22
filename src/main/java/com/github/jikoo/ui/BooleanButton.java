package com.github.jikoo.ui;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BooleanButton extends Button {

	private final AtomicBoolean value;
	private final Material materialTrue, materialFalse;
	private final String name;

	public BooleanButton(AtomicBoolean value, Material materialTrue, Material materialFalse, String name) {
		super(getItem(value, materialTrue, materialFalse, name), event -> {
			value.set(!value.get());

			if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
				((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
			}
		});

		this.value = value;
		this.materialTrue = materialTrue;
		this.materialFalse = materialFalse;
		this.name = name;
	}

	@Override
	public ItemStack getItem() {
		ItemStack item = super.getItem();
		Material expectedType = value.get() ? materialTrue : materialFalse;

		if (item.getType() == expectedType) {
			return item;
		}

		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get());
		}
		item.setType(expectedType);
		item.setItemMeta(itemMeta);

		return item;
	}

	private static ItemStack getItem(AtomicBoolean value, Material typeOn, Material typeOff, String name) {
		ItemStack item = new ItemStack(value.get() ? typeOn : typeOff);
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get());
			itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "Click to toggle"));
		}
		item.setItemMeta(itemMeta);
		return item;
	}

}
