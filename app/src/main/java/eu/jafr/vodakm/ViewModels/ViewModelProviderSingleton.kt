package eu.jafr.vodakm.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

object ViewModelProviderSingleton {
    private var viewModel: MainViewModel? = null

    fun getViewModel(context: Context): MainViewModel {
        if (viewModel == null) {
            viewModel = ViewModelProvider(context as ViewModelStoreOwner).get(MainViewModel::class.java)
        }
        return viewModel!!
    }
}
