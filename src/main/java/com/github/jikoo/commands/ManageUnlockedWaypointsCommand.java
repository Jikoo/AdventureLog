package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import com.github.jikoo.data.Waypoint;
import com.github.jikoo.util.AdvLogPerms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

abstract class ManageUnlockedWaypointsCommand extends PluginCommand<AdventureLogPlugin> {

	public ManageUnlockedWaypointsCommand(@NotNull AdventureLogPlugin plugin, @NotNull String command) {
		super(plugin, command);
		setUsage("/<command> <player> <waypoint>");

		Permission parent = AdvLogPerms.getOrCreate("manage");
		Permission child = AdvLogPerms.defineChild(parent, "discovery");
		child.setDescription("Permission to spawn adventure logs for self.");

		setPermission(child.getName());
	}

	abstract @NotNull String getSuccessFeedback();

	abstract @NotNull BiPredicate<UserData, String> waypointStateSetter();

	abstract @NotNull Collection<ServerWaypoint> getCompletableWaypoints(@NotNull UserData userData);

	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull String alias,
			@NotNull String[] args) {

		if (args.length < 2) {
			return false;
		}

		AdventureLogPlugin plugin = getPlugin();
		Player target = plugin.getServer().getPlayer(args[0]);

		if (target == null) {
			sender.sendMessage("Invalid target!");
			return true;
		}

		boolean requiresBook = args.length < 3 || args[2].equalsIgnoreCase("false");
		if (requiresBook && !target.getInventory().contains(plugin.getWaypointBook())) {
			sender.sendMessage(target.getName() + " is not carrying an Adventure Log!");
			return true;
		}

		if (waypointStateSetter().test(plugin.getDataManager().getUserData(target.getUniqueId()), args[1])) {
			target.showTitle(Title.title(
					Component.text("Waypoint discovered!"),
					Component.text("Check your Adventure Log."),
					Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(2_500L), Duration.ofMillis(1_000L))));
		}

		sender.sendMessage(getSuccessFeedback());
		return true;

	}

	@Override
	public @NotNull List<String> onTabComplete(
					@NotNull CommandSender sender,
					@NotNull String alias,
					@NotNull String[] args) {
		if (args.length == 1) {
			return super.onTabComplete(sender, alias, args);
		} else if (args.length == 2) {

			Player target = getPlugin().getServer().getPlayer(args[0]);

			if (target == null) {
				return List.of();
			}

			Collection<ServerWaypoint> waypoints = getCompletableWaypoints(getPlugin().getDataManager().getUserData(target.getUniqueId()));

			return waypoints.stream().map(Waypoint::getId)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).toList();

		} else if (args.length == 3) {
			return List.of("false");
		}
		return List.of();
	}

}
