package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ConfigFeatureFlagName.SEGMENT_ENABLED
import com.kickstarter.ui.adapters.FeatureFlagsAdapter
import com.kickstarter.ui.itemdecorations.TableItemDecoration
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import com.kickstarter.viewmodels.FeatureFlagsViewModel
import kotlinx.android.synthetic.internal.activity_feature_flags.*
import kotlinx.android.synthetic.internal.item_feature_flag_override.view.*
import javax.inject.Inject

@RequiresActivityViewModel(FeatureFlagsViewModel.ViewModel::class)
class FeatureFlagsActivity : BaseActivity<FeatureFlagsViewModel.ViewModel>(), FeatureFlagViewHolder.Delegate {

    @JvmField
    @Inject
    var featuresFlagPreference: StringPreferenceType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flags)

        (applicationContext as KSApplication).component().inject(this)

        val configFlagsAdapter = FeatureFlagsAdapter(this)
        config_flags.adapter = configFlagsAdapter
        config_flags.addItemDecoration(TableItemDecoration())

        val optimizelyFlagsAdapter = FeatureFlagsAdapter(this)
        optimizely_flags.adapter = optimizelyFlagsAdapter
        optimizely_flags.addItemDecoration(TableItemDecoration())

        this.viewModel.outputs.configFeatures()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { configFlagsAdapter.takeFlags(it) }

        this.viewModel.outputs.optimizelyFeatures()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { optimizelyFlagsAdapter.takeFlags(it) }
    }

    private fun displayPreference(@StringRes labelRes: Int, booleanPreferenceType: BooleanPreferenceType, overrideContainer: View) {
        overrideContainer.override_label.setText(labelRes)

        val switch = overrideContainer.override_switch
        switch.isChecked = booleanPreferenceType.get()

        overrideContainer.setOnClickListener {
            booleanPreferenceType.set(BooleanUtils.negate(booleanPreferenceType.get()))
            switch.isChecked = booleanPreferenceType.get()
        }
    }

    override fun featureOptionToggle(featureName: String, isEnabled: Boolean) {

        when (featureName) {
            SEGMENT_ENABLED.featureFlag -> {
                this.viewModel.inputs.updateSegmentFlag(isEnabled, featuresFlagPreference)
            }
        }
    }
}
