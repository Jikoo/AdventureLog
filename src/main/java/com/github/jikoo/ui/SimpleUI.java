package com.github.jikoo.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SimpleUI implements InventoryHolder {

	private String name;
	private final TreeMap<Integer, Button> buttons = new TreeMap<>();
	private final Map<Integer, Button> navigation = new HashMap<>();
	private int startIndex = 0;

	public SimpleUI(String name) {
		this.name = name;
	}

	public void handleClick(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		Button button;
		if (slot > 44) {
			button = navigation.get(slot);
		} else {
			button = buttons.get(slot + startIndex);
		}
		if (button != null) {
			button.getConsumer().accept(event);
		}
	}

	public void addButton(Button button) {
		setButton(getHighestButton() + 1, button);
	}

	public void setButton(int slot, Button button) {
		buttons.put(slot, button);
	}

	public void removeButton(int slot) {
		buttons.remove(slot);
	}

	public Button getButton(int slot) {
		return buttons.get(slot);
	}

	public int getHighestButton() {
		return buttons.size() > 0 ? buttons.lastKey() : 0;
	}

	@Override
	public @NotNull
	Inventory getInventory() {
		int highestIndex = buttons.size() > 0 ? buttons.lastKey() : 0;
		int requiredSize = Math.max(54, Math.max(9, (int) Math.ceil(highestIndex / 9D) * 9));
		Inventory inventory = Bukkit.createInventory(this, requiredSize > 54 ? 54 : requiredSize, name);
		draw(inventory);
		return inventory;
	}

	public void draw(Inventory inventory) {
		ItemStack[] contents = inventory.getContents();
		Arrays.fill(contents, new ItemStack(Material.AIR));
		int endIndex = startIndex + (contents.length == 54 ? contents.length - 9 : contents.length);
		SortedMap<Integer, Button> sortedMap = buttons.subMap(startIndex, endIndex);

		sortedMap.forEach((index, button) -> contents[index - startIndex] = button.getItem());

		if (contents.length == 54) {
			// First page button
			navigation.put(45, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				Consumer<InventoryClickEvent> consumer;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.SPECTRAL_ARROW);
					consumer = event -> {
						startIndex = 0;
						draw(event.getView().getTopInventory());
					};
				} else {
					itemStack = new ItemStack(Material.BARRIER);
					consumer = event -> {
					};
				}
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta != null) {
					itemMeta.setDisplayName(ChatColor.WHITE + "First Page");
					itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  1/" + maxPage));
					itemStack.setItemMeta(itemMeta);
				}
				return new Button(itemStack, consumer);
			}).get());

			// Previous page button
			navigation.put(46, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.ARROW);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta != null) {
						itemMeta.setDisplayName(ChatColor.WHITE + "Previous Page");
						itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  " + (startIndex / 45) + '/' + maxPage));
						itemStack.setItemMeta(itemMeta);
					}
					return new Button(itemStack, event -> {
						startIndex -= 45;
						draw(event.getView().getTopInventory());
					});
				} else {
					itemStack = new ItemStack(Material.BARRIER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta != null) {
						itemMeta.setDisplayName(ChatColor.WHITE + "First Page");
						itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  1/" + maxPage));
						itemStack.setItemMeta(itemMeta);
					}
					return new Button(itemStack, event -> {});
				}
			}).get());

			// Next page button
			navigation.put(52, ((Supplier<Button>) () -> {
				int highestCurrentButton = startIndex + 44;
				int highestRequiredButton = getHighestButton();
				int maxPage = (int) Math.ceil(highestRequiredButton / 45D);
				ItemStack itemStack;
				if (highestCurrentButton > highestRequiredButton) {
					itemStack = new ItemStack(Material.ARROW);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta != null) {
						itemMeta.setDisplayName(ChatColor.WHITE + "Next Page");
						itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  " + (startIndex / 45 + 2) + '/' + maxPage));
						itemStack.setItemMeta(itemMeta);
					}
					return new Button(itemStack, event -> {
						startIndex += 45;
						draw(event.getView().getTopInventory());
					});
				} else {
					itemStack = new ItemStack(Material.BARRIER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta != null) {
						itemMeta.setDisplayName(ChatColor.WHITE + "Last Page");
						itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  " + maxPage + '/' + maxPage));
						itemStack.setItemMeta(itemMeta);
					}
					return new Button(itemStack, event -> {});
				}
			}).get());

			// Last page button
			navigation.put(53, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				Consumer<InventoryClickEvent> consumer;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.SPECTRAL_ARROW);
					consumer = event -> {
						startIndex = 45 * maxPage - 45;
						draw(event.getView().getTopInventory());
					};
				} else {
					itemStack = new ItemStack(Material.BARRIER);
					consumer = event -> {
					};
				}
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta != null) {
					itemMeta.setDisplayName(ChatColor.WHITE + "Last Page");
					itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  " + maxPage + '/' + maxPage));
					itemStack.setItemMeta(itemMeta);
				}
				return new Button(itemStack, consumer);
			}).get());

			navigation.forEach((slot, button) -> contents[slot] = button.getItem());
		}

		inventory.setContents(contents);
	}

}
