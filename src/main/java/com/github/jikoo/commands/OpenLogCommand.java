package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.impl.ServerWaypointUI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenLogCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public OpenLogCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		UUID target;
		if (sender.hasPermission("adventurelog.view.other") && args.length > 0) {
			try {
				target = UUID.fromString(args[0]);
			} catch (IllegalArgumentException e) {
				OfflinePlayer offline = plugin.getServer().getOfflinePlayer(args[0]);

				if (!offline.isOnline() && !offline.hasPlayedBefore()) {
					sender.sendMessage("Invalid recipient!");
					return true;
				}
				target = offline.getUniqueId();
			}
		} else {
			target = player.getUniqueId();
		}

		player.openInventory(new ServerWaypointUI(plugin, target, player).getInventory());
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		return args.length == 1 && sender.hasPermission("adventurelog.view.other")
				? plugin.onTabComplete(sender, command, label, args) : Collections.emptyList();
	}

}
