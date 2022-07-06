package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.impl.ServerWaypointUI;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class OpenLogCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public OpenLogCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			return true;
		}
		if (!sender.hasPermission("adventurelog.view.other") || args.length == 0) {
			player.openInventory(new ServerWaypointUI(plugin, player.getUniqueId(), player).getInventory());
			return true;
		}

		plugin.getServer().getScheduler().runTaskAsynchronously(
				plugin,
				() -> {
					UUID target;
					try {
						target = UUID.fromString(args[0]);
					} catch (IllegalArgumentException e) {
						OfflinePlayer offline = plugin.getServer().getOfflinePlayer(args[0]);

						if (!offline.isOnline() && !offline.hasPlayedBefore()) {
							sender.sendMessage("Invalid recipient!");
						}
						target = offline.getUniqueId();
					}
					UUID finalTarget = target;
					plugin.getServer().getScheduler().runTask(
							plugin,
							() -> player.openInventory(new ServerWaypointUI(plugin, finalTarget, player).getInventory()));
				});
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		return args.length == 1 && sender.hasPermission("adventurelog.view.other")
				? plugin.onTabComplete(sender, command, label, args) : List.of();
	}

}
