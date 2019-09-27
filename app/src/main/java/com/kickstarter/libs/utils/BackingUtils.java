package com.kickstarter.libs.utils;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import androidx.annotation.NonNull;

public final class BackingUtils {
  private BackingUtils() {}

  public static boolean isBacked(final @NonNull Project project, final @NonNull Reward reward) {
    final Backing backing = project.backing();
    if (backing == null) {
      return false;
    }

    final Long rewardId = backing.rewardId();
    if (rewardId == null) {
      return RewardUtils.isNoReward(reward);
    }

    return rewardId == reward.id();
  }

  public static boolean isShippable(final @NonNull Backing backing) {
    final Reward reward = backing.reward();
    if (reward == null) {
      return false;
    }
    return RewardUtils.isShippable(reward);
  }
}
