package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.UserUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.SettingsActivity;
import com.kickstarter.ui.data.Newsletter;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface SettingsViewModel {

  interface Inputs {
    /** Call when the user dismiss the logout confirmation dialog. */
    void closeLogoutConfirmationClicked();

    /** Call when the user has confirmed that they want to log out. */
    void confirmLogoutClicked();

    /** Call when the user clicks on contact email. */
    void contactEmailClicked();

    /** Call when the user taps the logout button. */
    void logoutClicked();

    /** Call when the notify mobile of new followers toggle changes. */
    void notifyMobileOfFollower(boolean checked);

    /** Call when the notify mobile of friend backs a project toggle changes. */
    void notifyMobileOfFriendActivity(boolean checked);

    /** Call when the notify mobile of project updates toggle changes. */
    void notifyMobileOfUpdates(boolean checked);

    /** Call when the notify of new followers toggle changes. */
    void notifyOfFollower(boolean checked);

    /** Call when the notify of friend backs a project toggle changes. */
    void notifyOfFriendActivity(boolean checked);

    /** Call when the notify of project updates toggle changes. */
    void notifyOfUpdates(boolean checked);

    /** Call when the user toggles the Kickstarter Loves Games newsletter switch. */
    void sendGamesNewsletter(boolean checked);

    /** Call when the user toggles the Happening newsletter switch. */
    void sendHappeningNewsletter(boolean checked);

    /** Call when the user toggles the Kickstarter News & Events newsletter switch. */
    void sendPromoNewsletter(boolean checked);

    /** Call when the user toggles the Projects We Love newsletter switch. */
    void sendWeeklyNewsletter(boolean checked);
  }

  interface Outputs {
    /** Emits when its time to log the user out. */
    Observable<Void> logout();

    /** Emits a boolean that determines if the logout confirmation should be displayed. */
    Observable<Boolean> showConfirmLogoutPrompt();

    /** Show a dialog to inform the user that their newsletter subscription must be confirmed via email. */
    Observable<Newsletter> showOptInPrompt();

    /** Emits user containing settings state. */
    Observable<User> user();
  }

  interface Errors {
    Observable<String> unableToSavePreferenceError();
  }

  final class ViewModel extends ActivityViewModel<SettingsActivity> implements Inputs, Errors, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      this.client.fetchCurrentUser()
        .retry(2)
        .compose(Transformers.neverError())
        .compose(bindToLifecycle())
        .subscribe(this.currentUser::refresh);

      this.currentUser.observable()
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.userOutput::onNext);

      this.userInput
        .concatMap(this::updateSettings)
        .compose(bindToLifecycle())
        .subscribe(this::success);

      this.userInput
        .compose(bindToLifecycle())
        .subscribe(this.userOutput);

      this.userOutput
        .window(2, 1)
        .flatMap(Observable::toList)
        .map(ListUtils::first)
        .compose(takeWhen(this.unableToSavePreferenceError))
        .compose(bindToLifecycle())
        .subscribe(this.userOutput);

      this.currentUser.observable()
        .compose(takePairWhen(this.newsletterInput))
        .filter(us -> requiresDoubleOptIn(us.first, us.second.first))
        .map(us -> us.second.second)
        .compose(bindToLifecycle())
        .subscribe(this.showOptInPrompt);

      this.contactEmailClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackContactEmailClicked());

      this.newsletterInput
        .map(bs -> bs.first)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackNewsletterToggle);

      this.confirmLogoutClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.koala.trackLogout();
          this.logout.onNext(null);
        });

      this.koala.trackSettingsView();
    }

    private boolean requiresDoubleOptIn(final @NonNull User user, final boolean checked) {
      return UserUtils.isLocationGermany(user) && checked;
    }

    private void success(final @NonNull User user) {
      this.currentUser.refresh(user);
      this.updateSuccess.onNext(null);
    }

    private @NonNull Observable<User> updateSettings(final @NonNull User user) {
      return this.client.updateUserSettings(user)
        .compose(Transformers.pipeErrorsTo(this.unableToSavePreferenceError));
    }

    private final PublishSubject<Void> confirmLogoutClicked = PublishSubject.create();
    private final PublishSubject<Void> contactEmailClicked = PublishSubject.create();
    private final PublishSubject<Pair<Boolean, Newsletter>> newsletterInput = PublishSubject.create();
    private final PublishSubject<User> userInput = PublishSubject.create();

    private final BehaviorSubject<Void> logout = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showConfirmLogoutPrompt = BehaviorSubject.create();
    private final PublishSubject<Newsletter> showOptInPrompt = PublishSubject.create();
    private final PublishSubject<Void> updateSuccess = PublishSubject.create();
    private final BehaviorSubject<User> userOutput = BehaviorSubject.create();

    private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;
    public final Errors errors = this;

    @Override public void closeLogoutConfirmationClicked() {
      this.showConfirmLogoutPrompt.onNext(false);
    }
    @Override public void confirmLogoutClicked() {
      this.confirmLogoutClicked.onNext(null);
    }
    @Override public void contactEmailClicked() {
      this.contactEmailClicked.onNext(null);
    }
    @Override public void logoutClicked() {
      this.showConfirmLogoutPrompt.onNext(true);
    }
    @Override public void notifyMobileOfFollower(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFollower(b).build());
    }
    @Override public void notifyMobileOfFriendActivity(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFriendActivity(b).build());
    }
    @Override public void notifyMobileOfUpdates(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfUpdates(b).build());
    }
    @Override public void notifyOfFollower(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFollower(b).build());
    }
    @Override public void notifyOfFriendActivity(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFriendActivity(b).build());
    }
    @Override public void notifyOfUpdates(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfUpdates(b).build());
    }
    @Override public void sendGamesNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().gamesNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.GAMES));
    }
    @Override public void sendHappeningNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().happeningNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.HAPPENING));
    }
    @Override public void sendPromoNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().promoNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.PROMO));
    }
    @Override public void sendWeeklyNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().weeklyNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.WEEKLY));
    }

    @Override public @NonNull Observable<Void> logout() {
      return this.logout;
    }
    @Override public @NonNull Observable<Boolean> showConfirmLogoutPrompt() {
      return this.showConfirmLogoutPrompt;
    }
    @Override public @NonNull Observable<Newsletter> showOptInPrompt() {
      return this.showOptInPrompt;
    }
    @Override public @NonNull Observable<User> user() {
      return this.userOutput;
    }

    @Override public @NonNull Observable<String> unableToSavePreferenceError() {
      return this.unableToSavePreferenceError
        .takeUntil(this.updateSuccess)
        .map(__ -> null);
    }
  }
}
