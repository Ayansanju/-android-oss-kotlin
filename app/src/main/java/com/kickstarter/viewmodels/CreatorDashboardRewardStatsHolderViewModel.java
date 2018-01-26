package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ComparatorUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public interface CreatorDashboardRewardStatsHolderViewModel {

  interface Inputs {
    /** Call when user clicks pledged column title. */
    void pledgedColumnTitleClicked();

    /** Current project and list of stats. */
    void projectAndRewardStatsInput(Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStatsEnvelope);
  }

  interface Outputs {
    /** Emits current project and sorted reward stats. */
    Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats();

    /** Emits when there are no reward stats. */
    Observable<Boolean> rewardsStatsListShouldBeGone();

    /** Emits when there are more than 10 reward stats. */
    Observable<Boolean> rewardsStatsTruncatedTextIsGone();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<List<ProjectStatsEnvelope.RewardStats>> sortedRewardStats = this.projectAndRewardStatsInput
        .map(PairUtils::second)
        .map(this::sortRewardStats);

      this.projectAndRewardStats = this.projectAndRewardStatsInput
        .map(PairUtils::first)
        .compose(combineLatestPair(sortedRewardStats));

      this.projectAndRewardStats
        .map(pr -> pr.second.isEmpty())
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.rewardsStatsListIsGone);

      this.projectAndRewardStats
        .map(pr -> pr.second.size() <= 10)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.rewardsStatsTruncatedTextIsGone);
    }

    final private class OrderByPledgedRewardStatsComparator implements Comparator<ProjectStatsEnvelope.RewardStats> {
      @Override
      public int compare(final ProjectStatsEnvelope.RewardStats o1, final ProjectStatsEnvelope.RewardStats o2) {
        return new ComparatorUtils.DescendingOrderFloatComparator().compare(o1.pledged(), o2.pledged());
      }
    }

    private @NonNull List<ProjectStatsEnvelope.RewardStats> sortRewardStats(final @NonNull List<ProjectStatsEnvelope.RewardStats> rewardStatsList) {
      final OrderByPledgedRewardStatsComparator rewardStatsComparator = new OrderByPledgedRewardStatsComparator();
      final Set<ProjectStatsEnvelope.RewardStats> rewardStatsTreeSet = new TreeSet<>(rewardStatsComparator);
      rewardStatsTreeSet.addAll(rewardStatsList);

      return new ArrayList<>(rewardStatsTreeSet);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Void> pledgedColumnTitleClicked = PublishSubject.create();
    private final PublishSubject<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStatsInput = PublishSubject.create();

    private final Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats;
    private final PublishSubject<Boolean> rewardsStatsListIsGone = PublishSubject.create();
    private final PublishSubject<Boolean> rewardsStatsTruncatedTextIsGone = PublishSubject.create();

    @Override
    public void pledgedColumnTitleClicked() {
      this.pledgedColumnTitleClicked.onNext(null);
    }
    @Override
    public void projectAndRewardStatsInput(final @NonNull Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStats) {
      this.projectAndRewardStatsInput.onNext(projectAndRewardStats);
    }

    @Override public @NonNull Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats() {
      return this.projectAndRewardStats;
    }
    @Override public @NonNull Observable<Boolean> rewardsStatsListShouldBeGone() {
      return this.rewardsStatsListIsGone;
    }
    @Override public @NonNull Observable<Boolean> rewardsStatsTruncatedTextIsGone() {
      return this.rewardsStatsTruncatedTextIsGone;
    }
  }
}
