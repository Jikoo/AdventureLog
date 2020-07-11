package com.github.jikoo.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YamlData {

	private final File file;
	private final YamlConfiguration storage;
	private boolean dirty = false;

	public YamlData(File file) {
		this.file = file;
		this.storage = YamlConfiguration.loadConfiguration(file);
	}

	void set(@NotNull String path, @Nullable Object value) {
		Object existing = this.storage.get(path);
		if (Objects.equals(value, existing)) {
			// TODO should consider List contents comparison
			return;
		}
		this.storage.set(path, value);
		this.dirty = true;
	}

	@Nullable Object get(@NotNull String path) {
		return this.storage.get(path);
	}

	@Nullable String getString(@NotNull String path) {
		return this.storage.getString(path);
	}

	int getInt(@NotNull String path) {
		return this.storage.getInt(path);
	}

	boolean getBoolean(@NotNull String path) {
		return this.storage.getBoolean(path);
	}

	double getDouble(@NotNull String path) {
		return this.storage.getDouble(path);
	}

	long getLong(@NotNull String path) {
		return this.storage.getLong(path);
	}

	@NotNull List<String> getStringList(@NotNull String path) {
		return this.storage.getStringList(path);
	}

	<T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz) {
		return this.storage.getObject(path, clazz);
	}

	<T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
		return this.storage.getSerializable(path, clazz);
	}

	@Nullable Vector getVector(@NotNull String path) {
		return this.storage.getVector(path);
	}

	@Nullable ItemStack getItemStack(@NotNull String path) {
		return this.storage.getItemStack(path);
	}

	@Nullable Location getLocation(@NotNull String path) {
		return this.storage.getLocation(path);
	}

	public void save() throws IOException {
		if (!this.dirty) {
			return;
		}

		this.storage.save(this.file);
		this.dirty = false;
	}

}
