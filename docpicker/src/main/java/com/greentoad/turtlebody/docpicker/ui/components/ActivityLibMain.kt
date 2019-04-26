package com.greentoad.turtlebody.docpicker.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.greentoad.turtlebody.docpicker.R
import com.greentoad.turtlebody.docpicker.core.Constants
import com.greentoad.turtlebody.docpicker.core.DocPickerConfig
import com.greentoad.turtlebody.docpicker.ui.base.ActivityBase
import com.greentoad.turtlebody.docpicker.ui.common.bottom_sheet_filter.DocFilterFragment
import com.greentoad.turtlebody.docpicker.ui.components.file.DocFragment
import com.greentoad.turtlebody.docpicker.ui.components.folder.DocFolderFragment
import kotlinx.android.synthetic.main.tb_doc_picker_activity_lib_main.*
import org.jetbrains.anko.info
import java.io.Serializable


class ActivityLibMain : ActivityBase(){

    companion object{
        const val B_ARG_FILE_MISSING = "activity.lib.main.file.missing"
        const val B_ARG_URI_LIST = "activity.lib.main.uri.list"
    }


    private lateinit var mMenuItem: MenuItem
    private lateinit var mPickerConfig: DocPickerConfig

    private var mDocFilterFragment: BottomSheetDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tb_doc_picker_activity_lib_main)

        initToolbar(R.drawable.ic_arrow_back_black_24dp,tb_doc_picker_activity_toolbar)
        toolbarTitle = "Select Folder"

        if (intent.extras != null) {
            mPickerConfig = intent.getSerializableExtra(DocPickerConfig.ARG_BUNDLE) as DocPickerConfig
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tb_doc_picker_activity_main, menu)
        mMenuItem = menu.getItem(0)
        startDocFolderFragment()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_open -> {
                //createPickFromDocumentsIntent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
        when (fragment) {
            is DocFolderFragment -> finish()
            is DocFragment -> {
                super.onBackPressed()
                toolbarTitle = "Select Folder"
                tb_doc_picker_toolbar_txt_count.visibility = View.GONE
                mMenuItem.isVisible = true
                updateCounter(0)

                val fragment2 = supportFragmentManager.findFragmentById(R.id.frame_content)
                if(fragment2 is DocFolderFragment){
                    fragment2.onRefresh()
                }

            }
            else -> super.onBackPressed()
        }
    }


    /**
     * @param counter counter for selecting multiple media files
     */
    fun updateCounter(counter: Int) {
        tb_doc_picker_toolbar_txt_count.text = "$counter"
    }

    fun sendBackData(list: ArrayList<Uri>, isFileMissing: Boolean=false) {
        if (list.isNotEmpty()) {
            val intent = Intent()
            intent.putExtra(B_ARG_URI_LIST, list as Serializable)

            if(isFileMissing)
                intent.putExtra(B_ARG_FILE_MISSING, true)
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }


    private fun startDocFolderFragment() {
        toolbarTitle = "Select Folder"
        tb_doc_picker_toolbar_txt_count.visibility = View.GONE
        mMenuItem.isVisible = true

        val bundle = Bundle()
        bundle.putSerializable(DocPickerConfig.ARG_BUNDLE, mPickerConfig)

        val fragment = DocFolderFragment.newInstance(Constants.Fragment.DOC_FOLDER, bundle)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_content, fragment, DocFolderFragment::class.java.simpleName)
            .addToBackStack(null)
            .commit()
    }


    fun startDocFragment(folderPath: String,pickerConfig: DocPickerConfig) {
        toolbarTitle = "Choose Doc"
        tb_doc_picker_toolbar_txt_count.visibility = View.VISIBLE
        mMenuItem.isVisible = false

        val bundle = Bundle()
        bundle.putSerializable(DocPickerConfig.ARG_BUNDLE, pickerConfig)
        bundle.putString(DocFragment.B_ARG_FOLDER_PATH, folderPath)

        val fragment = DocFragment.newInstance(Constants.Fragment.DOC_LIST, bundle)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_content, fragment, DocFragment::class.java.simpleName)
            .addToBackStack(null)
            .commit()
    }


    fun startFragmentCreate() {
        val bundle = Bundle()
        bundle.putSerializable(DocPickerConfig.ARG_BUNDLE, mPickerConfig)

        info { "fragment: create" }
        mDocFilterFragment = DocFilterFragment.newInstance(Constants.Fragment.DOC_FILTER, bundle)
        (mDocFilterFragment as DocFilterFragment).setListener(object : DocFilterFragment.OnFilterDoneListener{
            override fun onFilterDone() {
                val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
                when (fragment) {
                    is DocFolderFragment -> {
                        info { "folder fragment" }
                        fragment.onFilterDone()
                    }
                    is DocFragment -> {
                        info { "doc fragment" }
                        fragment.onFilterDone()
                    }
                }
                mDocFilterFragment?.dismiss()
            }
        })
        mDocFilterFragment?.show(supportFragmentManager, DocFilterFragment::class.java.simpleName)
    }

}