package com.github.jikoo.ui;

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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

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
			throw new IllegalArgumentException("Additional navigation buttons may only occupy indices 0-4!");
		}
		navigation.put(2 + slot, button);
	}

	protected int getInventorySize() {
		int highestIndex = buttons.size() > 0 ? buttons.lastKey() + 1 : 0;
		return Math.min(54, Math.max(9, (int) Math.ceil(highestIndex / 9D) * 9 + (navigation.isEmpty() ? 0 : 9)));
	}

	@Override
	public @NotNull Inventory getInventory() {
		Inventory inventory = Bukkit.createInventory(this, getInventorySize(), name);
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
			navigation.put(0, getNavFirstPage());

			// Previous page button
			navigation.put(1, getNavPreviousPage());

			// Next page button
			navigation.put(7, getNavNextPage());

			// Last page button
			navigation.put(8, getNavLastPage());
		}
		if (!navigation.isEmpty()) {
			int navStart = contents.length - 9;
			navigation.forEach((slot, button) -> contents[slot + navStart] = button.getItem());
		}

		inventory.setContents(contents);
	}

	@NotNull
	private Button getNavFirstPage() {
		int maxPage = (int) Math.ceil(getHighestButton() / 45D);
		String navName = "First Page";
		String navIndex = getNavIndex(1, maxPage);

		if (startIndex <= 0) {
			return getNavDisabled(navName, navIndex);
		}

		return getNav(
				navName,
				navIndex,
				event -> {
					startIndex = 0;
					draw(event.getView().getTopInventory());
				},
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT),
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT_MIRROR),
				new Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT)
		);
	}

	private @NotNull Button getNavPreviousPage() {
		int maxPage = (int) Math.ceil(getHighestButton() / 45D);
		String navIndex = getNavIndex(Math.max(1, startIndex / 45), maxPage);

		if (startIndex <= 0) {
			return getNavDisabled("First Page", navIndex);
		}

		return getNav(
				"Previous Page",
				navIndex,
				event -> {
					startIndex -= 45;
					draw(event.getView().getTopInventory());
				},
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT),
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_LEFT_MIRROR)
		);
	}

	private @NotNull Button getNavNextPage() {
		int highestCurrentButton = startIndex + 44;
		int highestButton = getHighestButton();
		int maxPage = (int) Math.ceil(highestButton / 45D);
		int nextPage = Math.min(startIndex / 45 + 2, maxPage);
		String navIndex = getNavIndex(nextPage, maxPage);

		if (highestCurrentButton >= highestButton) {
			return getNavDisabled("Last Page", navIndex);
		}

		return getNav(
				"Next Page",
				navIndex,
				event -> {
					startIndex += 45;
					draw(event.getView().getTopInventory());
				},
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT),
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT_MIRROR)
		);
	}

	private @NotNull Button getNavLastPage() {
		int highestCurrentButton = startIndex + 44;
		int highestButton = getHighestButton();
		int maxPage = (int) Math.ceil(highestButton / 45D);
		String navName = "Last Page";
		String navIndex = getNavIndex(maxPage, maxPage);

		if (highestCurrentButton >= highestButton) {
			return getNavDisabled(navName, navIndex);
		}

		return getNav(
				navName,
				navIndex,
				event -> {
					startIndex = 45 * maxPage - 45;
					draw(event.getView().getTopInventory());
				},
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT),
				new Pattern(DyeColor.WHITE, PatternType.DIAGONAL_RIGHT_MIRROR),
				new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
	}

	@Contract(pure = true)
	private @NotNull String getNavName(@NotNull String name) {
		return ChatColor.WHITE + name;
	}

	@Contract(pure = true)
	private @NotNull String getNavIndex(int pageNumber, int maxPageNumber) {
		return ChatColor.GOLD + "  " + pageNumber + '/' + maxPageNumber;
	}

	private @NotNull Button getNav(
			@NotNull String name,
			@NotNull String index,
			@NotNull Consumer<InventoryClickEvent> consumer,
			@NotNull Pattern... patterns) {
		ItemStack itemStack = new ItemStack(Material.BLACK_BANNER);
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (itemMeta instanceof BannerMeta bannerMeta) {
			bannerMeta.setPatterns(List.of(patterns));
			bannerMeta.setDisplayName(getNavName(name));
			bannerMeta.setLore(List.of(index));
			itemStack.setItemMeta(itemMeta);
		}

		return new Button(itemStack, consumer);
	}

	private @NotNull Button getNavDisabled(@NotNull String name, @NotNull String index) {
		ItemStack itemStack = new ItemStack(Material.BARRIER);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(getNavName(name));
			itemMeta.setLore(List.of(index));
			itemStack.setItemMeta(itemMeta);
		}
		return new Button(itemStack, event -> {});
	}

}
