package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardHeaderHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardHeaderHolderViewModel.ViewModel vm;

  private final TestSubscriber<Boolean> messagesButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> otherProjectsButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFundedProgress = new TestSubscriber<>();
  private final TestSubscriber<String> projectBackersCountText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Integer> progressBarBackground = new TestSubscriber<>();
  private final TestSubscriber<String> timeRemainingText = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startMessageThreadsActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment);
    this.vm.outputs.messagesButtonIsGone().subscribe(this.messagesButtonIsGone);
    this.vm.outputs.otherProjectsButtonIsGone().subscribe(this.otherProjectsButtonIsGone);
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.outputs.percentageFundedProgress().subscribe(this.percentageFundedProgress);
    this.vm.outputs.progressBarBackground().subscribe(this.progressBarBackground);
    this.vm.outputs.startMessageThreadsActivity().subscribe(this.startMessageThreadsActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.timeRemainingText().subscribe(this.timeRemainingText);
  }

  @Test
  public void testMessagesButtonIsGone() {
    final User creator = UserFactory.creator();
    final CurrentUserType currentUser = new MockCurrentUser(UserFactory.user());

    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));

    // Messages button is gone if current user is not the project creator (e.g. a collaborator).
    this.messagesButtonIsGone.assertValues(true);
  }

  @Test
  public void testOtherProjectsButtonIsGone_WhenCreatorHas1Project() {
    final User creatorWith1Project = UserFactory.creator().toBuilder().createdProjectsCount(1).build();
    final CurrentUserType creator = new MockCurrentUser(creatorWith1Project);

    setUpEnvironment(environment().toBuilder().currentUser(creator).build());
    this.vm.inputs.projectAndStats(Pair.create(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope()));

    this.otherProjectsButtonIsGone.assertValues(true);
  }

  @Test
  public void testOtherProjectsButtonIsGone_WhenCreatorHasManyProjects() {
    final CurrentUserType creator = new MockCurrentUser(UserFactory.creator());

    setUpEnvironment(environment().toBuilder().currentUser(creator).build());
    this.vm.inputs.projectAndStats(Pair.create(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope()));

    this.otherProjectsButtonIsGone.assertValues(false);
  }

  @Test
  public void testProjectBackersCountText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(10).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.projectBackersCountText.assertValues("10");
  }

  @Test
  public void testProjectNameTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.projectNameTextViewText.assertValues("somebody once told me");
  }

  @Test
  public void testPercentageFunded() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    final String percentageFundedOutput = NumberUtils.flooredPercentage(project.percentageFunded());
    this.percentageFunded.assertValues(percentageFundedOutput);
    final int percentageFundedProgressOutput = ProgressBarUtils.progress(project.percentageFunded());
    this.percentageFundedProgress.assertValues(percentageFundedProgressOutput);
  }

  @Test
  public void testProgressBarBackground_LiveProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_LIVE));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_SubmittedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_SUBMITTED));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_StartedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_STARTED));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_SuccessfulProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_SUCCESSFUL));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_FailedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_FAILED));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testProgressBarBackground_CanceledProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_CANCELED));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testProgressBarBackground_SuspendedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.projectAndStats(getProjectAndStats(Project.STATE_SUSPENDED));
    this.progressBarBackground.assertValues(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testStartMessagesActivity() {
    final User creator = UserFactory.creator();
    final CurrentUserType currentUser = new MockCurrentUser(creator);
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.projectAndStats(Pair.create(project, ProjectStatsEnvelopeFactory.projectStatsEnvelope()));
    this.vm.inputs.messagesButtonClicked();

    // Messages button is shown to project creator, messages activity starts.
    this.messagesButtonIsGone.assertValues(false);
    this.startMessageThreadsActivity.assertValues(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testStartProjectActivity() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.vm.inputs.projectButtonClicked();
    this.startProjectActivity.assertValues(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testTimeRemainingText() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project().toBuilder().deadline(new DateTime().plusDays(10)).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final int deadlineVal = ProjectUtils.deadlineCountdownValue(project);

    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.timeRemainingText.assertValues(NumberUtils.format(deadlineVal));
  }

  private Pair<Project, ProjectStatsEnvelope> getProjectAndStats(final String state) {
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    final Project project = ProjectFactory.project().toBuilder().state(state).build();
    return Pair.create(project, projectStatsEnvelope);
  }
}
