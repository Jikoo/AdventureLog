package com.github.jikoo;

import com.github.jikoo.commands.AddWaypointCommand;
import com.github.jikoo.commands.GiveLogCommand;
import com.github.jikoo.commands.ManageDefaultWaypointsCommand;
import com.github.jikoo.commands.ManageUnlockedWaypointsCommand;
import com.github.jikoo.commands.RemoveWaypointCommand;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdventureLogPlugin extends JavaPlugin {

	private ItemStack waypointBook;
	private DataStore dataStore;

	@Override
	public void onEnable() {
		this.dataStore = new DataStore(this);

		NamespacedKey waypointRecipeKey = new NamespacedKey(this, "adventure_log");
		ShapelessRecipe recipe = new ShapelessRecipe(waypointRecipeKey, new ItemStack(Material.KNOWLEDGE_BOOK));
		recipe.addIngredient(Material.PETRIFIED_OAK_SLAB);
		getServer().addRecipe(recipe);

		waypointBook = new ItemStack(Material.KNOWLEDGE_BOOK);
		ItemMeta itemMeta = waypointBook.getItemMeta();

		if (itemMeta instanceof KnowledgeBookMeta) {
			KnowledgeBookMeta bookMeta = (KnowledgeBookMeta) itemMeta;
			bookMeta.setDisplayName(ChatColor.DARK_PURPLE + "Adventure Log");
			bookMeta.addRecipe(waypointRecipeKey);
			waypointBook.setItemMeta(bookMeta);
		}

		getServer().getPluginManager().registerEvents(new AdventureLogListener(this), this);

		addTabExecutor("givelog", new GiveLogCommand(this));
		addTabExecutor("addlogwaypoint", new AddWaypointCommand(this));
		addTabExecutor("removelogwaypoint", new RemoveWaypointCommand(this));
		TabExecutor executor = new ManageDefaultWaypointsCommand(this);
		addTabExecutor("adddefaultlogwaypoint", executor);
		addTabExecutor("removedefaultlogwaypoint", executor);
		executor = new ManageUnlockedWaypointsCommand(this);
		addTabExecutor("unlocklogwaypoint", executor);
		addTabExecutor("locklogwaypoint", executor);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> getServer().getOnlinePlayers().forEach(player -> {
			if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
					|| !player.getInventory().contains(getWaypointBook())) {
				// Require survival/adventure and waypoint book in inventory to discover waypoints
				return;
			}

			getDataStore().getWaypoints().stream().filter(waypoint ->
					getDataStore().getDefaultWaypoints().stream().noneMatch(waypoint::equals)
							&& getDataStore().getWaypoints(player.getUniqueId()).stream().noneMatch(waypoint::equals))
					.forEach(waypoint -> {
						if (player.getWorld().equals(waypoint.getLocation().getWorld())
								&& player.getLocation().distanceSquared(waypoint.getLocation()) <= 900
								&& getDataStore().unlockWaypoint(player.getUniqueId(), waypoint.getName()) == DataStore.Result.SUCCESS) {
							player.sendTitle("Waypoint discovered!", "Check your Adventure Log.", 10, 50, 20);
						}
					});
			}
		), 60L, 60L);
	}

	private void addTabExecutor(String command, TabExecutor tabExecutor) {
		PluginCommand pluginCommand = getCommand(command);

		if (pluginCommand == null) {
			return;
		}

		pluginCommand.setExecutor(tabExecutor);
		pluginCommand.setTabCompleter(tabExecutor);
	}

	@Override
	public void onDisable() {
		dataStore.save();
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {
		if (args.length == 0) {
			return ImmutableList.of();
		}

		String lastWord = args[args.length - 1];
		Player senderPlayer = sender instanceof Player ? (Player) sender : null;

		return getServer().getOnlinePlayers().stream().map(player -> {
			if ((senderPlayer == null || senderPlayer.canSee(player))
					&& StringUtil.startsWithIgnoreCase(player.getName(), lastWord)) {
				return player.getName();
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public DataStore getDataStore() {
		return this.dataStore;
	}

	public @NotNull ItemStack getWaypointBook() {
		return waypointBook.clone();
	}

	boolean isWaypointBook(@NotNull ItemStack itemStack) {
		if (itemStack.getType() != Material.KNOWLEDGE_BOOK || !itemStack.hasItemMeta()) {
			return false;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!(itemMeta instanceof KnowledgeBookMeta)) {
			return false;
		}

		return ((KnowledgeBookMeta) itemMeta).getRecipes().stream().anyMatch(key ->
				key.getNamespace().equals(getName().toLowerCase()) && key.getKey().equals("adventure_log"));
	}

}
