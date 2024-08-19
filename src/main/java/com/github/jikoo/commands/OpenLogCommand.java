package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.impl.ServerWaypointUI;
import com.github.jikoo.util.AdvLogPerms;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class OpenLogCommand extends PluginCommand<AdventureLogPlugin> {

	private final String permissionOpenOther;

	public OpenLogCommand(@NotNull AdventureLogPlugin plugin) {
		super(plugin, "openlog");
		setDescription("Open the Adventure Log interface.");
		setUsage("/<command> [player]");

		Permission parent = AdvLogPerms.getOrCreate("open");
		parent.setDescription("Permission to open adventure logs using commands.");
		Permission child = AdvLogPerms.defineChild(parent, "self");
		child.setDescription("Permission to open adventure own adventure log via command.");

		permissionOpenOther = parent.getName();
		setPermission(permissionOpenOther + ';' + child.getName());
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player player)) {
			return true;
		}

		AdventureLogPlugin plugin = getPlugin();
		if (!sender.hasPermission(permissionOpenOther) || args.length == 0) {
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
							sender.sendMessage("Invalid target!");
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
	public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		return args.length == 1 && sender.hasPermission(permissionOpenOther)
						? super.onTabComplete(sender, label, args) : List.of();
	}

}
