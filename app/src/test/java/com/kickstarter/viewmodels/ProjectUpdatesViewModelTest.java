package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UpdateFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.apiresponses.UpdatesEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class ProjectUpdatesViewModelTest extends KSRobolectricTestCase {
  private ProjectUpdatesViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> isFetchingUpdates = new TestSubscriber<>();
  private final TestSubscriber<List<Update>> updates = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Update>> startUpdateActivity = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment env, final @NonNull Project project) {
    this.vm = new ProjectUpdatesViewModel.ViewModel(env);
    this.vm.outputs.isFetchingUpdates().subscribe(this.isFetchingUpdates);
    this.vm.outputs.startUpdateActivity().subscribe(this.startUpdateActivity);
    this.vm.outputs.updates().subscribe(this.updates);

    // Configure the view model with a project intent.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
  }

  @Test
  public void testIsFetchingUpdates() {
    setUpEnvironment(environment(), ProjectFactory.project());

    this.isFetchingUpdates.assertValues(true, false);
    this.koalaTest.assertValue("Viewed Updates");
  }

  @Test
  public void testStartUpdateActivity() {
    final Update update = UpdateFactory.update();
    final List<Update> updates = Collections.singletonList(update);

    final Project project = ProjectFactory.project();
    setUpEnvironment(environment().toBuilder().apiClient(new MockApiClient() {
      @NonNull
      @Override
      public Observable<UpdatesEnvelope> fetchUpdates(@NonNull Project project) {
        return Observable.just(
          UpdatesEnvelope
            .builder()
            .updates(updates)
            .urls(urlsEnvelope())
            .build()
        );
      }
    }).build(), project);

    this.vm.inputs.updateClicked(update);

    this.startUpdateActivity.assertValues(Pair.create(project, update));
    this.koalaTest.assertValues("Viewed Updates", "Viewed Update");
  }

  @Test
  public void testUpdates() {
    final List<Update> updates = Arrays.asList(
      UpdateFactory.update(),
      UpdateFactory.update()
    );

    final Project project = ProjectFactory.project();
    setUpEnvironment(environment().toBuilder().apiClient(new MockApiClient() {
      @NonNull
      @Override
      public Observable<UpdatesEnvelope> fetchUpdates(@NonNull Project project) {
        return Observable.just(
          UpdatesEnvelope
            .builder()
            .updates(updates)
            .urls(urlsEnvelope())
            .build()
        );
      }
    }).build(), project);

    this.updates.assertValue(updates);
    this.koalaTest.assertValue("Viewed Updates");
  }

  private UpdatesEnvelope.UrlsEnvelope urlsEnvelope() {
    return UpdatesEnvelope.UrlsEnvelope
      .builder()
      .api(
        UpdatesEnvelope.UrlsEnvelope.ApiEnvelope
          .builder()
          .moreUpdates("http://more.updates.please")
          .build()
      )
      .build();
  }
}
