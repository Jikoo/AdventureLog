package com.github.jikoo.ui;

import com.github.jikoo.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class IntegerButton extends Button {

	public IntegerButton(
			@NotNull AtomicInteger value,
			@NotNull Material type,
			@NotNull String name,
			@NotNull Component @NotNull ... additionalInfo) {
		this(value, type, null, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			@NotNull Material type,
			@Nullable Consumer<AtomicInteger> postprocess,
			@NotNull String name,
			@NotNull Component @NotNull ... additionalInfo) {
		this(value, Integer.MIN_VALUE, Integer.MAX_VALUE, type, postprocess, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			int minValue,
			int maxValue,
			@NotNull Material type,
			@NotNull String name,
			@NotNull Component @NotNull ... additionalInfo) {
		this(value, minValue, maxValue, type, null, name, additionalInfo);
	}

	public IntegerButton(
			@NotNull AtomicInteger value,
			int minValue,
			int maxValue,
			@NotNull Material type,
			@Nullable Consumer<AtomicInteger> postprocess,
			@NotNull String name,
			@NotNull Component @NotNull ... additionalInfo) {
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
			@NotNull Component @NotNull ... additionalInfo) {
		List<Component> components = new ArrayList<>();
		components.add(Component.text().append(Component.text(name + ": ").color(NamedTextColor.WHITE), Component.text(value.get()).color(NamedTextColor.GOLD)).build());
		components.add(Component.text().append(Component.text("Left click: ").color(NamedTextColor.WHITE), Component.text("+1").color(NamedTextColor.GOLD)).build());
		components.add(Component.text().append(Component.text("Right click: ").color(NamedTextColor.WHITE), Component.text("-1").color(NamedTextColor.GOLD)).build());
		components.add(Component.text().append(Component.text("Shift+click: ").color(NamedTextColor.WHITE), Component.text("±10").color(NamedTextColor.GOLD)).build());
		Component.text().color(NamedTextColor.RED)
				.append(
						Component.text("Drop stack (Default ctrl+"),
						Component.translatable("key.drop"),
						Component.text(')'));
		components.add(Component.text().color(NamedTextColor.WHITE)
				.append(
						Component.text("Drop (Default "),
						Component.translatable("key.drop"),
						Component.text(')'),
						Component.text("+100").color(NamedTextColor.GOLD))
				.build());
		components.add(Component.text().color(NamedTextColor.WHITE)
				.append(
						Component.text("Drop stack (Default ctrl+"),
						Component.translatable("key.drop"),
						Component.text(')'),
						Component.text("-100").color(NamedTextColor.GOLD))
				.build());

		components.addAll(List.of(additionalInfo));
		return ItemUtil.getItem(type, components);
	}

}
