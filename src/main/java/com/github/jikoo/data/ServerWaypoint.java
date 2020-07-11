package com.github.jikoo.data;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ServerWaypoint extends Waypoint {

	public ServerWaypoint(YamlData storage, String name, @NotNull ItemStack icon, @NotNull Location location) {
		super(storage, name, icon, location);
	}

	public ServerWaypoint(YamlData storage, String name) {
		super(storage, name);
	}

	public long getRangeSquared() {
		return this.getLong("range_squared");
	}

	public void setRange(int range) {
		this.set("range_squared", range < 1 ? -1 : range * range);
	}

	public boolean isDefault() {
		// TODO should migrate and manually manage list
		return this.promiseMeThatYouKnowWhatYouAreDoing().getStringList("defaults").contains(getName());
	}

	public void setDefault(boolean isDefault) {
		List<String> defaults = this.promiseMeThatYouKnowWhatYouAreDoing().getStringList("defaults");
		if (isDefault) {
			if (defaults.contains(getName())) {
				return;
			}
			defaults.add(getName());
		} else {
			if (!defaults.contains(getName())) {
				return;
			}
			defaults.remove(getName());
		}
		this.promiseMeThatYouKnowWhatYouAreDoing().set("defaults", defaults);
	}

	@Override
	public void delete() {
		setDefault(false);
		super.delete();
	}
}
