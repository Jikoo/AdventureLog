package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.Waypoint;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddWaypointCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public AddWaypointCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Waypoints can only be created by a player!");
			return true;
		}

		if (args.length < 1) {
			return false;
		}

		Player senderPlayer = (Player) sender;

		if (senderPlayer.getInventory().getItemInMainHand().getType() == Material.AIR) {
			sender.sendMessage("Must have an item in hand to assign waypoint to!" +
					"\nIt is highly recommended that you customize the item for the waypoint.");
			return true;
		}

		int priority = 0;
		if (args.length > 1) {
			try {
				priority = Integer.parseInt(args[1]);
			} catch (NumberFormatException ignored) {}
		}

		Waypoint waypoint = new Waypoint.Builder(args[0]).setIcon(senderPlayer.getInventory().getItemInMainHand())
				.setLocation(senderPlayer.getLocation()).setPriority(priority).build();

		switch (plugin.getDataStore().addWaypoint(waypoint)) {
			case SUCCESS:
			case UNNECESSARY:
				String lastArg = args[args.length - 1];
				if (args.length > 1 && (lastArg.equalsIgnoreCase("default") || lastArg.equalsIgnoreCase("true"))) {
					plugin.getDataStore().addDefault(waypoint.getName());
				}

				sender.sendMessage("Added waypoint successfully!");
				return true;
			case FAILURE:
			default:
				sender.sendMessage("Failed to add waypoint! Please check server logs.");
				return true;
		}

	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (args.length == 1) {
			return plugin.getDataStore().getWaypoints().stream().map(Waypoint::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[0])).collect(Collectors.toList());
		} else if (args.length == 2) {
			// TODO complete numbers
			return Collections.singletonList("default");
		} else if (args.length == 3) {
			return Collections.singletonList("default");
		}
		return Collections.emptyList();
	}

}
