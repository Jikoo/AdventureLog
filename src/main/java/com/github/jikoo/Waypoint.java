package com.github.jikoo;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Waypoint {

	private final String name;
	private Location location;
	private ItemStack icon;
	private int priority;
	private int rangeSquared;

	private Waypoint(@NotNull String name, @NotNull Location location, @NotNull ItemStack icon, int priority,
			int rangeSquared) {
		this.name = name;
		this.location = location;
		this.icon = icon;
		this.priority = priority;
		this.rangeSquared = rangeSquared;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@NotNull
	public Location getLocation() {
		return location;
	}

	public void setLocation(@NotNull Location location) {
		Objects.requireNonNull(location.getWorld(), "Waypoint world must be set!");
		this.location = location;
	}

	@NotNull
	public ItemStack getIcon() {
		return icon;
	}

	public void setIcon(@NotNull ItemStack icon) {
		this.icon = icon;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getRangeSquared() {
		return rangeSquared;
	}

	public void setRange(int range) {
		this.rangeSquared = range < 1 ? -1 : range * range;
	}

	public static class Builder {
		private final String name;
		private Location location;
		private ItemStack icon;
		private int priority;
		private int rangeSquared;

		public Builder(@NotNull String name) {
			this.name = name;
		}

		public Builder setLocation(@NotNull Location location) {
			this.location = location;
			return this;
		}

		public Builder setIcon(@NotNull ItemStack icon) {
			if (icon.getType() == Material.AIR) {
				return this;
			}
			this.icon = icon;
			return this;
		}

		public Builder setPriority(int priority) {
			this.priority = priority;
			return this;
		}

		public Builder setRangeSquared(int rangeSquared) {
			this.rangeSquared = rangeSquared;
			return this;
		}

		public Builder setRange(int range) {
			this.rangeSquared = range < 1 ? -1 : range * range;
			return this;
		}

		public Waypoint build() {
			Objects.requireNonNull(location, "Waypoint must have a location set!");
			Objects.requireNonNull(location.getWorld(), "Waypoint world must be set!");
			Objects.requireNonNull(icon, "Inventory representation must not be null!");
			return new Waypoint(name, location, icon, priority, rangeSquared);
		}
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
		return name.equals(other.name) && location.equals(other.location) && icon.isSimilar(other.icon)
				&& priority == other.priority && rangeSquared == other.rangeSquared;
	}

}
