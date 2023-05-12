package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiPredicate;

public class UnlockWaypointCommand  extends ManageUnlockedWaypointsCommand {

  public UnlockWaypointCommand(@NotNull AdventureLogPlugin plugin) {
    super(plugin, "unlockwaypoint");
    setDescription("Unlock an Adventure Log waypoint.");
  }

  @Override
  protected @NotNull String getSuccessFeedback() {
    return "Unlocked waypoint!";
  }

  @Override
  protected @NotNull BiPredicate<UserData, String> waypointStateSetter() {
    return UserData::unlockWaypoint;
  }

  @Override
  @NotNull Collection<ServerWaypoint> getCompletableWaypoints(@NotNull UserData userData) {
    return userData.getUnlockedWaypoints();
  }

}
