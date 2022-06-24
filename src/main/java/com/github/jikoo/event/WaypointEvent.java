package com.github.jikoo.event;

import com.github.jikoo.data.IWaypoint;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WaypointEvent extends Event {

    private final @NotNull IWaypoint waypoint;

    public WaypointEvent(@NotNull IWaypoint waypoint) {
        this.waypoint = waypoint;
    }

    public @NotNull IWaypoint getWaypoint() {
        return waypoint;
    }

    // Generic event requirements.
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
