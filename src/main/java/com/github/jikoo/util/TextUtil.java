package com.github.jikoo.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class TextUtil {

	public static @NotNull String getDisplay(@NotNull World world) {
		String name = world.getName();
		if ("world".equals(name)) {
			return "Overworld";
		}
		if ("world_nether".equals(name)) {
			return "The Nether";
		}
		if ("world_the_end".equals(name)) {
			return "The End";
		}
		StringBuilder builder = new StringBuilder();
		for (String word : name.split("[_ ]")) {
			if (word.length() == 0) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return builder.toString();
	}

	public static @NotNull String getDisplay(@NotNull Location location) {
		return String.format("%s: %sx, %sy, %sz",
				location.getWorld() == null ? "Unloaded World" : getDisplay(location.getWorld()),
				location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static @NotNull TextComponent.Builder itemText() {
		return Component.text().decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
	}

	public static @NotNull TextComponent itemText(String content) {
		return Component.text(content).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
	}

	private TextUtil() {}

}
