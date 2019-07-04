package com.github.jikoo;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Waypoint {

	private final String name;
	private final Location location;
	private final ItemStack icon;
	private final int priority;

	private Waypoint(@NotNull String name, @NotNull Location location, @NotNull ItemStack icon, int priority) {
		this.name = name;
		this.location = location;
		this.icon = icon;
		this.priority = priority;
	}

	public @NotNull String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}

	public @NotNull ItemStack getIcon() {
		return icon;
	}

	public int getPriority() {
		return priority;
	}

	public static class Builder {
		private final String name;
		private Location location;
		private ItemStack icon;
		private int priority;

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

		public Waypoint build() {
			Objects.requireNonNull(location, "Waypoint must have a location set!");
			Objects.requireNonNull(location.getWorld(), "Waypoint world must be set!");
			Objects.requireNonNull(icon, "Inventory representation must not be null!");
			return new Waypoint(name, location, icon, priority);
		}
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Waypoint)) {
			return false;
		}
		Waypoint other = (Waypoint) obj;
		return name.equals(other.name) && location.equals(other.location) && icon.isSimilar(other.icon) && priority == other.priority;
	}

}
