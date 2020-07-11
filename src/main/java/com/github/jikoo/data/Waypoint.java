package com.github.jikoo.data;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Waypoint extends YamlSubsetData {

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

	@NotNull
	public String getName() {
		return name;
	}

	public @NotNull Location getLocation() {
		return Objects.requireNonNull(this.getLocation("location"));
	}

	public void setLocation(@NotNull Location location) {
		Preconditions.checkArgument(location.isWorldLoaded(), "Waypoint world must be loaded to set!");
		this.set("location", location);
	}

	@NotNull
	public ItemStack getIcon() {
		ItemStack icon = this.getItemStack("icon");
		if (icon != null && !icon.getType().isAir()) {
			return icon;
		}
		ItemStack gollyGeeHeckers = new ItemStack(Material.DIRT);
		ItemMeta thisIsABummer = gollyGeeHeckers.getItemMeta();
		if (thisIsABummer != null) {
			thisIsABummer.setDisplayName(getName());
			thisIsABummer.setLore(Arrays.asList("Something went wrong loading icon!", "Please enjoy this complimentary dirt."));
			gollyGeeHeckers.setItemMeta(thisIsABummer);
		}
		return gollyGeeHeckers;
	}

	public void setIcon(@NotNull ItemStack icon) {
		this.set("icon", icon);
	}

	public int getPriority() {
		return this.getInt("priority");
	}

	public void setPriority(int priority) {
		this.set("priority", priority);
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

	public boolean isValid() {
		return this.getLocation("location") != null;
	}

}
