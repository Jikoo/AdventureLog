package com.github.jikoo.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PluginCommand<T extends Plugin> extends Command implements PluginIdentifiableCommand {

  private final T plugin;

  protected PluginCommand(@NotNull T plugin, @NotNull String name) {
    super(name);
    this.plugin = plugin;
  }

  @Override
  public @NotNull T getPlugin() {
    return plugin;
  }

  public final boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    if (!this.plugin.isEnabled()) {
      throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin " + this.plugin.getName() + " - plugin is disabled.");
    }
    if (!this.testPermission(sender)) {
      return true;
    }

    boolean success;
    try {
      success = onCommand(sender, args);
    } catch (Throwable exception) {
      throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + this.plugin.getName(), exception);
    }

    if (!success && !this.usageMessage.isEmpty()) {
      String[] usage = this.usageMessage.replace("<command>", commandLabel).split("\n");
      for (String line : usage) {
        sender.sendMessage(line);
      }
    }

    return success;
  }

  protected abstract boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args);

  public final @NotNull List<String> tabComplete(
          @NotNull CommandSender sender,
          @NotNull String alias,
          @NotNull String[] args)
          throws CommandException {
    if (!this.testPermissionSilent(sender)) {
      return List.of();
    }

    try {
      return onTabComplete(sender, alias, args);
    } catch (Throwable throwable) {
      StringBuilder message = new StringBuilder();
      message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

      for (String arg : args) {
        message.append(arg).append(' ');
      }

      message.deleteCharAt(message.length() - 1).append("' in plugin ").append(this.plugin.getName());
      throw new CommandException(message.toString(), throwable);
    }
  }

  protected @NotNull List<String> onTabComplete(
          @NotNull CommandSender sender,
          @NotNull String alias,
          @NotNull String[] args) {
    if (args.length == 0) {
      return ImmutableList.of();
    }

    String lastWord = args[args.length - 1];
    Player senderPlayer = sender instanceof Player ? (Player) sender : null;

    return plugin.getServer().getOnlinePlayers().stream().map(player -> {
      if ((senderPlayer == null || senderPlayer.canSee(player))
              && StringUtil.startsWithIgnoreCase(player.getName(), lastWord)) {
        return player.getName();
      }
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

}
