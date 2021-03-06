package com.github.jikoo.data;

import com.github.jikoo.util.ItemUtil;
import java.util.function.Supplier;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimpleWaypoint implements IWaypoint {

	private final String name;
	private final ItemStack icon;
	private final Supplier<Location> location;

	public SimpleWaypoint(@NotNull String name, @NotNull Material type, @NotNull Location location) {
		this(name, type, () -> location);
	}

	public SimpleWaypoint(@NotNull String name, @NotNull Material type, @NotNull Supplier<Location> location) {
		this.name = name;
		this.icon = ItemUtil.getItem(type, ChatColor.GOLD + name);
		if (!location.get().isWorldLoaded()) {
			throw new IllegalStateException("SimpleWaypoint location's world must be loaded!");
		}
		this.location = location;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public @NotNull Location getLocation() {
		return this.location.get();
	}

	@Override
	public @NotNull ItemStack getIcon() {
		if (this.icon != null && !this.icon.getType().isAir()) {
			return this.icon;
		}
		return IWaypoint.super.getIcon();
	}

}
