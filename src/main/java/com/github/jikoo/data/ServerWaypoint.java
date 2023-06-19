package com.github.jikoo.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public final class ServerWaypoint extends Waypoint {

	public static final Comparator<ServerWaypoint> COMPARATOR = Comparator.comparing(ServerWaypoint::getPriority).reversed();

	ServerWaypoint(ServerData storage, String id, @NotNull ItemStack icon, @NotNull Location location) {
		super(storage, id, icon, location);
	}

	ServerWaypoint(ServerData storage, String name) {
		super(storage, name);
	}

	public int getPriority() {
		return this.getInt("priority");
	}

	public void setPriority(int priority) {
		this.set("priority", priority);
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
