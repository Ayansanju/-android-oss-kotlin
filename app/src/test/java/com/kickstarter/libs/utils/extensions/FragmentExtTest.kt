package com.kickstarter.libs.utils.extensions

import androidx.fragment.app.Fragment
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.CrowdfundCheckoutFragment
import com.kickstarter.ui.fragments.PledgeFragment
import org.junit.Test

class FragmentExtTest : KSRobolectricTestCase() {

    @Test
    fun testFragment_whenData_Null() {
        val fragment = Fragment().withData(null, null)
        assertNull(fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA))
        assertNull(fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON))
    }

    @Test
    fun testFragment_whenData_HaveData() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val reward = Reward.builder().build()
        val addOns = listOf(reward)

        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.MANAGE_REWARD)
            .projectData(projectData)
            .reward(reward)
            .addOns(addOns)
            .build()

        val fragment = Fragment().withData(pledgeData, PledgeReason.PLEDGE)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.PLEDGE)
    }

    @Test
    fun testPledgeFragmentInstance_ForNewPledge() {
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val reward = Reward.builder().build()
        val addOns = listOf(reward)

        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.NEW_PLEDGE)
            .projectData(projectData)
            .reward(reward)
            .addOns(addOns)
            .build()

        val fragment = Fragment().selectPledgeFragment(pledgeData, PledgeReason.PLEDGE)

        assertTrue(fragment is CrowdfundCheckoutFragment)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.PLEDGE)
    }

    @Test
    fun testPledgeFragmentInstance_ForFixPledge() {
        val project = ProjectFactory.project()
        val backing = BackingFactory.backing(project)
        val updatedProj = project.toBuilder().backing(backing).isBacking(true).build()
        val projectData = ProjectDataFactory.project(updatedProj)
        val reward = Reward.builder().build()
        val addOns = listOf(reward)

        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.FIX_ERRORED_PLEDGE)
            .projectData(projectData)
            .reward(reward)
            .addOns(addOns)
            .build()

        val fragment = Fragment().selectPledgeFragment(pledgeData, PledgeReason.FIX_PLEDGE)

        assertTrue(fragment is PledgeFragment)

        val arg1 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val arg2 = fragment.arguments?.get(ArgumentsKey.PLEDGE_PLEDGE_REASON)

        assertEquals(arg1, pledgeData)
        assertEquals(arg2, PledgeReason.FIX_PLEDGE)
    }
}
