package com.github.jikoo.data;

import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleWaypoint implements IWaypoint {

	private final String id;
	private final ItemStack icon;
	private final Supplier<Location> location;

	public SimpleWaypoint(@NotNull String id, @NotNull Material type, @NotNull Location location) {
		this(id, type, () -> location);
	}

	public SimpleWaypoint(@NotNull String id, @NotNull Material type, @NotNull Supplier<Location> location) {
		this.id = id;
		this.icon = TextUtil.getTextItem(type, TextUtil.itemText(id).color(NamedTextColor.GOLD));
		if (!location.get().isWorldLoaded()) {
			throw new IllegalStateException("SimpleWaypoint location's world must be loaded!");
		}
		this.location = location;
	}

	@Override
	public @NotNull String getId() {
		return this.id;
	}

	@Override
	public @NotNull Component getName() {
		if (icon.hasItemMeta()) {
			ItemMeta itemMeta = icon.getItemMeta();
			Component name = itemMeta.displayName();
			if (name != null) {
				return name;
			}
		}
		return Component.text(getId());
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
