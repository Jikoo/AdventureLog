package com.github.jikoo.data;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Waypoint extends YamlSubsetData implements IWaypoint {

	private final String name;

	public Waypoint(YamlData storage, String name, @NotNull ItemStack icon, @NotNull Location location) {
		super(storage, "waypoints." + name);
		this.name = name;
		this.setIcon(icon);
		this.setLocation(location);
	}

	public Waypoint(YamlData storage, String name) {
		super(storage, "waypoints." + name);
		this.name = name;
	}

	@Override
	@NotNull
	public String getName() {
		return name;
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
	@NotNull
	public ItemStack getIcon() {
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
		return this.name.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Waypoint)) {
			return false;
		}
		Waypoint other = (Waypoint) obj;
		// TODO ensure backed by same YamlConfiguration
		return name.equals(other.name);
	}

	public boolean isInvalid() {
		return this.getLocation("location") == null;
	}

}
