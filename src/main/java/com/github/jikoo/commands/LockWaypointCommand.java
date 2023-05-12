package com.github.jikoo.commands;

import com.github.jikoo.AdventureLogPlugin;
import com.github.jikoo.data.ServerWaypoint;
import com.github.jikoo.data.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiPredicate;

public class LockWaypointCommand extends ManageUnlockedWaypointsCommand {

  public LockWaypointCommand(@NotNull AdventureLogPlugin plugin) {
    super(plugin, "lockwaypoint");
    setDescription("Lock an Adventure Log waypoint.");
  }

  @Override
  protected @NotNull String getSuccessFeedback() {
    return "Locked waypoint!";
  }

  @Override
  protected @NotNull BiPredicate<UserData, String> waypointStateSetter() {
    return UserData::lockWaypoint;
  }

  @Override
  @NotNull
  Collection<ServerWaypoint> getCompletableWaypoints(@NotNull UserData userData) {
    Collection<ServerWaypoint> waypoints = getPlugin().getDataManager().getServerData().getWaypoints();
    Collection<ServerWaypoint> unlocked = userData.getUnlockedWaypoints();
    return waypoints.stream().filter(waypoint -> unlocked.stream().noneMatch(waypoint::equals)).toList();
  }

}
