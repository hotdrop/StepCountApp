package jp.hotdrop.stepcountapp.di.component

import android.app.Activity
import androidx.fragment.app.Fragment

interface DaggerComponentProvider {
    val component: AppComponent
}

val Activity.component
    get() = (application as DaggerComponentProvider).component.plus()

val Fragment.component
    get() = requireActivity().component.plus()