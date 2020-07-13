package com.github.jikoo.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanButton extends Button {

	public BooleanButton(@NotNull AtomicBoolean value, @NotNull Material materialTrue, @NotNull Material materialFalse, @NotNull String name) {
		this(value, materialTrue, materialFalse, null, name);

	}

	public BooleanButton(@NotNull AtomicBoolean value, @NotNull Material materialTrue, @NotNull Material materialFalse,
			@Nullable Consumer<AtomicBoolean> postprocess, @NotNull String name) {
		super(() -> getItem(value, materialTrue, materialFalse, name), event -> {
			value.set(!value.get());

			if (event.getView().getTopInventory().getHolder() instanceof SimpleUI) {
				((SimpleUI) event.getView().getTopInventory().getHolder()).draw(event.getView().getTopInventory());
			}

			if (postprocess != null) {
				postprocess.accept(value);
			}
		});

	}

	private static ItemStack getItem(@NotNull AtomicBoolean value, @NotNull Material typeOn, @NotNull Material typeOff,
			@NotNull String name) {
		return createIcon(value.get() ? typeOn : typeOff,
				ChatColor.WHITE + name + ": " + ChatColor.GOLD + value.get(), ChatColor.WHITE + "Click to toggle");
	}

}
