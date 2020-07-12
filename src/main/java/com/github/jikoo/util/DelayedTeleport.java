package com.github.jikoo.util;

import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DelayedTeleport extends BukkitRunnable {

	private final BossBar bossBar;
	private final Player target;
	private final Vector originalLocation;
	private final Location teleportTo;
	private final int maxCycles;
	private int count = 0;

	public DelayedTeleport(Plugin plugin, Player target, Location teleportTo, int seconds) {
		this.target = target;
		this.teleportTo = teleportTo;
		bossBar = plugin.getServer().createBossBar("Concentrating...", BarColor.PURPLE, BarStyle.SEGMENTED_20);
		bossBar.setProgress(0);
		bossBar.addPlayer(target);
		originalLocation = target.getLocation().toVector();
		maxCycles = seconds * 10;
	}

	@Override
	public void run() {
		if (moved()) {
			target.sendMessage("You have to maintain concentration to remember the way!");
			bossBar.removeAll();
			cancel();
			return;
		}

		if (count >= maxCycles) {
			target.teleport(teleportTo);
			bossBar.removeAll();
			cancel();
			return;
		}
		this.bossBar.setProgress(count * 1D / maxCycles);
		++count;
	}

	private boolean moved() {
		if (!target.isOnline()) {
			return true;
		}
		Location newLocation = target.getLocation();
		double min = Math.min(originalLocation.getX(), newLocation.getX());
		double max = originalLocation.getX() == min ? newLocation.getX() : originalLocation.getX();
		if (Math.abs(max - min) > 1) {
			return true;
		}
		min = Math.min(originalLocation.getY(), newLocation.getY());
		max = originalLocation.getY() == min ? newLocation.getY() : originalLocation.getY();
		if (Math.abs(max - min) > 1) {
			return true;
		}
		min = Math.min(originalLocation.getZ(), newLocation.getZ());
		max = originalLocation.getZ() == min ? newLocation.getZ() : originalLocation.getZ();
		return Math.abs(max - min) > 1;
	}

}
