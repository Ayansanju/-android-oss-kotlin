package com.kickstarter.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.models.User
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.viewmodels.EmailVerificationInterstitialFragmentViewModel
import kotlinx.android.synthetic.main.fragment_email_verification_interstitial.*

@RequiresFragmentViewModel(EmailVerificationInterstitialFragmentViewModel.ViewModel::class)
class EmailVerificationInterstitialFragment : BaseFragment<EmailVerificationInterstitialFragmentViewModel.ViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_email_verification_interstitial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.outputs.startEmailActivity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    startActivity(Intent.createChooser(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL), "Choose Email"))
                }

        email_verification_interstitial_cta_button.setOnClickListener {
            this.viewModel.inputs.openInboxButtonPressed()
        }
    }

    companion object {
        fun newInstance(user: User): EmailVerificationInterstitialFragment {
            val fragment = EmailVerificationInterstitialFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.USER, user)
            fragment.arguments = argument
            return fragment
        }
    }
}