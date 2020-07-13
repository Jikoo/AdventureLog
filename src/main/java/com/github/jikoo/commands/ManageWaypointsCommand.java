package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.ui.WaypointEditorUI;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManageWaypointsCommand implements TabExecutor {

	private final AdventureLogPlugin plugin;

	public ManageWaypointsCommand(AdventureLogPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Waypoints can only be edited by a player!");
			return true;
		}

		Player player = (Player) sender;
		player.openInventory(new WaypointEditorUI(plugin, player.getUniqueId(), player).getInventory());

		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return Collections.emptyList();
	}

}
