package com.kickstarter.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showSnackbar
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.viewmodels.NewCardFragmentViewModel
import com.stripe.android.view.CardInputListener
import kotlinx.android.synthetic.main.form_new_card.*
import kotlinx.android.synthetic.main.fragment_new_card.*
import kotlinx.android.synthetic.main.modal_fragment_new_card.*
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(NewCardFragmentViewModel.ViewModel::class)
class NewCardFragment : BaseFragment<NewCardFragmentViewModel.ViewModel>() {
    interface OnCardSavedListener {
        fun cardSaved(storedCard: StoredCard)
    }

    private var saveEnabled = false
    private var onCardSavedListener: OnCardSavedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = if (modal()) R.layout.modal_fragment_new_card else R.layout.fragment_new_card
        return inflater.inflate(layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(new_card_toolbar)
        setHasOptionsMenu(true)

        when {
            modal() -> new_card_app_bar_layout.stateListAnimator = null
        }

        this.viewModel.outputs.allowedCardWarningIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setInvisible(allowed_card_warning, !it) }

        this.viewModel.outputs.cardWidgetFocusDrawable()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { card_focus.setImageResource(it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { this.activity != null && isVisible }
                .subscribe {
                    ViewUtils.setGone(progress_bar, !it)
                    updateMenu(!it)
                }

        this.viewModel.outputs.saveButtonIsEnabled()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.onCardSavedListener?.cardSaved(it) }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val snackbarAnchor = if (modal()) modal_new_card_snackbar_anchor else new_card_root
                    showSnackbar(snackbarAnchor, it)
                }

        cardholder_name.onChange { this.viewModel.inputs.name(it) }
        postal_code.onChange { this.viewModel.inputs.postalCode(it) }
        addListeners()
        cardholder_name.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.saveCardClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCardSavedListener = context as? OnCardSavedListener
        if (onCardSavedListener == null) {
            throw ClassCastException("$context must implement OnCardSavedListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val save = menu.findItem(R.id.save)
        save.isEnabled = saveEnabled
    }

    private fun addListeners() {
        card_input_widget.clearFocus()
        cardholder_name.onFocusChangeListener = cardFocusChangeListener
        postal_code.onFocusChangeListener = cardFocusChangeListener
        card_input_widget.setCardNumberTextWatcher(cardNumberWatcher)
        card_input_widget.setCvcNumberTextWatcher(cardValidityWatcher)
        card_input_widget.setExpiryDateTextWatcher(cardValidityWatcher)
        card_input_widget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: String?) {
                this@NewCardFragment.viewModel.inputs.cardFocus(true)
            }

            override fun onPostalCodeComplete() {
            }

            override fun onCardComplete() {
                cardChanged()
            }

            override fun onExpirationComplete() {
                cardChanged()
            }

            override fun onCvcComplete() {
                cardChanged()
            }
        })
    }

    private fun cardChanged() {
        this.viewModel.inputs.card(card_input_widget.card)
    }

    private fun modal(): Boolean {
        return arguments?.getBoolean(ArgumentsKey.NEW_CARD_MODAL) ?: false
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        activity?.invalidateOptionsMenu()
    }

    private val cardValidityWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cardChanged()
        }
    }

    private val cardNumberWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            this@NewCardFragment.viewModel.inputs.cardNumber(s?.toString() ?: "")
        }
    }

    private val cardFocusChangeListener = View.OnFocusChangeListener { _, _ -> this@NewCardFragment.viewModel.inputs.cardFocus(false) }

    companion object {

        fun newInstance(modal: Boolean = false): NewCardFragment {
            val fragment = NewCardFragment()
            val argument = Bundle()
            argument.putBoolean(ArgumentsKey.NEW_CARD_MODAL, modal)
            fragment.arguments = argument
            return fragment
        }
    }
}
