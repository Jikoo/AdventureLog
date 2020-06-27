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
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleUI implements InventoryHolder {

	private final String name;
	private final boolean actionBlocking;
	private final TreeMap<Integer, Button> buttons = new TreeMap<>();
	private final Map<Integer, Button> navigation = new HashMap<>();
	private int startIndex = 0;

	public SimpleUI(@NotNull String name) {
		this(name, true);
	}

	public SimpleUI(@NotNull String name, boolean actionBlocking) {
		this.name = name;
		this.actionBlocking = actionBlocking;
	}

	public boolean isActionBlocking() {
		return this.actionBlocking;
	}

	public void handleClick(@NotNull InventoryClickEvent event) {
		int slot = event.getRawSlot();

		if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
			// Click not in top inventory
			if (this.isActionBlocking()) {
				event.setCancelled(true);
			}
			return;
		}

		event.setCancelled(true);

		int navStart = event.getView().getTopInventory().getSize() - 9;
		Button button;

		if (!navigation.isEmpty() && slot >= navStart) {
				button = navigation.get(slot - navStart);
		} else {
			button = buttons.get(slot + startIndex);
		}

		if (button != null) {
			button.getConsumer().accept(event);
		}
	}

	public void addButton(@NotNull Button button) {
		setButton(getHighestButton() + 1, button);
	}

	public void setButton(int slot, @Nullable Button button) {
		if (slot < 0) {
			throw new IllegalArgumentException("Button index must be >= 0!");
		}
		buttons.put(slot, button);
	}

	public int getHighestButton() {
		return buttons.size() > 0 ? buttons.lastKey() : -1;
	}

	public void setNavButton(int slot, @Nullable Button button) {
		if (slot < 0 || slot > 4) {
			throw new IllegalArgumentException("Additional navigation buttons may only occupy indexes 0-4!");
		}
		navigation.put(2 + slot, button);
	}

	@Override
	public @NotNull Inventory getInventory() {
		int highestIndex = buttons.size() > 0 ? buttons.lastKey() + 1 : 0;
		int size = Math.min(54, Math.max(9, (int) Math.ceil(highestIndex / 9D) * 9 + (navigation.isEmpty() ? 0 : 9)));
		Inventory inventory = Bukkit.createInventory(this, size, name);
		draw(inventory);
		return inventory;
	}

	public void draw(@NotNull Inventory inventory) {
		ItemStack[] contents = inventory.getContents();
		Arrays.fill(contents, new ItemStack(Material.AIR));
		int endIndex = startIndex + (contents.length == 54 || !navigation.isEmpty() ? contents.length - 9 : contents.length);
		SortedMap<Integer, Button> sortedMap = buttons.subMap(startIndex, endIndex);

		sortedMap.forEach((index, button) -> contents[index - startIndex] = button.getItem());

		if (contents.length == 54) {
			// First page button
			navigation.put(0, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				Consumer<InventoryClickEvent> consumer;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.BLACK_BANNER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta instanceof BannerMeta) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT));
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT_MIRROR));
						bannerMeta.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
						itemStack.setItemMeta(itemMeta);
					}
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
			navigation.put(1, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.BLACK_BANNER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta instanceof BannerMeta) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT));
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT_MIRROR));
					}
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
			navigation.put(7, ((Supplier<Button>) () -> {
				int highestCurrentButton = startIndex + 44;
				int highestRequiredButton = getHighestButton();
				int maxPage = (int) Math.ceil(highestRequiredButton / 45D);
				ItemStack itemStack;
				if (highestCurrentButton > highestRequiredButton) {
					itemStack = new ItemStack(Material.BLACK_BANNER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta instanceof BannerMeta) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT));
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT_MIRROR));
					}
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
			navigation.put(8, ((Supplier<Button>) () -> {
				int maxPage = (int) Math.ceil(getHighestButton() / 45D);
				ItemStack itemStack;
				Consumer<InventoryClickEvent> consumer;
				if (startIndex > 0) {
					itemStack = new ItemStack(Material.BLACK_BANNER);
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta instanceof BannerMeta) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT));
						bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT_MIRROR));
						bannerMeta.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
						itemStack.setItemMeta(itemMeta);
					}
					consumer = event -> {
						startIndex = 45 * maxPage - 45;
						draw(event.getView().getTopInventory());
					};
				} else {
					itemStack = new ItemStack(Material.BARRIER);
					consumer = event -> {};
				}
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta != null) {
					itemMeta.setDisplayName(ChatColor.WHITE + "Last Page");
					itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "  " + maxPage + '/' + maxPage));
					itemStack.setItemMeta(itemMeta);
				}
				return new Button(itemStack, consumer);
			}).get());
		}
		if (!navigation.isEmpty()) {
			int navStart = contents.length - 9;
			navigation.forEach((slot, button) -> contents[slot + navStart] = button.getItem());
		}

		inventory.setContents(contents);
	}

}
