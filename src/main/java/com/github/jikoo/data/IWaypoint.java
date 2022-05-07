package com.github.jikoo.data;

import com.github.jikoo.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IWaypoint {

	@NotNull String getName();

	@NotNull Location getLocation();

	default @NotNull ItemStack getIcon() {
		return ItemUtil.getItem(
				Material.DIRT,
				getName(),
				"Something went wrong loading icon!",
				"Please enjoy this complimentary dirt.");
	}

}
