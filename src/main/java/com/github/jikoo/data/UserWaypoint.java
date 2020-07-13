package com.github.jikoo.data;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class UserWaypoint extends Waypoint {

	UserWaypoint(@NotNull UserData storage, @NotNull String name) {
		super(storage, name);
	}

	@Override
	public @NotNull String getName() {
		String customName = this.getString("custom_name");
		return customName != null ? customName : super.getName();
	}

	public @NotNull String getSortingName() {
		String comparableName = this.getString("comparable_name");
		return comparableName != null ? comparableName : getName();
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
			itemMeta.setDisplayName(ChatColor.GOLD + "Home " + getName());
			icon.setItemMeta(itemMeta);
		}
		return icon;
	}

	@Override
	public void setIcon(@NotNull ItemStack icon) {
		super.setIcon(icon);

		// Lift icon name for later use in sorting
		String displayName = null;
		ItemMeta itemMeta = icon.getItemMeta();
		if (itemMeta != null && itemMeta.hasDisplayName()) {
			displayName = itemMeta.getDisplayName();
			if (displayName.isEmpty()) {
				displayName = null;
			}
		}

		this.set("custom_name", displayName);
		// TODO double check - stripColor appears to only consider upper case color codes while translate etc. create lower
		this.set("comparable_name", displayName == null ? null : ChatColor.stripColor(displayName.toUpperCase()));
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
