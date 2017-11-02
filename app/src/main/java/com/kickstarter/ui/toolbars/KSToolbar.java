package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Environment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class KSToolbar extends Toolbar {
  protected @Nullable @Bind(R.id.title_text_view) TextView titleTextView;
  private final CompositeSubscription subscriptions = new CompositeSubscription();

  public KSToolbar(final @NonNull Context context) {
    super(context);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Nullable @OnClick(R.id.back_button)
  protected void backButtonClick() {
    ((BaseActivity) getContext()).back();
  }

  protected @NonNull Environment environment() {
    return ((KSApplication) getContext().getApplicationContext()).component().environment();
  }

  /**
   * If the toolbar has a textview with id title_text_view, set its title.
   */
  public void setTitle(final @NonNull String title) {
    if (this.titleTextView != null) {
      this.titleTextView.setText(title);
    }
  }

  @CallSuper
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @CallSuper
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @CallSuper
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    this.subscriptions.clear();
  }

  protected final void addSubscription(final @NonNull Subscription subscription) {
    this.subscriptions.add(subscription);
  }
}
