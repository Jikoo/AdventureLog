package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GiveLogCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public GiveLogCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		if (args.length < 1 && !(sender instanceof Player)) {
			return false;
		}
		Player target;
		if (!(sender instanceof Player) || sender.hasPermission("adventurelog.givelog.other") && args.length > 0) {
			target = plugin.getServer().getPlayer(args[0]);
		} else {
			target = (Player) sender;
		}

		if (target == null) {
			sender.sendMessage("Invalid recipient!");
			return true;
		}

		HashMap<Integer, ItemStack> addFailures = target.getInventory().addItem(plugin.getWaypointBook());

		if (addFailures.size() > 0) {
			sender.sendMessage(String.format("%s's inventory is full! ", target.getName()));
		}

		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		return args.length == 1 ? plugin.onTabComplete(sender, command, alias, args) : Collections.emptyList();
	}

}
