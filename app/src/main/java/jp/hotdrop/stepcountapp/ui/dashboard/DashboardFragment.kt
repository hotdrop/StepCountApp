package jp.hotdrop.stepcountapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import javax.inject.Inject

class DashboardFragment: Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<DashboardViewModel>
    private val viewModel: DashboardViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        component?.inject(this)
        return root
    }
}
