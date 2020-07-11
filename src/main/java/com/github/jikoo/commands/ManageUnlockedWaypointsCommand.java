package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.Waypoint;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManageUnlockedWaypointsCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public ManageUnlockedWaypointsCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		if (args.length < 2) {
			return false;
		}

		Player target = plugin.getServer().getPlayer(args[0]);

		if (target == null) {
			sender.sendMessage("Invalid target!");
			return true;
		}

		boolean unlock = command.getName().contains("un");
		boolean requiresBook = args.length < 3 || args[2].equalsIgnoreCase("false");
		if (requiresBook && !target.getInventory().contains(plugin.getWaypointBook())) {
			sender.sendMessage(target.getName() + " is not carrying an Adventure Log!");
			return true;
		}

		switch (unlock ? plugin.getDataStore().unlockWaypoint(target.getUniqueId(), args[1])
				: plugin.getDataStore().lockWaypoint(target.getUniqueId(), args[1])) {

			case SUCCESS:
				if (unlock) {
					target.sendTitle("Waypoint discovered!", "Check your Adventure Log.", 10, 50, 20);
				}
			case UNNECESSARY:
				sender.sendMessage((unlock ? "Unl" : "L") + "ocked waypoint!");
				return true;
			case FAILURE:
			default:
				sender.sendMessage("Failed to " + (unlock ? "un" : "") + "lock waypoint! Please check server logs.");
				return true;
		}

	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (args.length == 1) {
			return plugin.onTabComplete(sender, command, alias, args);
		} else if (args.length == 2) {

			Player target = plugin.getServer().getPlayer(args[0]);

			if (target == null) {
				return Collections.emptyList();
			}

			Collection<? extends Waypoint> waypoints;
			if (command.getName().contains("un")) {
				waypoints = plugin.getDataStore().getWaypoints();
				Collection<? extends Waypoint> unlocked = plugin.getDataStore().getWaypoints(target.getUniqueId());
				waypoints = waypoints.stream().filter(waypoint -> unlocked.stream().noneMatch(waypoint::equals))
						.collect(Collectors.toList());
			} else {
				waypoints = plugin.getDataStore().getWaypoints(target.getUniqueId());
				Collection<? extends Waypoint> defaults = plugin.getDataStore().getDefaultWaypoints();
				waypoints = waypoints.stream().filter(waypoint -> defaults.stream().noneMatch(waypoint::equals))
						.collect(Collectors.toList());
			}

			return waypoints.stream().map(Waypoint::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());

		} else if (args.length == 3) {
			return Collections.singletonList("false");
		}
		return Collections.emptyList();
	}

}
