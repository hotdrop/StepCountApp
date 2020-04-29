package jp.hotdrop.stepcountapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory<VM: ViewModel> @Inject constructor(
    private val vm: Provider<VM>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return vm.get() as T
    }
}