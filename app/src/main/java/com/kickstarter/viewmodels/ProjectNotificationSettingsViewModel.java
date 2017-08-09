package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity;
import com.kickstarter.viewmodels.errors.ProjectNotificationSettingsViewModelErrors;
import com.kickstarter.viewmodels.outputs.ProjectNotificationSettingsViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ProjectNotificationSettingsViewModel extends ActivityViewModel<ProjectNotificationSettingsActivity> implements
  ProjectNotificationSettingsViewModelOutputs, ProjectNotificationSettingsViewModelErrors {

  public ProjectNotificationSettingsViewModel(final @NonNull Environment environment) {
    super(environment);

    final ApiClientType client = environment.apiClient();

    this.projectNotifications = client.fetchProjectNotifications()
      .compose(Transformers.pipeErrorsTo(this.unableToFetchProjectNotificationsError));
  }

  private Observable<List<ProjectNotification>> projectNotifications;

  private final PublishSubject<Throwable> unableToFetchProjectNotificationsError = PublishSubject.create();

  public final ProjectNotificationSettingsViewModelOutputs outputs = this;
  public final ProjectNotificationSettingsViewModelErrors errors = this;

  public Observable<List<ProjectNotification>> projectNotifications() {
    return this.projectNotifications;
  }

  public Observable<Void> unableToFetchProjectNotificationsError() {
    return this.unableToFetchProjectNotificationsError
      .map(__ -> null);
  }
}
