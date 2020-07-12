package com.github.jikoo.data;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YamlSubsetData {

	private final YamlData storage;
	private final String path;

	public YamlSubsetData(YamlData storage, String path) {
		this.storage = storage;
		this.path = path + '.';
	}

	void set(@NotNull String path, @Nullable Object value) {
		this.storage.set(this.path + path, value);
	}

	@Nullable Object get(@NotNull String path) {
		return this.storage.get(this.path + path);
	}

	@Nullable String getString(@NotNull String path) {
		return this.storage.getString(this.path + path);
	}

	int getInt(@NotNull String path) {
		return this.storage.getInt(this.path + path);
	}

	boolean getBoolean(@NotNull String path) {
		return this.storage.getBoolean(this.path + path);
	}

	double getDouble(@NotNull String path) {
		return this.storage.getDouble(this.path + path);
	}

	long getLong(@NotNull String path) {
		return this.storage.getLong(this.path + path);
	}

	@NotNull List<String> getStringList(@NotNull String path) {
		return this.storage.getStringList(this.path + path);
	}

	<T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz) {
		return this.storage.getObject(this.path + path, clazz);
	}

	<T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
		return this.storage.getSerializable(this.path + path, clazz);
	}

	@Nullable Vector getVector(@NotNull String path) {
		return this.storage.getVector(this.path + path);
	}

	@Nullable ItemStack getItemStack(@NotNull String path) {
		return this.storage.getItemStack(this.path + path);
	}

	@Nullable Location getLocation(@NotNull String path) {
		return this.storage.getLocation(this.path + path);
	}

	@NotNull YamlData raw() {
		return this.storage;
	}

	public void delete() {
		this.storage.set(this.path.substring(0, this.path.length() - 1), null);
	}

}
