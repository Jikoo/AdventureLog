package com.github.jikoo.ui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

}
