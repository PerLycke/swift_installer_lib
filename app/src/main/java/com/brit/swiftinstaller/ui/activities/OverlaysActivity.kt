package com.brit.swiftinstaller.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
import com.brit.swiftinstaller.R
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.ui.applist.AppItem
import com.brit.swiftinstaller.ui.applist.AppListFragment
import com.brit.swiftinstaller.ui.applist.AppsTabPagerAdapter
import com.brit.swiftinstaller.utils.*
import com.brit.swiftinstaller.utils.OverlayUtils.getAvailableOverlayVersions
import com.brit.swiftinstaller.utils.Utils.createImage
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_overlays.*
import kotlinx.android.synthetic.main.sheet_confirm_uninstall.view.*
import kotlinx.android.synthetic.main.tab_layout_overlay.*
import kotlinx.android.synthetic.main.tab_overlays_updates.*
import kotlinx.android.synthetic.main.toolbar_overlays.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class OverlaysActivity : ThemeActivity() {

    companion object {
        private const val INSTALL_TAB = 0
        private const val ACTIVE_TAB = 1
        const val UPDATE_TAB = 2
    }

    private var mPagerAdapter: AppsTabPagerAdapter? = null
    private lateinit var mViewPager: ViewPager
    private var overlaysList = ArrayList<AppItem>()
    private var hasUpdate = false
    private var checked = 0
    private var apps = 0
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlays)

        val bundle = intent.extras
        overlaysList = bundle?.getParcelableArrayList("overlays_list") ?: arrayListOf()

        mPagerAdapter = AppsTabPagerAdapter(supportFragmentManager,
                false, INSTALL_TAB, ACTIVE_TAB, UPDATE_TAB)
        mPagerAdapter!!.setAlertIconClickListener(object : AppListFragment.AlertIconClickListener {
            override fun onAlertIconClick(appItem: AppItem) {
                val packageInfo = packageManager.getPackageInfo(appItem.packageName, 0)
                val dialog = AlertDialog.Builder(this@OverlaysActivity, R.style.AppTheme_AlertDialog)
                        .setTitle(appItem.title)
                        .setIcon(appItem.icon)
                        .setMessage("Version support info:" +
                                "\nCurrent Version: ${packageInfo.versionName}" +
                                "\nAvailable Versions: ${getAvailableOverlayVersions(
                                        this@OverlaysActivity, appItem.packageName)}")
                        .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                themeDialog()
                dialog.show()
            }
        })
        mPagerAdapter!!.setAppCheckBoxClickListener(object : AppListFragment.AppCheckBoxClickListener {
            override fun onCheckBoxClick(appItem: AppItem) {
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
                mHandler.post {
                    checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
                }
            }
        })
        mPagerAdapter!!.setViewClickListener(object : AppListFragment.ViewClickListener {
            override fun onClick(appItem: AppItem) {
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
                mHandler.post {
                    checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
                }
            }
        })
        mPagerAdapter!!.setRequiredApps(INSTALL_TAB,
                RomInfo.getRomInfo(this).getRequiredApps())

        search_view.setOnSearchClickListener {
            toolbar_overlays_main_content.visibility = View.GONE
            select_all_btn.isClickable = false
            select_all_btn.isEnabled = false
        }
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mPagerAdapter!!.querySearch(container.currentItem, newText!!)
                return true
            }
        })
        search_view.setOnCloseListener {
            onClose()
            false
        }
        val textViewId = search_view.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        setCursorPointerColor(textViewId, getAccentColor(this))
        setCursorDrawableColor(textViewId, getAccentColor(this))

        mViewPager = container
        mViewPager.offscreenPageLimit = 2

        select_all_btn.setOnClickListener {
            if (checked == apps) {
                mPagerAdapter!!.selectAll(container.currentItem, false)
                if (select_all_btn.isChecked) {
                    select_all_btn.isChecked = false
                }
            } else {
                mPagerAdapter!!.selectAll(container.currentItem, true)
            }
            mPagerAdapter!!.notifyFragmentDataSetChanged(container.currentItem)
            mHandler.post {
                checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
            }
        }

        container.adapter = mPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs_overlays_root))
        tabs_overlays_root.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                doAsync {
                    apps = mPagerAdapter!!.getCheckableCount(this@OverlaysActivity, container.currentItem)
                }
                val checked = mPagerAdapter!!.getCheckedCount(position)
                select_all_btn.isChecked =
                        checked == mPagerAdapter!!.getAppsCount(position) && checked > 0
                setBackgroundImage()
            }

        })

        if (intent.hasExtra("tab")) {
            mViewPager.currentItem = intent.getIntExtra("tab", 0)
        }
    }

    private fun setBackgroundImage() {

        if (mPagerAdapter!!.getAppsCount(container.currentItem) == 0) {
            when (container.currentItem) {
                0 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_inactive))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_inactive)
                    empty_list_text.alpha = 0.2f
                }
                1 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_active))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_active)
                    empty_list_text.alpha = 0.2f
                }
                2 -> {
                    empty_list_image.setImageDrawable(getDrawable(R.drawable.ic_empty_updates))
                    empty_list_image.alpha = 0.2f
                    empty_list_text.text = getString(R.string.empty_list_updates)
                    empty_list_text.alpha = 0.2f
                }
            }
        } else {
            empty_list_image.setImageDrawable(null)
            empty_list_text.text = ""
        }
    }

    private fun onClose() {
        toolbar_overlays_main_content.visibility = View.VISIBLE
        select_all_btn.isClickable = true
        select_all_btn.isEnabled = true
    }

    override fun onBackPressed() {
        if (!search_view.isIconified) {
            search_view.onActionViewCollapsed()
            onClose()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        toolbar_subtitle_current_accent.setTextColor(getAccentColor(this))
        toolbar_subtitle_current_accent.text = getString(R.string.hex_string,
                String.format("%06x", getAccentColor(this)).substring(2))
        if (select_all_btn.isChecked) {
            select_all_btn.isChecked = false
        }
        updateAdapter()
    }

    fun updateAdapter() {
        select_all_btn.visibility = View.INVISIBLE
        select_all_btn.isClickable = false
        loading_progress.visibility = View.VISIBLE
        loading_progress.indeterminateDrawable.setColorFilter(getAccentColor(this), PorterDuff.Mode.SRC_ATOP)
        mPagerAdapter!!.clearApps()
        doAsync {
            if (overlaysList.size == 0) {
                overlaysList = Utils.sortedOverlaysList(this@OverlaysActivity)
            }
            val context = this@OverlaysActivity
            val updates = getAppsToUpdate(context)
            val pm = context.packageManager
            val installTabList = arrayListOf<AppItem>()
            val activeTabsList = arrayListOf<AppItem>()
            val updatesTabList = arrayListOf<AppItem>()
            for (item in overlaysList) {
                var status: Int?
                val pn = item.packageName
                item.icon = pm.getApplicationIcon(item.packageName)
                try {
                    status = pm.getApplicationEnabledSetting(pn)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
                if (RomInfo.getRomInfo(context).isOverlayInstalled(pn)) {
                    if (updates.contains(pn)
                            && status != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                        updatesTabList.add(item)
                        hasUpdate = true
                    } else {
                        activeTabsList.add(item)
                    }
                } else if (status != COMPONENT_ENABLED_STATE_DISABLED_USER) {
                    installTabList.add(item)
                }
            }

            for (i in installTabList) {
                mPagerAdapter!!.addApp(INSTALL_TAB, i)
            }
            for (i in activeTabsList) {
                mPagerAdapter!!.addApp(ACTIVE_TAB, i)
            }
            for (i in updatesTabList) {
                mPagerAdapter!!.addApp(UPDATE_TAB, i)
            }

            uiThread { _ ->
                doAsync {
                    checked = mPagerAdapter!!.getCheckedCount(container.currentItem)
                    apps = mPagerAdapter!!.getCheckableCount(this@OverlaysActivity, container.currentItem)
                    uiThread { _ ->
                        if (hasUpdate) {
                            update_tab_indicator.visibility = View.VISIBLE
                        }
                        setBackgroundImage()
                        loading_progress.visibility = View.INVISIBLE
                        select_all_btn.visibility = View.VISIBLE
                        select_all_btn.isClickable = true
                    }
                }
            }
        }
    }

    fun customizeBtnClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, CustomizeActivity::class.java)
        startActivity(intent)
    }

    interface Callback {
        fun updateApps(tab: Int, item: AppItem)
    }

    private fun getCheckedItems(index: Int): ArrayList<AppItem> {
        return mPagerAdapter!!.getCheckedItems(index)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun fabClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_overlays_fab, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(getBackgroundColor(this))
        bottomSheetDialog.show()

        val install = sheetView.findViewById<View>(R.id.install)
        val uninstall = sheetView.findViewById<View>(R.id.uninstall)
        val update = sheetView.findViewById<View>(R.id.update)

        val sheetListener = View.OnClickListener {
            when (it) {
                install -> {
                    val launch = getSharedPreferences("launched", Context.MODE_PRIVATE).getString("launched","first")
                    bottomSheetDialog.dismiss()

                    when (launch) {
                        "default" -> installAction()
                        "first" -> {
                            getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "second").apply()
                            installAction()
                        }
                        "second" -> {
                            val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                                    .setTitle(R.string.reboot_delay_title)
                                    .setMessage(R.string.reboot_delay_msg)
                                    .setPositiveButton(R.string.proceed) { dialogInterface, _ ->
                                        getSharedPreferences("launched", Context.MODE_PRIVATE).edit().putString("launched", "default").apply()
                                        dialogInterface.dismiss()
                                        installAction()
                                    }
                                    .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }

                            themeDialog()
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                }
                uninstall -> {
                    bottomSheetDialog.dismiss()
                    uninstallAction()
                }
                update -> {
                    bottomSheetDialog.dismiss()
                    updateAction()
                }
            }
        }

        install.setOnClickListener(sheetListener)
        uninstall.setOnClickListener(sheetListener)
        update.setOnClickListener(sheetListener)

        when {
            container.currentItem == INSTALL_TAB -> {
                uninstall.visibility = View.GONE
                update.visibility = View.GONE
            }
            container.currentItem == ACTIVE_TAB -> {
                install.visibility = View.GONE
                val checked = getCheckedItems(ACTIVE_TAB)
                val updates = getAppsToUpdate(this)
                var updatesAvailable = false
                checked.forEach {
                    if (!updatesAvailable)
                        updatesAvailable = updates.contains(it.packageName)
                }
                update.visibility = if (updatesAvailable) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            container.currentItem == UPDATE_TAB -> {
                install.visibility = View.GONE
            }
        }
    }

    private fun installAction() {
        val checked = getCheckedItems(mViewPager.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        val intent = Intent(this, InstallActivity::class.java)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        startActivity(intent)
    }

    private fun uninstallAction() {
        val checked = getCheckedItems(mViewPager.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = View.inflate(this, R.layout.sheet_confirm_uninstall, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.setBackgroundColor(getBackgroundColor(this))
        bottomSheetDialog.show()

        sheetView.confirm_layout.setOnClickListener {
            bottomSheetDialog.dismiss()
            uninstallProgressAction(checked)

            Toast.makeText(this, "This can take a lot of time, have patience!", Toast.LENGTH_LONG).show()
        }

        sheetView.cancel_layout.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun uninstallProgressAction(checked: ArrayList<AppItem>) {
        val intent = Intent(this, InstallActivity::class.java)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("uninstall", true)
        startActivity(intent)
    }

    private fun updateAction() {
        val checked = getCheckedItems(mViewPager.currentItem)
        if (checked.isEmpty()) {
            toast("No apps checked. Nothing to do")
            return
        }
        UpdateChecker(this, object : UpdateChecker.Callback() {
            override fun finished(installedCount: Int, updates: ArrayList<String>) {
                updateAdapter()
            }
        }).execute()
        val intent = Intent(this, InstallActivity::class.java)
        val apps = ArrayList<String>()
        checked.mapTo(apps) { it.packageName }
        intent.putStringArrayListExtra("apps", apps)
        intent.putExtra("update", true)
        startActivity(intent)
    }

    fun overlaysBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        onBackPressed()
    }

    @Suppress("UNUSED_PARAMETER")
    fun gboardInfo(view: View) {
        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(R.string.gboard_dialog_title)
                .setMessage(R.string.gboard_bg_info)
                .setPositiveButton(R.string.save) { dialogInterface, _ ->
                    val bitmap = createImage(512,512, getBackgroundColor(this))
                    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val image = File(downloads, "swift_bg.png")
                    var success = false
                    val outStream: FileOutputStream
                    try {
                        outStream = FileOutputStream(image)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                        outStream.flush()
                        outStream.close()
                        success = true
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (success) {
                        Toast.makeText(applicationContext, R.string.saved,
                                Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext,
                                R.string.save_error, Toast.LENGTH_LONG).show()
                    }

                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
        themeDialog()
        val dialog = builder.create()
        dialog.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun blockedPackagesInfo(view: View) {
        val builder = AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(R.string.blocked_packages_title)
                .setMessage(R.string.blocked_packages_message)
                .setNegativeButton(R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
        themeDialog()
        val dialog = builder.create()
        dialog.show()
    }
}
