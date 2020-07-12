package com.github.jikoo.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ServerWaypoint extends Waypoint {

	public ServerWaypoint(ServerData storage, String name, @NotNull ItemStack icon, @NotNull Location location) {
		super(storage, name, icon, location);
	}

	public ServerWaypoint(ServerData storage, String name) {
		super(storage, name);
	}

	public long getRangeSquared() {
		return this.getLong("range_squared");
	}

	public void setRange(int range) {
		this.set("range_squared", range < 1 ? -1 : range * range);
		raw().notifyRangeUpdate(this);
	}

	public boolean isDefault() {
		return this.getBoolean("default");
	}

	public void setDefault(boolean isDefault) {
		this.set("default", isDefault);
		raw().notifyDefaultUpdate(this);
	}

	@Override
	public void delete() {
		super.delete();
		raw().notifyDelete(this);
	}

	@Override
	@NotNull ServerData raw() {
		return (ServerData) super.raw();
	}

}
