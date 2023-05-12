package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.util.AdvLogPerms;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GiveLogCommand extends PluginCommand<AdventureLogPlugin> {

	private final String permissionGiveOther;

	public GiveLogCommand(@NotNull AdventureLogPlugin plugin) {
		super(plugin, "givelog");
		setDescription("Give a user an Adventure Log.");
		setUsage("/<command> [player]");

		Permission parent = AdvLogPerms.getOrCreate("give");
		parent.setDescription("Permission to spawn adventure logs using commands.");
		Permission child = AdvLogPerms.defineChild(parent, "self");
		child.setDescription("Permission to spawn adventure logs for self.");

		permissionGiveOther = parent.getName();
		setPermission(permissionGiveOther + ';' + child.getName());
	}

	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull String alias,
			@NotNull String @NotNull [] args) {
		if (args.length < 1 && !(sender instanceof Player)) {
			return false;
		}

		Player target;
		if (!(sender instanceof Player) || sender.hasPermission(permissionGiveOther) && args.length > 0) {
			target = getPlugin().getServer().getPlayer(args[0]);
		} else {
			target = (Player) sender;
		}

		if (target == null) {
			sender.sendMessage("Invalid recipient!");
			return true;
		}

		Map<Integer, ItemStack> addFailures = target.getInventory().addItem(getPlugin().getWaypointBook());

		if (addFailures.size() > 0) {
			sender.sendMessage(String.format("%s's inventory is full! ", target.getName()));
		}

		return true;
	}

	@Override
	public @NotNull List<String> onTabComplete(
					@NotNull CommandSender sender,
					@NotNull String alias,
					@NotNull String @NotNull [] args) {
		return args.length == 1 && sender.hasPermission(permissionGiveOther)
						? super.onTabComplete(sender, alias, args) : List.of();
	}

}
