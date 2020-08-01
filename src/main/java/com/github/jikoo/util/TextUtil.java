package com.github.jikoo.util;

import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextUtil {

	private static final Pattern STRIP_COLOR = Pattern.compile(ChatColor.STRIP_COLOR_PATTERN.pattern(), Pattern.CASE_INSENSITIVE);

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

	@Contract(value = "null -> null; !null -> !null", pure = true)
	public static @Nullable String stripColor(final String input) {
		if ( input == null ) {
			return null;
		}

		return STRIP_COLOR.matcher(input).replaceAll("");
	}

	private TextUtil() {}

}
