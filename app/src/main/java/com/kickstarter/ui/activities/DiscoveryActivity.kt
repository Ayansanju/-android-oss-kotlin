package com.kickstarter.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryLayoutBinding
import com.kickstarter.features.pledgedprojectsoverview.ui.PledgedProjectsOverviewActivity
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Environment
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.checkPermissions
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showSuccessSnackBar
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.ui.fragments.DiscoveryFragment.Companion.newInstance
import com.kickstarter.viewmodels.DiscoveryViewModel
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@RequiresActivityViewModel(DiscoveryViewModel.ViewModel::class)
class DiscoveryActivity : BaseActivity<DiscoveryViewModel.ViewModel>() {
    private lateinit var drawerAdapter: DiscoveryDrawerAdapter
    private lateinit var drawerLayoutManager: LinearLayoutManager
    private lateinit var pagerAdapter: DiscoveryPagerAdapter
    private lateinit var consentManagementDialogFragment: ConsentManagementDialogFragment
    private var internalTools: InternalToolsType? = null
    private lateinit var binding: DiscoveryLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = DiscoveryLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        environment()

        // TODO: Replace with compose implementation
        val nightModeFlags = this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        viewModel.setDarkTheme(
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> { true }
                else -> false
            }
        )

        if (savedInstanceState == null) {
            activateFeatureFlags(environment())
        }

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

        internalTools = environment().internalTools()

        drawerLayoutManager = LinearLayoutManager(this)

        binding.discoveryDrawerRecyclerView.layoutManager = drawerLayoutManager
        drawerAdapter = DiscoveryDrawerAdapter(
            viewModel.inputs,
        )
        binding.discoveryDrawerRecyclerView.adapter = drawerAdapter

        val viewPagerTitles = listOf(
            getString(R.string.discovery_sort_types_magic),
            getString(R.string.Popular),
            getString(R.string.discovery_sort_types_newest),
            getString(R.string.Ending_soon)
        )

        pagerAdapter = DiscoveryPagerAdapter(
            supportFragmentManager,
            createFragments(viewPagerTitles.size).toMutableList(),
            viewPagerTitles,
            viewModel.inputs
        )

        binding.discoveryViewPager.adapter = pagerAdapter
        binding.discoveryTabLayout.setupWithViewPager(binding.discoveryViewPager)

        addTabSelectedListenerToTabLayout()

        viewModel.outputs.expandSortTabLayout()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.discoverySortAppBarLayout.setExpanded(it) }

        viewModel.outputs.updateToolbarWithParams()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.discoveryToolbar.discoveryToolbar.loadParams(it) }

        viewModel.outputs.updateParamsForPage()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.discoveryViewPager.currentItem = it.sort().positionFromSort()
                pagerAdapter.takeParams(it)
            }

        viewModel.outputs.showNotifPermissionsRequest()
            .distinctUntilChanged()
            .filter {
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                    this.checkPermissions(Manifest.permission.POST_NOTIFICATIONS)
            }
            .delay(2000, TimeUnit.MILLISECONDS)
            .compose(bindToLifecycle())
            .subscribe {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                viewModel.inputs.hasSeenNotificationsPermission(true)
            }

        viewModel.outputs.showConsentManagementDialog()
            .distinctUntilChanged()
            .subscribe {
                consentManagementDialogFragment = ConsentManagementDialogFragment()
                consentManagementDialogFragment.isCancelable = false
                consentManagementDialogFragment.show(supportFragmentManager, "consentManagementDialogFragment")
            }

        viewModel.outputs.clearPages()
            .compose(bindToLifecycle())
            .compose<List<Int?>>(Transformers.observeForUI())
            .subscribe { pagerAdapter.clearPages(it) }

        viewModel.outputs.rootCategoriesAndPosition()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pagerAdapter.takeCategoriesForPosition(it.first, it.second) }

        viewModel.outputs.showActivityFeed()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startActivityFeedActivity() }

        viewModel.outputs.showHelp()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startHelpSettingsActivity() }

        viewModel.outputs.showInternalTools()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { internalTools?.maybeStartInternalToolsActivity(this) }

        viewModel.outputs.showLoginTout()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startLoginToutActivity() }

        viewModel.outputs.showMessages()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startMessageThreadsActivity() }

        viewModel.outputs.showProfile()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProfileActivity() }

        viewModel.outputs.showPledgedProjects()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startPledgedProjectsOverview() }

        viewModel.outputs.showSettings()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startSettingsActivity() }

        viewModel.outputs.navigationDrawerData()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drawerAdapter.takeData(it) }

        viewModel.outputs.drawerIsOpen()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(RxDrawerLayout.open(binding.discoveryDrawerLayout, GravityCompat.START))

        viewModel.outputs.drawerMenuIcon()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { drawerMenuIcon: Int -> updateDrawerMenuIcon(drawerMenuIcon) }

        //endregion
        RxDrawerLayout.drawerOpen(binding.discoveryDrawerLayout, GravityCompat.START)
            .skip(1)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.openDrawer(it) }

        viewModel.outputs.showSuccessMessage()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this@DiscoveryActivity.showSuccessSnackBar(binding.discoveryAnchorView, it) }

        viewModel.outputs.showErrorMessage()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this@DiscoveryActivity.showErrorSnackBar(binding.discoveryAnchorView, it ?: "") }
    }

    private fun activateFeatureFlags(environment: Environment) {
        environment.featureFlagClient()?.activate(this)
    }

    fun discoveryLayout(): DrawerLayout {
        return binding.discoveryDrawerLayout
    }

    private fun addTabSelectedListenerToTabLayout() {
        binding.discoveryTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.sortClicked(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                pagerAdapter.scrollToTop(tab.position)
                viewModel.sortClicked(tab.position)
            }
        })
    }

    private fun updateDrawerMenuIcon(drawerMenuIcon: Int) {
        binding.discoveryToolbar.menuButton.setImageResource(drawerMenuIcon)
        if (binding.discoveryToolbar.menuButton.drawable is Animatable) {
            val menuDrawable = binding.discoveryToolbar.menuButton.drawable as Animatable
            menuDrawable.start()
        }
    }

    protected fun startActivityFeedActivity() {
        startActivity(Intent(this, ActivityFeedActivity::class.java))
    }

    protected fun startHelpSettingsActivity() {
        startActivity(Intent(this, HelpSettingsActivity::class.java))
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    private fun startMessageThreadsActivity() {
        val intent = Intent(this, MessageThreadsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startPledgedProjectsOverview() {
        val intent = Intent(this, PledgedProjectsOverviewActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        viewModel = null
        super.onDestroy()
    }

    companion object {
        private fun createFragments(pages: Int): List<DiscoveryFragment> {
            val fragments: MutableList<DiscoveryFragment> = ArrayList(pages)
            for (position in 0..pages) {
                fragments.add(newInstance(position))
            }
            return fragments
        }
    }
}
