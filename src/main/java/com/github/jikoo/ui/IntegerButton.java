package com.github.jikoo.ui;

import com.github.jikoo.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class IntegerButton extends Button {

	public IntegerButton(
			@NotNull AtomicInteger value,
			@NotNull Material type,
			@NotNull String name,
			@NotNull String @NotNull ... additionalInfo) {
		this(value, type, null, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			@NotNull Material type,
			@Nullable Consumer<AtomicInteger> postprocess,
			@NotNull String name,
			@NotNull String @NotNull ... additionalInfo) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, type, postprocess, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			int minValue,
			int maxValue,
			@NotNull Material type,
			@NotNull String name,
			@NotNull String @NotNull ... additionalInfo) {
		this(value, minValue, maxValue, type, null, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			int minValue,
			int maxValue,
			@NotNull Material type,
			@Nullable Consumer<AtomicInteger> postprocess,
			@NotNull String name,
			@NotNull String @NotNull ... additionalInfo) {
		super(() -> getItem(value, type, name, additionalInfo), event -> {
			int diff = switch (event.getClick()) {
				case LEFT -> 1;
				// In case of click spamming, still do something
				case DOUBLE_CLICK -> 2;
				case SHIFT_LEFT -> 10;
				case RIGHT -> -1;
				case SHIFT_RIGHT -> -10;
				case DROP -> 100;
				case CONTROL_DROP -> -100;
				default -> 0;
			};
			int newValue = value.addAndGet(diff);
			if (newValue < minValue) {
				value.set(minValue);
			} else if (newValue >= maxValue) {
				value.set(maxValue);
			}

			if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
				((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
			}

			if (postprocess != null) {
				postprocess.accept(value);
			}
		});
	}

	private static @NotNull ItemStack getItem(
			@NotNull AtomicInteger value,
			@NotNull Material type,
			@NotNull String name,
			@NotNull String @NotNull ... additionalInfo) {
		String[] newInfo = new String[6 + additionalInfo.length];
		newInfo[0] = ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get();
		newInfo[1] = ChatColor.WHITE + "Left click: " + ChatColor.GOLD + "+1";
		newInfo[2] = ChatColor.WHITE + "Right click: " + ChatColor.GOLD + "-1";
		newInfo[3] = ChatColor.WHITE + "Shift+click: " + ChatColor.GOLD + "±10";
		newInfo[4] = ChatColor.WHITE + "Drop (default Q): " + ChatColor.GOLD + "+100";
		newInfo[5] = ChatColor.WHITE + "Drop stack (Ctrl+Q): " + ChatColor.GOLD + "-100";
		System.arraycopy(additionalInfo, 0, newInfo, 6, additionalInfo.length);
		return ItemUtil.getItem(type, newInfo);
	}

}
