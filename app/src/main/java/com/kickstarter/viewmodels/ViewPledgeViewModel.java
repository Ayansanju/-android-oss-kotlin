package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FeatureKey;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BackingUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.RewardUtils;
import com.kickstarter.models.Avatar;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ViewPledgeActivity;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.zipPair;

public interface ViewPledgeViewModel {

  interface Inputs {
    /** Call when the project context section is clicked. */
    void projectClicked();

    /** Call when the view messages button is clicked. */
    void viewMessagesButtonClicked();
  }

  interface Outputs {
    /** Set the backer name TextView's text. */
    Observable<String> backerNameTextViewText();

    /** Set the backer number TextView's text. */
    Observable<String> backerNumberTextViewText();

    /** Set the backing status TextView's text. */
    Observable<String> backingStatusTextViewText();

    /** Set the backing amount and date TextView's text. */
    Observable<Pair<String, String>> backingAmountAndDateTextViewText();

    /** Set the creator name TextView's text. */
    Observable<String> creatorNameTextViewText();

    /** Whether to hide the estimated delivery date section. */
    Observable<Boolean> estimatedDeliverySectionIsGone();

    /** text date for the estimated delivery section. */
    Observable<String> estimatedDeliverySectionTextViewText();

    /** Navigate back. */
    Observable<Void> goBack();

    /** Load the backer avatar given the URL. */
    Observable<String> loadBackerAvatar();

    /** Load the project photo given the URL. */
    Observable<String> loadProjectPhoto();

    /** Set the project name TextView's text. */
    Observable<String> projectNameTextViewText();

    /** Set the reward minimum and description TextView's text. */
    Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText();

    /** Show the rewards items. */
    Observable<List<RewardsItem>> rewardsItems();

    /** Returns `true` if the items section should be gone, `false` otherwise. */
    Observable<Boolean> rewardsItemsAreGone();

    /** Set the shipping amount TextView's text. */
    Observable<String> shippingAmountTextViewText();

    /** Set the shipping location TextView's text. */
    Observable<String> shippingLocationTextViewText();

    /** Set the visibility of the shipping section.*/
    Observable<Boolean> shippingSectionIsGone();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.MessagesActivity}. */
    Observable<Pair<Project, Backing>> startMessagesActivity();

    /** Emits a boolean to determine when the View Messages button should be gone. */
    Observable<Boolean> viewMessagesButtonIsGone();
  }

  final class ViewModel extends ActivityViewModel<ViewPledgeActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentConfigType currentConfig;
    private final CurrentUserType currentUser;
    private final KSCurrency ksCurrency;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();
      this.ksCurrency = environment.ksCurrency();

      final Observable<Project> project = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class);

      final Observable<Boolean> isFromMessagesActivity = intent()
        .map(i -> i.getBooleanExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, false))
        .ofType(Boolean.class);

      final Observable<Boolean> featureFlagIsEnabled = this.currentConfig.observable()
        .map(config -> ObjectUtils.coalesce(config.features().get(FeatureKey.ANDROID_MESSAGES), false));

      final Observable<Backing> backing = project
        .compose(combineLatestPair(this.currentUser.observable()))
        .filter(pu -> pu.second != null)
        .switchMap(pu -> this.client.fetchProjectBacking(pu.first, pu.second)
          .retry(3)
          .compose(neverError())
        )
        .share();

      final Observable<User> backer = backing
        .map(Backing::backer);

      final Observable<Backing> shippableBacking = backing
        .filter(BackingUtils::isShippable);

      final Observable<Reward> reward = backing
        .map(Backing::reward)
        .filter(ObjectUtils::isNotNull);

      Observable.zip(project, backing, Pair::create)
        .compose(takeWhen(this.viewMessagesButtonClicked))
        .compose(bindToLifecycle())
        .subscribe(this.startMessagesActivity);

      backing
        .map(Backing::sequence)
        .map(NumberUtils::format)
        .compose(bindToLifecycle())
        .subscribe(this.backerNumberTextViewText);

      backer
        .map(User::name)
        .compose(bindToLifecycle())
        .subscribe(this.backerNameTextViewText);

      project
        .compose(zipPair(backing))
        .map(pb -> backingAmountAndDate(this.ksCurrency, pb.first, pb.second))
        .compose(bindToLifecycle())
        .subscribe(this.backingAmountAndDateTextViewText);

      backing
        .map(Backing::status)
        .compose(bindToLifecycle())
        .subscribe(this.backingStatusTextViewText);

      project
        .map(p -> p.creator().name())
        .compose(bindToLifecycle())
        .subscribe(this.creatorNameTextViewText);

      this.goBack = this.projectClicked;

      backer
        .map(User::avatar)
        .map(Avatar::medium)
        .compose(bindToLifecycle())
        .subscribe(this.loadBackerAvatar);

      project
        .map(Project::photo)
        .filter(ObjectUtils::isNotNull)
        .map(Photo::full)
        .compose(bindToLifecycle())
        .subscribe(this.loadProjectPhoto);

      project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText);

      project
        .compose(zipPair(backing.map(Backing::reward)))
        .map(pr -> rewardMinimumAndDescription(this.ksCurrency, pr.first, pr.second))
        .compose(bindToLifecycle())
        .subscribe(this.rewardMinimumAndDescriptionTextViewText);

      reward
        .map(Reward::rewardsItems)
        .compose(Transformers.coalesce(new ArrayList<RewardsItem>()))
        .compose(bindToLifecycle())
        .subscribe(this.rewardsItems);

      reward
        .map(RewardUtils::isItemized)
        .map(BooleanUtils::negate)
        .compose(bindToLifecycle())
        .subscribe(this.rewardsItemsAreGone);

      reward
        .map(Reward::estimatedDeliveryOn)
        .map(ObjectUtils::isNull)
        .compose(bindToLifecycle())
        .subscribe(this.estimatedDeliverySectionIsGone);

      reward
        .map(Reward::estimatedDeliveryOn)
        .filter(ObjectUtils::isNotNull)
        .map(DateTimeUtils::estimatedDeliveryOn)
        .compose(bindToLifecycle())
        .subscribe(this.estimatedDeliverySectionTextViewText);

      project
        .compose(zipPair(shippableBacking))
        .map(pb -> this.ksCurrency.format(pb.second.shippingAmount(), pb.first))
        .compose(bindToLifecycle())
        .subscribe(this.shippingAmountTextViewText);

      backing
        .map(Backing::location)
        .filter(ObjectUtils::isNotNull)
        .map(Location::displayableName)
        .compose(bindToLifecycle())
        .subscribe(this.shippingLocationTextViewText);

      backing
        .map(BackingUtils::isShippable)
        .map(BooleanUtils::negate)
        .compose(bindToLifecycle())
        .subscribe(this.shippingSectionIsGone);

      Observable.zip(
        isFromMessagesActivity,
        featureFlagIsEnabled,
        Pair::create
      )
        .map(fromMessagesAndFlagEnabled -> fromMessagesAndFlagEnabled.first || !fromMessagesAndFlagEnabled.second)
        .compose(bindToLifecycle())
        .subscribe(this.viewMessagesButtonIsGone::onNext);

      project
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedPledgeInfo);
    }

    private static @NonNull Pair<String, String> backingAmountAndDate(final @NonNull KSCurrency ksCurrency,
      final @NonNull Project project, final @NonNull Backing backing) {

      final String amount = ksCurrency.format(backing.amount(), project);
      final String date = DateTimeUtils.fullDate(backing.pledgedAt());

      return Pair.create(amount, date);
    }

    private static @NonNull Pair<String, String> rewardMinimumAndDescription(final @NonNull KSCurrency ksCurrency,
      final @NonNull Project project, final @NonNull Reward reward) {

      final String minimum = ksCurrency.format(reward.minimum(), project);
      return Pair.create(minimum, reward.description());
    }

    private final PublishSubject<Void> projectClicked = PublishSubject.create();
    private final PublishSubject<Void> viewMessagesButtonClicked = PublishSubject.create();

    private final BehaviorSubject<String> backerNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> backerNumberTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> backingStatusTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Pair<String, String>> backingAmountAndDateTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> creatorNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> estimatedDeliverySectionIsGone = BehaviorSubject.create();
    private final BehaviorSubject<String> estimatedDeliverySectionTextViewText = BehaviorSubject.create();
    private final Observable<Void> goBack;
    private final BehaviorSubject<String> loadBackerAvatar = BehaviorSubject.create();
    private final BehaviorSubject<String> loadProjectPhoto = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Pair<String, String>> rewardMinimumAndDescriptionTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<List<RewardsItem>> rewardsItems = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> rewardsItemsAreGone = BehaviorSubject.create();
    private final BehaviorSubject<String> shippingAmountTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> shippingLocationTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> shippingSectionIsGone = BehaviorSubject.create();
    private final PublishSubject<Pair<Project, Backing>> startMessagesActivity = PublishSubject.create();
    private final BehaviorSubject<Boolean> viewMessagesButtonIsGone = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void projectClicked() {
      this.projectClicked.onNext(null);
    }
    @Override public void viewMessagesButtonClicked() {
      this.viewMessagesButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> backerNameTextViewText() {
      return this.backerNameTextViewText;
    }
    @Override public @NonNull Observable<String> backerNumberTextViewText() {
      return this.backerNumberTextViewText;
    }
    @Override public @NonNull Observable<Pair<String, String>> backingAmountAndDateTextViewText() {
      return this.backingAmountAndDateTextViewText;
    }
    @Override public @NonNull Observable<String> backingStatusTextViewText() {
      return this.backingStatusTextViewText;
    }
    @Override public @NonNull Observable<String> creatorNameTextViewText() {
      return this.creatorNameTextViewText;
    }
    @Override public @NonNull Observable<Boolean> estimatedDeliverySectionIsGone() {
      return this.estimatedDeliverySectionIsGone;
    }
    @Override public @NonNull Observable<String> estimatedDeliverySectionTextViewText() {
      return this.estimatedDeliverySectionTextViewText;
    }
    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<String> loadBackerAvatar() {
      return this.loadBackerAvatar;
    }
    @Override public @NonNull Observable<String> loadProjectPhoto() {
      return this.loadProjectPhoto;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText() {
      return this.rewardMinimumAndDescriptionTextViewText;
    }
    @Override public @NonNull Observable<List<RewardsItem>> rewardsItems() {
      return this.rewardsItems;
    }
    @Override public @NonNull Observable<Boolean> rewardsItemsAreGone() {
      return this.rewardsItemsAreGone;
    }
    @Override public @NonNull Observable<String> shippingAmountTextViewText() {
      return this.shippingAmountTextViewText;
    }
    @Override public @NonNull Observable<String> shippingLocationTextViewText() {
      return this.shippingLocationTextViewText;
    }
    @Override public @NonNull Observable<Boolean> shippingSectionIsGone() {
      return this.shippingSectionIsGone;
    }
    @Override public @NonNull Observable<Pair<Project, Backing>> startMessagesActivity() {
      return this.startMessagesActivity;
    }
    @Override public @NonNull Observable<Boolean> viewMessagesButtonIsGone() {
      return this.viewMessagesButtonIsGone;
    }
  }
}
