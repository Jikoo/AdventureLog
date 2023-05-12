package com.github.jikoo.util;

import com.github.jikoo.AdventureLogPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public final class AdvLogPerms {

  public static final String MANAGE_SERVER;
  public static final String MANAGE_PLAYER;
  public static final String PERSONAL_UNLIMITED;

  static {
    Permission manage = getOrCreate("manage");
    manage.setDescription("Permission to manage all aspects of Adventure Log waypoints.");
    Permission manageServer = defineChild(manage, "server");
    manageServer.setDescription("Permission to manage server waypoints.");
    MANAGE_SERVER = manageServer.getName();
    Permission managePlayers = defineChild(manage, "other");
    managePlayers.setDescription("Permission to manage others' personal waypoints.");
    MANAGE_PLAYER = managePlayers.getName();

    Permission personalUnlimited = getOrCreate("personal.unlimited");
    personalUnlimited.setDescription("Permission to have unlimited personal waypoints.");
    PERSONAL_UNLIMITED = personalUnlimited.getName();
  }

  public static @NotNull Permission getOrCreate(@NotNull String node) {
    node = AdventureLogPlugin.NAMESPACE + '.' + node;
    PluginManager pluginManager = Bukkit.getPluginManager();
    Permission permission = pluginManager.getPermission(node);
    if (permission != null) {
      return permission;
    }
    permission = new Permission(node);
    pluginManager.addPermission(permission);
    return permission;
  }

  public static @NotNull Permission defineChild(@NotNull Permission parent, @NotNull String childNode) {
    Permission child = getOrCreate(parent.getName() + '.' + childNode);
    child.setDefault(PermissionDefault.FALSE);
    child.addParent(parent, true);
    return child;
  }

  public static int getPermittedPersonalWarps(@NotNull ConfigurationSection config, @NotNull Player player) {
    if (player.hasPermission(PERSONAL_UNLIMITED)) {
      return Integer.MAX_VALUE;
    }
    ConfigurationSection permissionSection = config.getConfigurationSection("personal.permissions");
    if (permissionSection == null) {
      return 0;
    }
    for (String key : permissionSection.getKeys(true)) {
      if (!permissionSection.isInt(key) || !player.hasPermission(AdventureLogPlugin.NAMESPACE + ".personal." + key)) {
        continue;
      }
      return permissionSection.getInt(key);
    }
    return 0;
  }

  private AdvLogPerms() {}

}
