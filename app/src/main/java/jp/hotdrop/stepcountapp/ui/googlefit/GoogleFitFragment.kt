package jp.hotdrop.stepcountapp.ui.googlefit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.hotdrop.stepcountapp.R
import kotlinx.android.synthetic.main.fragment_google_fit.*

class GoogleFitFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_google_fit, container, false)
        return root
    }
}
