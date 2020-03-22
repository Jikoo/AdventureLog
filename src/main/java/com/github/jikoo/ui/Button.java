package com.github.jikoo.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Button {

	private static Button empty;

	public static Button empty() {
		if (empty == null) {
			empty = new Button(new ItemStack(Material.AIR), event -> {});
		}
		return empty;
	}

	private Supplier<ItemStack> item;
	private Consumer<InventoryClickEvent> consumer;

	public Button(ItemStack item, Consumer<InventoryClickEvent> consumer) {
		this.item = () -> item;
		this.consumer = consumer;
	}

	public Button(Supplier<ItemStack> item, Consumer<InventoryClickEvent> consumer) {
		this.item = item;
		this.consumer = consumer;
	}

	public ItemStack getItem(){
		return item.get();
	}

	public Consumer<InventoryClickEvent> getConsumer(){
		return consumer;
	}

}
