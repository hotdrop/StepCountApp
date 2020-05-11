package jp.hotdrop.stepcountapp.di.component

import dagger.Subcomponent
import jp.hotdrop.stepcountapp.ui.MainActivity
import jp.hotdrop.stepcountapp.ui.setting.SettingsActivity

@Subcomponent
interface ActivityComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: SettingsActivity)
    fun plus(): FragmentComponent
}