package com.github.jikoo.data;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public interface IWaypoint {

	@NotNull String getName();

	@NotNull Location getLocation();

	default @NotNull ItemStack getIcon() {
		ItemStack gollyGeeHeckers = new ItemStack(Material.DIRT);
		ItemMeta thisIsABummer = gollyGeeHeckers.getItemMeta();
		if (thisIsABummer != null) {
			thisIsABummer.setDisplayName(getName());
			thisIsABummer.setLore(Arrays.asList("Something went wrong loading icon!", "Please enjoy this complimentary dirt."));
			gollyGeeHeckers.setItemMeta(thisIsABummer);
		}
		return gollyGeeHeckers;
	}

	int getPriority();

}
