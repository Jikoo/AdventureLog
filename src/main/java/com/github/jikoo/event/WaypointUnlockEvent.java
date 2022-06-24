package com.github.jikoo.event;

import com.github.jikoo.data.IWaypoint;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WaypointUnlockEvent extends WaypointEvent implements Cancellable {

    private final @NotNull UUID user;

    public WaypointUnlockEvent(@NotNull UUID user, @NotNull IWaypoint waypoint) {
        super(waypoint);
        this.user = user;
    }

    public @NotNull UUID getUser() {
        return user;
    }

    // Cancellable requirements

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
