package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.FreezeLinearLayoutManager
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.ui.viewholders.RewardPledgeCardViewHolder
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pledge.*
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment : BaseFragment<PledgeFragmentViewModel.ViewModel>(), RewardCardAdapter.Delegate {

    private lateinit var adapter : RewardCardAdapter
    private val animDuration = 200L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_pledge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val viewTreeObserver = pledge_root.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        showPledgeSection()
                        pledge_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }

        this.adapter = RewardCardAdapter(this)
        cards_recycler.layoutManager = FreezeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        cards_recycler.adapter = adapter
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)))

        this.viewModel.outputs.estimatedDelivery()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe{ pledge_estimated_delivery.text = it }

        this.viewModel.outputs.pledgeAmount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe{ pledge_amount.text = it }

        this.viewModel.outputs.cards()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { this.adapter.takeCards(it) }

        this.viewModel.outputs.showThanksActivity()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { fragmentManager?.popBackStack() }

        this.viewModel.outputs.updateSelectedPosition()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.adapter.setSelectedPosition(it) }
    }

    override fun closePledgeButtonClicked(position: Int) {
        this.adapter.resetSelectedPosition(position)
        (cards_recycler.layoutManager as FreezeLinearLayoutManager).setFrozen(false)
    }

    override fun pledgeButtonClicked(viewHolder: RewardPledgeCardViewHolder) {
        this.viewModel.inputs.pledgeButtonClicked()
    }

    override fun selectCardButtonClicked(position: Int) {
        this.viewModel.inputs.selectCardButtonClicked(position)
        cards_recycler.scrollToPosition(position)
        (cards_recycler.layoutManager as FreezeLinearLayoutManager).setFrozen(true)
    }

    private fun showPledgeSection() {
        val initialMargin = resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)

        val reward = arguments?.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward?
        val location = arguments?.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation?
        if (location != null && reward != null) {
            resetPledgeViews(location, reward)
            revealPledgeSection(location, initialMargin)
        }
    }

    private fun resetPledgeViews(location: ScreenLocation, reward: Reward) {
        positionRewardSnapshot(location, reward)

        setDeliveryHeight(location)

        pledge_details.y = pledge_root.height.toFloat()
    }

    private fun setDeliveryHeight(location: ScreenLocation) {
        val miniRewardWidth = resources.getDimensionPixelSize(R.dimen.mini_reward_width)
        val scaleX = miniRewardWidth.toFloat() / location.width
        val miniRewardHeight = location.height * scaleX
        val deliveryParams = delivery.layoutParams as LinearLayout.LayoutParams
        deliveryParams.height = Math.max(miniRewardHeight.toInt(), delivery.height)
        delivery.layoutParams = deliveryParams
    }

    private fun positionRewardSnapshot(location: ScreenLocation, reward: Reward) {
        val rewardParams = reward_snapshot.layoutParams as FrameLayout.LayoutParams
        rewardParams.marginStart = location.x.toInt()
        rewardParams.topMargin = location.y.toInt()
        rewardParams.height = location.height
        rewardParams.width = location.width
        reward_snapshot.layoutParams = rewardParams
        reward_snapshot.pivotX = 0f
        reward_snapshot.pivotY = 0f

//        todo: blocked until rewards work is available
//        val rewardViewHolder = RewardsAdapter.RewardViewHolder(reward_to_copy)
//        rewardViewHolder.bind(reward)

        reward_to_copy.post {
            pledge_root.visibility = View.VISIBLE
//            reward_snapshot.setImageBitmap(ViewUtils.getBitmap(reward_to_copy, location.width, location.width))
            reward_to_copy.visibility = View.GONE
        }
    }

    private fun revealPledgeSection(location: ScreenLocation, initialMargin: Int) {
        val slideRewardLeft = getRewardMarginAnimator(location.x.toInt(), initialMargin)

        val miniRewardWidth = resources.getDimensionPixelSize(R.dimen.mini_reward_width)
        val shrinkReward = getRewardSizeAnimator(location.width, miniRewardWidth, location)

        val slideDetailsUp = getDetailsYAnimator(pledge_root.height.toFloat(), 0f)

        reward_snapshot.setOnClickListener { view ->
            if (!shrinkReward.isRunning) {
                view.setOnClickListener(null)
                hidePledgeSection(location, initialMargin)
            }
        }

        startPledgeAnimatorSet(shrinkReward, slideRewardLeft, slideDetailsUp)
    }

    private fun hidePledgeSection(location: ScreenLocation, initialMargin: Int) {
        val slideRewardRight = getRewardMarginAnimator(initialMargin, location.x.toInt())

        val expandReward = getRewardSizeAnimator(reward_snapshot.width, location.width, location).apply {
            addUpdateListener {
                if (it.animatedFraction == 1f) {
                    fragmentManager?.popBackStack()
                }
            }
        }

        val slideDetailsDown = getDetailsYAnimator(0f, pledge_root.height.toFloat())

        startPledgeAnimatorSet(expandReward, slideRewardRight, slideDetailsDown)
    }

    private fun getDetailsYAnimator(initialValue: Float, finalValue: Float): ObjectAnimator? {
        return ObjectAnimator.ofFloat(pledge_details, View.Y, initialValue, finalValue).apply {
            addUpdateListener {
                val animatedFraction = it.animatedFraction
                pledge_details.alpha = if (finalValue == 0f) animatedFraction else 1 - animatedFraction
            }
        }
    }

    private fun getRewardSizeAnimator(initialValue: Int, finalValue: Int, location: ScreenLocation): ValueAnimator {
        return ValueAnimator.ofInt(initialValue, finalValue).apply {
            addUpdateListener {
                val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                val newWidth = it.animatedValue as Int
                newParams?.width = newWidth
                val scaleX = newWidth / location.width.toFloat()

                newParams?.height = (scaleX * location.height).toInt()
                reward_snapshot?.layoutParams = newParams
            }
        }
    }

    private fun getRewardMarginAnimator(initialValue: Int, finalValue: Int): ValueAnimator {
        return ValueAnimator.ofInt(initialValue, finalValue).apply {
            addUpdateListener {
                val newParams = reward_snapshot?.layoutParams as FrameLayout.LayoutParams?
                val newMargin = it.animatedValue as Int
                newParams?.marginStart = newMargin
                reward_snapshot?.layoutParams = newParams
            }
        }
    }

    private fun startPledgeAnimatorSet(rewardSizeAnimator: ValueAnimator, rewardMarginAnimator: ValueAnimator, detailsYAnimator: ObjectAnimator?) {
        AnimatorSet().apply {
            interpolator = FastOutSlowInInterpolator()
            playTogether(rewardSizeAnimator, rewardMarginAnimator, detailsYAnimator)
            duration = animDuration
            start()
        }
    }

    companion object {

        fun newInstance(location: ScreenLocation, reward: Reward, project: Project): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_REWARD, reward)
            argument.putParcelable(ArgumentsKey.PLEDGE_PROJECT, project)
            argument.putSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION, location)
            fragment.arguments = argument
            return fragment
        }
    }

}
