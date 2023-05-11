package com.github.jikoo;

import com.github.jikoo.commands.GiveLogCommand;
import com.github.jikoo.commands.ManageUnlockedWaypointsCommand;
import com.github.jikoo.commands.OpenLogCommand;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdventureLogPlugin extends JavaPlugin {

	public static final String NAMESPACE = "adventurelog";
	public static final NamespacedKey ADVENTURE_LOG_KEY = new NamespacedKey(NAMESPACE, "adventure_log");
	private ItemStack waypointBook;
	private DataManager dataManager;

	@Override
	public void onEnable() {
		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
		} catch (ClassNotFoundException e) {
			getLogger().severe(() -> getDescription().getName() + " requires Paper.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig();

		this.dataManager = new DataManager(this);

		ShapelessRecipe recipe = new ShapelessRecipe(ADVENTURE_LOG_KEY, new ItemStack(Material.KNOWLEDGE_BOOK));
		recipe.addIngredient(Material.PETRIFIED_OAK_SLAB);
		getServer().addRecipe(recipe);

		waypointBook = new ItemStack(Material.KNOWLEDGE_BOOK);
		ItemMeta itemMeta = waypointBook.getItemMeta();

		if (itemMeta instanceof KnowledgeBookMeta bookMeta) {
			bookMeta.displayName(Component.text("Adventure Log").color(NamedTextColor.DARK_PURPLE));
			bookMeta.addRecipe(ADVENTURE_LOG_KEY);
			waypointBook.setItemMeta(bookMeta);
		}

		getServer().getPluginManager().registerEvents(new AdventureLogListener(this), this);

		addExecutor("givelog", new GiveLogCommand(this));
		addExecutor("openlog", new OpenLogCommand(this));
		TabExecutor executor = new ManageUnlockedWaypointsCommand(this);
		addExecutor("unlocklogwaypoint", executor);
		addExecutor("locklogwaypoint", executor);
	}

	@Override
	public void onDisable() {
		this.dataManager.save();
	}

	private void addExecutor(String command, TabExecutor tabExecutor) {
		PluginCommand pluginCommand = getCommand(command);

		if (pluginCommand == null) {
			return;
		}

		pluginCommand.setExecutor(tabExecutor);
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
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

	public DataManager getDataManager() {
		return this.dataManager;
	}

	public @NotNull ItemStack getWaypointBook() {
		return waypointBook.clone();
	}

	public boolean isWaypointBook(@Nullable ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() != Material.KNOWLEDGE_BOOK || !itemStack.hasItemMeta()) {
			return false;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!(itemMeta instanceof KnowledgeBookMeta)) {
			return false;
		}

		return ((KnowledgeBookMeta) itemMeta).getRecipes().stream().anyMatch(key -> key.equals(ADVENTURE_LOG_KEY));
	}

	public int getPermittedPersonalWarps(@NotNull Player player) {
		if (player.hasPermission("adventurelog.personal.unlimited")) {
			return Integer.MAX_VALUE;
		}
		ConfigurationSection permissionSection = this.getConfig().getConfigurationSection("personal.permissions");
		if (permissionSection == null) {
			return 0;
		}
		for (String key : permissionSection.getKeys(true)) {
			if (!permissionSection.isInt(key) || !player.hasPermission("adventurelog.personal." + key)) {
				continue;
			}
			return permissionSection.getInt(key);
		}
		return 0;
	}

}
