package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentRewardsBinding
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.RewardDecoration
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.reduce
import com.kickstarter.libs.utils.extensions.selectPledgeFragment
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardsAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardsFragmentViewModel.Factory
import com.kickstarter.viewmodels.RewardsFragmentViewModel.RewardsFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class RewardsFragment : Fragment(), RewardsAdapter.Delegate {

    private var rewardsAdapter = RewardsAdapter(this)
    private lateinit var dialog: AlertDialog
    private var binding: FragmentRewardsBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: RewardsFragmentViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)
        }

        setupRecyclerView()
        createDialog()

        this.viewModel.outputs.projectData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { rewardsAdapter.populateRewards(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backedRewardPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { scrollToReward(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showPledgeFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                showPledgeFragment(it.first, it.second)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showAddOnsFragment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                showAddonsFragment(it)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.rewardsCount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setRewardsCount(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showAlert()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showAlert()
            }
            .addToDisposable(disposables)

        context?.apply {
            binding?.rewardsCount?.isGone = ViewUtils.isLandscape(this)
        }
    }
    fun setState(state: Boolean?) {
        state?.let {
            viewModel.isExpanded(state)
        }
    }

    private fun createDialog() {
        context?.let { context ->
            dialog = AlertDialog.Builder(context, R.style.AlertDialog)
                .setCancelable(false)
                .setTitle(getString(R.string.Continue_with_this_reward))
                .setMessage(getString(R.string.It_may_not_offer_some_or_all_of_your_add_ons))
                .setNegativeButton(getString(R.string.No_go_back)) { _, _ -> {} }
                .setPositiveButton(getString(R.string.Yes_continue)) { _, _ ->
                    this.viewModel.inputs.alertButtonPressed()
                }.create()
        }
    }

    private fun showAlert() {
        if (this.isVisible)
            dialog.show()
    }

    private fun scrollToReward(position: Int) {
        if (position != 0) {
            val recyclerWidth = (binding?.rewardsRecycler?.width ?: 0)
            val linearLayoutManager = binding?.rewardsRecycler?.layoutManager as LinearLayoutManager
            val rewardWidth = resources.getDimensionPixelSize(R.dimen.item_reward_width)
            val rewardMargin = resources.getDimensionPixelSize(R.dimen.reward_margin)
            val center = (recyclerWidth - rewardWidth - rewardMargin) / 2
            linearLayoutManager.scrollToPositionWithOffset(position, center)
        }
    }

    private fun setRewardsCount(count: Int) {
        val rewardsCountString = requireNotNull(this.viewModel.environment.ksString()).format(
            "Rewards_count_rewards", count,
            "rewards_count", NumberUtils.format(count)
        )
        binding?.rewardsCount?.text = rewardsCountString
    }

    override fun onDetach() {
        disposables.clear()
        super.onDetach()
        binding?.rewardsRecycler?.adapter = null
    }

    override fun rewardClicked(reward: Reward) {
        this.viewModel.inputs.rewardClicked(reward)
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    private fun addItemDecorator() {
        val margin = resources.getDimension(R.dimen.reward_margin).toInt()
        binding?.rewardsRecycler?.addItemDecoration(RewardDecoration(margin))
    }

    private fun setupRecyclerView() {
        binding?.rewardsRecycler?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding?.rewardsRecycler?.adapter = rewardsAdapter
        addItemDecorator()
    }

    private fun showPledgeFragment(
        pledgeData: PledgeData,
        pledgeReason: PledgeReason
    ) {
        val fragment = this.selectPledgeFragment(pledgeData, pledgeReason)

        if (this.isVisible && this.parentFragmentManager.findFragmentByTag(fragment::class.java.simpleName) == null) {
            this.parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(
                    R.id.fragment_container,
                    fragment,
                    fragment::class.java.simpleName
                )
                .addToBackStack(fragment::class.java.simpleName)
                .commit()
        }
    }

    private fun showAddonsFragment(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) {
        if (this.isVisible && this.parentFragmentManager.findFragmentByTag(BackingAddOnsFragment::class.java.simpleName) == null) {

            val reducedProject = pledgeDataAndReason.first.projectData().project().reduce()

            val reducedProjectData = pledgeDataAndReason.first.projectData().toBuilder().project(reducedProject).build()
            val reducedPledgeData = pledgeDataAndReason.first.toBuilder().projectData(reducedProjectData).build()

            val addOnsFragment = BackingAddOnsFragment.newInstance(Pair(reducedPledgeData, pledgeDataAndReason.second))

            this.parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(
                    R.id.fragment_container,
                    addOnsFragment,
                    BackingAddOnsFragment::class.java.simpleName
                )
                .addToBackStack(BackingAddOnsFragment::class.java.simpleName)
                .commit()
        }
    }
}
