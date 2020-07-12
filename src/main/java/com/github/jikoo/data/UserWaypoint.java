package com.github.jikoo.data;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class UserWaypoint extends Waypoint {

	public UserWaypoint(@NotNull UserData storage, @NotNull String name) {
		super(storage, name);
	}

	public UserWaypoint(@NotNull UserData storage, @NotNull String name, @NotNull Location location) {
		super(storage, name);
		this.setLocation(location);
	}

	@Override
	public @NotNull ItemStack getIcon() {
		ItemStack icon = this.getItemStack("icon");
		if (icon == null || icon.getType().isAir()) {
			List<Material> values = new ArrayList<>(Tag.BEDS.getValues());
			int index = getName().hashCode() % values.size();
			icon = new ItemStack(values.get(index));
		}
		ItemMeta itemMeta = icon.getItemMeta();
		if (itemMeta != null && !itemMeta.hasDisplayName()) {
			itemMeta.setDisplayName(ChatColor.WHITE + "Home " + getName());
			icon.setItemMeta(itemMeta);
		}
		return icon;
	}

	@Override
	public void delete() {
		super.delete();
		raw().notifyDelete(this);
	}

	@Override
	@NotNull UserData raw() {
		return (UserData) super.raw();
	}

}
