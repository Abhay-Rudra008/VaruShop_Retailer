package com.example.varushopretailer.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object SessionManager {
    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()


    fun triggerLogout() {
        CoroutineScope(Dispatchers.Main).launch {
            _logoutEvent.emit(Unit)
        }
    }
}