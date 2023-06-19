package com.github.jikoo.data;

import com.github.jikoo.util.AlphanumComparator;
import com.github.jikoo.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserWaypoint extends Waypoint {

	public static final Comparator<UserWaypoint> COMPARATOR = new Comparator<>() {
		private final Comparator<String> alphanumComparator = new AlphanumComparator();

		@Override
		public int compare(@NotNull UserWaypoint o1, @NotNull UserWaypoint o2) {
			return alphanumComparator.compare(o1.getSortingName(), o2.getSortingName());
		}
	};

	UserWaypoint(@NotNull UserData storage, @NotNull String name) {
		super(storage, name);
	}

	@Override
	public @NotNull String getId() {
		String customName = this.getString("custom_name");
		return customName != null ? customName : super.getId();
	}

	private @NotNull String getSortingName() {
		String comparableName = this.getString("comparable_name");
		return comparableName != null ? comparableName : this.getId();
	}

	@Override
	public @NotNull ItemStack getIcon() {
		ItemStack icon = this.getItemStack("icon");
		if (icon == null || icon.getType().isAir()) {
			List<Material> values = new ArrayList<>(Tag.BEDS.getValues());
			int index = this.getId().hashCode() % values.size();
			icon = new ItemStack(values.get(index));
		}
		ItemMeta itemMeta = icon.getItemMeta();
		if (itemMeta != null && !itemMeta.hasDisplayName()) {
			itemMeta.displayName(TextUtil.itemText("Home " + this.getId()).color(NamedTextColor.GOLD));
			icon.setItemMeta(itemMeta);
		}
		return icon;
	}

	@Override
	public void setIcon(@NotNull ItemStack icon) {
		super.setIcon(icon);

		// Lift icon name for later use in sorting
		Component displayName = null;
		ItemMeta itemMeta = icon.getItemMeta();
		if (itemMeta != null && itemMeta.hasDisplayName()) {
			displayName = itemMeta.displayName();
		}

		this.set("custom_name", displayName);
		if (displayName == null) {
			this.set("comparable_name", null);
		} else {
			/*
			 * Strip down display name to form a more rapidly comparable value.
			 * This value is cached and reused rather than do a much more expensive
			 * (albeit slightly more accurate) Collator sort each time.
			 *
			 * Accents are intentionally not removed from the resulting normalized text
			 * so as to cause consistent sorting differences for unequal inputs.
			 */
			String comparableText = PlainTextComponentSerializer.plainText().serialize(displayName);
			comparableText = Normalizer.normalize(comparableText, Normalizer.Form.NFKD).toUpperCase();
			this.set("comparable_name", comparableText);
		}
	}

	@Override
	public void delete() {
		super.delete();
		raw().notifyDelete(this);
	}

	@Override
	@NotNull UserData raw() {
		return (UserData) super.raw();
	}

}
