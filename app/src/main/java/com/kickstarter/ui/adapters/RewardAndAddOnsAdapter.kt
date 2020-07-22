package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.*

class RewardAndAddOnsAdapter(private val viewListener: RewardAndAddOnsAdapter.ViewListener) : KSAdapter() {

    interface ViewListener:RewardViewHolder.Delegate, AddOnViewHolder.ViewListener

    init {
        insertSection(SECTION_REWARD_CARD, emptyList<Reward>())
        insertSection(SECTION_ADD_ONS_CARD, emptyList<Reward>())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()){
        SECTION_REWARD_CARD -> R.layout.item_reward
        SECTION_ADD_ONS_CARD -> R.layout.item_add_on
        else -> 0
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.item_reward -> RewardViewHolder(view, viewListener)
            R.layout.item_add_on -> AddOnViewHolder(view, viewListener)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateDataForAddOns(rewards: List<Pair<ProjectData, Reward>>) {
        setSection(SECTION_ADD_ONS_CARD, rewards)
        notifyDataSetChanged()
    }

    fun populateDataForRewad(reward: Pair<ProjectData, Reward>) {
        setSection(SECTION_REWARD_CARD, listOf(reward))
        notifyDataSetChanged()
    }

    companion object {
        private const val SECTION_REWARD_CARD = 0
        private const val SECTION_ADD_ONS_CARD = 1
    }
}
