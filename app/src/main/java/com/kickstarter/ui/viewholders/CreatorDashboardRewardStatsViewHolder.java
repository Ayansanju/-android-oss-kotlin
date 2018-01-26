package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardRewardStatsAdapter;
import com.kickstarter.viewmodels.CreatorDashboardRewardStatsHolderViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardRewardStatsViewHolder extends KSViewHolder {
  private final CreatorDashboardRewardStatsHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.dashboard_reward_stats_empty_text_view) TextView emptyTextView;
  protected @Bind(R.id.dashboard_reward_stats_pledged_view) View pledgedColumnView;
  protected @Bind(R.id.dashboard_reward_stats_truncated_text_view) TextView truncatedTextView;
  protected @Bind(R.id.dashboard_reward_stats_recycler_view) RecyclerView rewardStatsRecyclerView;

  public CreatorDashboardRewardStatsViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    final CreatorDashboardRewardStatsAdapter rewardStatsAdapter = new CreatorDashboardRewardStatsAdapter();
    this.rewardStatsRecyclerView.setAdapter(rewardStatsAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    this.rewardStatsRecyclerView.setLayoutManager(layoutManager);

    this.viewModel.outputs.projectAndRewardStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardStatsAdapter::takeProjectAndStats);

    this.viewModel.outputs.rewardsStatsListShouldBeGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::toggleRecyclerViewAndEmptyStateVisibility);

    this.viewModel.outputs.rewardsStatsTruncatedTextIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(truncatedTextView));
  }

  private void toggleRecyclerViewAndEmptyStateVisibility(boolean gone) {
    ViewUtils.setGone(rewardStatsRecyclerView, gone);
    ViewUtils.setGone(emptyTextView, !gone);
  }

  @OnClick(R.id.dashboard_reward_stats_pledged_view)
  public void pledgedColumnTitleClicked() {
    this.viewModel.inputs.pledgedColumnTitleClicked();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStats = requireNonNull((Pair<Project, List<ProjectStatsEnvelope.RewardStats>>) data);
    this.viewModel.inputs.projectAndRewardStatsInput(projectAndRewardStats);
  }

  @Override
  protected void destroy() {
    super.destroy();
    this.rewardStatsRecyclerView.setAdapter(null);
  }
}
