package com.github.jikoo.data;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Waypoint extends YamlSubsetData implements IWaypoint {

	private final String id;

	public Waypoint(YamlData storage, String id, @NotNull ItemStack icon, @NotNull Location location) {
		super(storage, "waypoints." + id);
		this.id = id;
		this.setIcon(icon);
		this.setLocation(location);
	}

	public Waypoint(YamlData storage, String id) {
		super(storage, "waypoints." + id);
		this.id = id;
	}

	@Override
	@NotNull
	public String getId() {
		return id;
	}

	@Override
	public @NotNull Component getName() {
		ItemStack icon = this.getItemStack("icon");
		if (icon != null && icon.hasItemMeta()) {
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
		return Objects.requireNonNull(this.getLocation("location"));
	}

	public void setLocation(@NotNull Location location) {
		Preconditions.checkArgument(location.isWorldLoaded(), "Waypoint world must be loaded to set!");
		this.set("location", location);
	}

	@Override
	public @NotNull ItemStack getIcon() {
		ItemStack icon = this.getItemStack("icon");
		if (icon != null && !icon.getType().isAir()) {
			return icon;
		}
		return IWaypoint.super.getIcon();
	}

	public void setIcon(@NotNull ItemStack icon) {
		this.set("icon", icon.clone());
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Waypoint other)) {
			return false;
		}
		return id.equals(other.id) && raw().raw().equals(other.raw().raw());
	}

	public boolean isInvalid() {
		return this.getLocation("location") == null;
	}

}
