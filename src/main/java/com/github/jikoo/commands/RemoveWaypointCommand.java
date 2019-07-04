package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.DataStore;
import com.github.jikoo.Waypoint;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RemoveWaypointCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public RemoveWaypointCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		if (args.length < 1) {
			return false;
		}

		if (plugin.getDataStore().removeWaypoint(args[0]) != DataStore.Result.FAILURE) {
			sender.sendMessage("Removed waypoint successfully!");
			return true;
		}
		sender.sendMessage("Failed to remove waypoint! Please check server logs.");
		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (args.length == 1) {
			return plugin.getDataStore().getWaypoints().stream().map(Waypoint::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[0])).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
