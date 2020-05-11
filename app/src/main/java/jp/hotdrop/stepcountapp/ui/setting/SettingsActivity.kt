package jp.hotdrop.stepcountapp.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import jp.hotdrop.stepcountapp.BuildConfig
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.di.component.component
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        component.inject(this)

        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app_version.text = BuildConfig.VERSION_NAME
        licenses_area.setOnClickListener {
            Snackbar.make(snack_bar_area, "未実装です。", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}