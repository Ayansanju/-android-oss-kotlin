package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.ExpandableHeaderViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class ExpandableHeaderAdapter: KSAdapter() {
    init {
        insertSection(SECTION_REWARD_SUMMARY, emptyList<Pair<Project, Reward>>())
    }

    override fun layout(sectionRow: SectionRow):Int {
        return when (sectionRow.section()) {
            SECTION_REWARD_SUMMARY -> R.layout.expandable_header_item
            else -> 0
        }
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when(layout) {
            R.layout.expandable_header_item -> ExpandableHeaderViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun populateData(rewards: List<Pair<Project, Reward>>) {
        if (rewards != null) {
            setSection(SECTION_REWARD_SUMMARY, rewards)
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val SECTION_REWARD_SUMMARY = 0
    }
}