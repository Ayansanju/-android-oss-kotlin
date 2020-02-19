package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.UpdatesEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectUpdatesActivity;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectUpdatesViewModel {

  interface Inputs {
    /** Call when a project update uri request has been made. */
    void goToUpdate(Update update);

    /** Invoke when pagination should happen.*/
    void nextPage();

    /** Invoke when the feed should be refreshed. */
    void refresh();
  }

  interface Outputs {
    /** Emits a boolean indicating whether updates are being fetched from the API. */
    Observable<Boolean> isFetchingUpdates();

    /** Emits the current project and its updates. */
    Observable<Pair<Project, List<Update>>> projectAndUpdates();

    /** Emits a project and an update to start the update activity with. */
    Observable<Pair<Project, Update>> startUpdateActivity();
  }

  final class ViewModel extends ActivityViewModel<ProjectUpdatesActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final Observable<Project> project = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .take(1);

      final Observable<Project> startOverWith = Observable.merge(
        project,
        project.compose(takeWhen(this.refresh))
      );

      final ApiPaginator<Update, UpdatesEnvelope, Project> paginator =
        ApiPaginator.<Update, UpdatesEnvelope, Project>builder()
          .nextPage(this.nextPage)
          .startOverWith(startOverWith)
          .envelopeToListOfData(UpdatesEnvelope::updates)
          .envelopeToMoreUrl(env -> env.urls().api().moreUpdates())
          .loadWithParams(this.client::fetchUpdates)
          .loadWithPaginationPath(this.client::fetchUpdates)
          .clearWhenStartingOver(false)
          .concater(ListUtils::concatDistinct)
          .build();

      project
        .compose(combineLatestPair(paginator.paginatedData().share()))
        .compose(bindToLifecycle())
        .subscribe(this.projectAndUpdates);

      paginator
        .isFetching()
        .compose(bindToLifecycle())
        .subscribe(this.isFetchingUpdates);

      project
        .compose(takePairWhen(this.goToUpdate))
        .compose(bindToLifecycle())
        .subscribe(this.startUpdateActivity::onNext);

      project
        .compose(takeWhen(this.goToUpdate))
        .compose(bindToLifecycle())
        .subscribe(p -> this.koala.trackViewedUpdate(p, KoalaContext.Update.UPDATES));

      project
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedUpdates);
    }

    private final PublishSubject<Update> goToUpdate = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();

    private final BehaviorSubject<Boolean> isFetchingUpdates = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Project, List<Update>>> projectAndUpdates = BehaviorSubject.create();
    private final PublishSubject<Pair<Project, Update>> startUpdateActivity = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void goToUpdate(final @NonNull Update update) {
      this.goToUpdate.onNext(update);
    }
    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void refresh() {
      this.refresh.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> isFetchingUpdates() {
      return this.isFetchingUpdates;
    }
    @Override public @NonNull Observable<Pair<Project, List<Update>>> projectAndUpdates() {
      return this.projectAndUpdates;
    }
    @Override public @NonNull Observable<Pair<Project, Update>> startUpdateActivity() {
      return this.startUpdateActivity;
    }
  }
}
