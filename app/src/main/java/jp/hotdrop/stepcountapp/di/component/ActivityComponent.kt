package jp.hotdrop.stepcountapp.di.component

import dagger.Subcomponent
import jp.hotdrop.stepcountapp.ui.MainActivity

@Subcomponent
interface ActivityComponent {
    fun inject(activity: MainActivity)
    fun plus(): FragmentComponent
}