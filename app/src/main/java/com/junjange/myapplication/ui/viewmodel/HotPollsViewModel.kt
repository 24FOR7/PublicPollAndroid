package com.junjange.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.junjange.myapplication.data.ModelBoard
import com.junjange.myapplication.repository.HotPollsRepository
import com.junjange.myapplication.repository.PollsRepository
import kotlinx.coroutines.launch

class HotPollsViewModel (private val repository: HotPollsRepository) : ViewModel() {

    private val _retrofitTodoList = MutableLiveData<ModelBoard>()

    // LiveData
    val retrofitTodoList: MutableLiveData<ModelBoard>
        get() = _retrofitTodoList

    init { // 초기화 시 서버에서 데이터를 받아옵니다.
        viewModelScope.launch {
            _retrofitTodoList.value = repository.retrofitSelectAllTodo()
        }
    }


    class Factory(private val application : Application) : ViewModelProvider.Factory { // factory pattern
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HotPollsViewModel(HotPollsRepository.getInstance(application)!!) as T
        }
    }


}