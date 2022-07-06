package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class ManageUnlockedWaypointsCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public ManageUnlockedWaypointsCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String alias,
			@NotNull String @NotNull [] args) {

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

		if (unlock ? plugin.getDataManager().getUserData(target.getUniqueId()).unlockWaypoint(args[1])
				: plugin.getDataManager().getUserData(target.getUniqueId()).lockWaypoint(args[1])) {
			target.showTitle(Title.title(
					Component.text("Waypoint discovered!"),
					Component.text("Check your Adventure Log."),
					Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(2_500L), Duration.ofMillis(1_000L))));
		}

		sender.sendMessage((unlock ? "Unl" : "L") + "ocked waypoint!");
		return true;

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
				return List.of();
			}

			Collection<ServerWaypoint> waypoints;
			if (command.getName().contains("un")) {
				waypoints = plugin.getDataManager().getServerData().getWaypoints();
				Collection<ServerWaypoint> unlocked = plugin.getDataManager().getUserData(target.getUniqueId()).getUnlockedWaypoints();
				waypoints = waypoints.stream().filter(waypoint -> unlocked.stream().noneMatch(waypoint::equals)).toList();
			} else {
				waypoints = plugin.getDataManager().getUserData(target.getUniqueId()).getUnlockedWaypoints();
			}

			return waypoints.stream().map(Waypoint::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).toList();

		} else if (args.length == 3) {
			return List.of("false");
		}
		return List.of();
	}

}
