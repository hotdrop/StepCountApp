package jp.hotdrop.stepcountapp.di.component

import dagger.Subcomponent
import jp.hotdrop.stepcountapp.ui.dashboard.DashboardFragment
import jp.hotdrop.stepcountapp.ui.googlefit.GoogleFitFragment
import jp.hotdrop.stepcountapp.ui.home.HomeFragment

@Subcomponent
interface FragmentComponent {
    fun inject(fragment: HomeFragment)
    fun inject(fragment: GoogleFitFragment)
    fun inject(fragment: DashboardFragment)
}