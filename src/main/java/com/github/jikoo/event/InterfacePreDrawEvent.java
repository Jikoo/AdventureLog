package com.github.jikoo.event;

import com.github.jikoo.ui.SimpleUI;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class InterfacePreDrawEvent extends Event {

    private final @NotNull SimpleUI simpleInterface;

    public InterfacePreDrawEvent(@NotNull SimpleUI simpleInterface) {
        this.simpleInterface = simpleInterface;
    }

    public SimpleUI getInterface() {
        return simpleInterface;
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
