package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.DataStore;
import com.github.jikoo.Waypoint;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManageDefaultWaypointsCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public ManageDefaultWaypointsCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		if (args.length < 1) {
			return false;
		}

		boolean add = command.getName().contains("add");

		if (add && plugin.getDataStore().getWaypoint(args[0]) == null) {
			sender.sendMessage("Invalid waypoint!");
			return true;
		}


		if ((add ? plugin.getDataStore().addDefault(args[0]) : plugin.getDataStore().removeDefault(args[0])) != DataStore.Result.FAILURE) {
			sender.sendMessage((add ? "Add" : "Remov") + "ed default waypoint!");
			return true;
		}

		sender.sendMessage("Failed to " + (add ? "add" : "remove") + " default waypoint! Please check server logs.");
		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (args.length == 1) {
			boolean add = command.getName().contains("add");
			Collection<Waypoint> waypoints;
			if (add) {
				waypoints = plugin.getDataStore().getWaypoints();
				waypoints = waypoints.stream().filter(waypoint -> plugin.getDataStore().getDefaultWaypoints().stream()
						.noneMatch(waypoint::equals)).collect(Collectors.toList());
			} else {
				waypoints = plugin.getDataStore().getDefaultWaypoints();
			}
			return waypoints.stream().map(Waypoint::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[0])).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
