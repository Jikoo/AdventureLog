package com.github.jikoo.data;

import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IWaypoint {

	@NotNull String getName();

	@NotNull Location getLocation();

	default @NotNull ItemStack getIcon() {
		return TextUtil.getTextItem(
				Material.DIRT,
				Component.text(getName()),
				Component.text("Something went wrong loading icon!"),
				Component.text("Please enjoy this complimentary dirt."));
	}

}
