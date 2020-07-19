package com.github.jikoo.ui;

import com.github.jikoo.util.ItemUtil;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerButton extends Button {

	public IntegerButton(@NotNull AtomicInteger value, @NotNull Material type, @NotNull String name,
			String @NotNull ... additionalInfo) {
		this(value, type, null, name, additionalInfo);
	}

	public IntegerButton(@NotNull AtomicInteger value, @NotNull Material type, 
			@Nullable Consumer<AtomicInteger> postprocess, @NotNull String name, String @NotNull ... additionalInfo) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, type, postprocess, name, additionalInfo);
	}

	public IntegerButton(@NotNull AtomicInteger value, Integer minValue, Integer maxValue, @NotNull Material type,
			@NotNull String name, String @NotNull ... additionalInfo) {
		this(value, minValue, maxValue, type, null, name, additionalInfo);
	}

	public IntegerButton(@NotNull AtomicInteger value, Integer minValue, Integer maxValue, @NotNull Material type,
			@Nullable Consumer<AtomicInteger> postprocess, @NotNull String name, String @NotNull ... additionalInfo) {
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

			if (postprocess != null) {
				postprocess.accept(value);
			}
		});
	}

	private static @NotNull ItemStack getItem(@NotNull AtomicInteger value, @NotNull Material type,
			@NotNull String name, String @NotNull ... additionalInfo) {
		String[] newInfo = new String[6 + additionalInfo.length];
		newInfo[0] = ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get();
		newInfo[1] = ChatColor.WHITE + "Left click: " + ChatColor.GOLD + "+1";
		newInfo[2] = ChatColor.WHITE + "Right click: " + ChatColor.GOLD + "-1";
		newInfo[3] = ChatColor.WHITE + "Shift+click: " + ChatColor.GOLD + "±10";
		newInfo[4] = ChatColor.WHITE + "Drop (default Q): " + ChatColor.GOLD + "+100";
		newInfo[5] = ChatColor.WHITE + "Drop stack (ctrl+Q): " + ChatColor.GOLD + "-100";
		System.arraycopy(additionalInfo, 0, newInfo, 6, additionalInfo.length);
		return ItemUtil.getItem(type, newInfo);
	}

}
