package com.github.jikoo.data;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public abstract class YamlData {

	private final Plugin plugin;
	private final File file;
	private final YamlConfiguration storage;
	private boolean dirty = false;
	private @Nullable BukkitTask saveTask;

	public YamlData(Plugin plugin, File file) {
		this.plugin = plugin;
		this.file = file;
		this.storage = YamlConfiguration.loadConfiguration(file);
	}

	void set(@NotNull String path, @Nullable Object value) {
		Object existing = this.storage.get(path, null);
		if (Objects.equals(value, existing)) {
			return;
		}
		this.storage.set(path, value);
		this.dirty = true;
		save();
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

	@Nullable <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
		return this.storage.getObject(path, clazz);
	}

	@Nullable <T extends ConfigurationSerializable> T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
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

	@NotNull YamlConfiguration raw() {
		return this.storage;
	}

	public void forceSave() throws IOException {
		if (this.saveTask != null) {
			this.saveTask.cancel();
			this.saveTask = null;
		}
		saveNow();
	}

	private void save() {
		if (this.saveTask != null || !this.dirty) {
			return;
		}
		try {
			saveTask = new BukkitRunnable() {
				@Override
				public void run() {
					saveTask = null;
					try {
						saveNow();
					} catch (IOException e) {
						YamlData.this.plugin.getLogger().log(Level.WARNING, "Failed to save data", e);
					}
				}

				@Override
				public synchronized void cancel() throws IllegalStateException {
					saveTask = null;
					try {
						saveNow();
					} catch (IOException e) {
						YamlData.this.plugin.getLogger().log(Level.WARNING, "Failed to save data", e);
					}
					super.cancel();
				}
			}.runTaskLaterAsynchronously(plugin, 200L);
		} catch (IllegalStateException illegalState) {
			// Plugin is being disabled, cannot schedule tasks
			try {
				saveNow();
			} catch (IOException io) {
				this.plugin.getLogger().log(Level.WARNING, "Failed to save data", io);
			}
		}
	}

	private void saveNow() throws IOException {
		if (!this.dirty) {
			return;
		}

		String data;
		if (Bukkit.isPrimaryThread()) {
			data = this.storage.saveToString();
		} else {
			try {
				data = this.plugin.getServer().getScheduler().callSyncMethod(this.plugin, this.storage::saveToString).get();
			} catch (InterruptedException | ExecutionException e) {
				// Likely that plugin is being disabled and task was already scheduled.
				data = this.storage.saveToString();
			}
		}

		Files.createDirectories(this.file.toPath().getParent());

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(this.file), Charsets.UTF_8)) {
			writer.write(data);
		}

		this.dirty = false;
	}

}
