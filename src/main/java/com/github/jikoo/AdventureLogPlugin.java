package com.github.jikoo;

import com.github.jikoo.commands.GiveLogCommand;
import com.github.jikoo.commands.LockWaypointCommand;
import com.github.jikoo.commands.OpenLogCommand;
import com.github.jikoo.commands.UnlockWaypointCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdventureLogPlugin extends JavaPlugin {

	public static final String NAMESPACE = "adventurelog";
	public static final NamespacedKey ADVENTURE_LOG_KEY = new NamespacedKey(NAMESPACE, "adventure_log");
	private ItemStack waypointBook;
	private DataManager dataManager;

	@Override
	public void onEnable() {
		try {
			Class.forName("io.papermc.paper.configuration.Configuration");
		} catch (ClassNotFoundException e) {
			//noinspection deprecation
			getLogger().severe(() -> getDescription().getName() + " requires modern Paper or a derivative because of its support for advanced text formatting on item tooltips.");
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

		getServer().getCommandMap().registerAll(
				NAMESPACE,
				List.of(
						new GiveLogCommand(this),
						new OpenLogCommand(this),
						new UnlockWaypointCommand(this),
						new LockWaypointCommand(this)
				)
		);
	}

	@Override
	public void onDisable() {
		this.dataManager.save();
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

}
