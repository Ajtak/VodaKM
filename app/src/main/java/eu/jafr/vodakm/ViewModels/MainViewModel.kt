package eu.jafr.vodakm.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _textData = MutableLiveData<String>()

    private val _kmRemain = MutableLiveData<Double>()

    val textData: LiveData<String>
        get() = _textData

    fun updateTextData(newData: String) {
        _textData.value = newData
    }

    val kmRemain: LiveData<Double>
        get() = _kmRemain

    fun updateKmRemain(newData: Double) {
        _kmRemain.value = newData
    }
}
