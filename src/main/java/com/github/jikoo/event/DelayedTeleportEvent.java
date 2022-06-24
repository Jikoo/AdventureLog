package com.github.jikoo.event;

import com.github.jikoo.data.IWaypoint;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DelayedTeleportEvent extends WaypointEvent {

    private final @NotNull Player player;
    private int delaySeconds;

    public DelayedTeleportEvent(@NotNull Player player, @NotNull IWaypoint waypoint, int delaySeconds) {
        super(waypoint);
        this.player = player;
        this.delaySeconds = delaySeconds;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int seconds) {
        this.delaySeconds = Math.max(0, seconds);
    }

}
