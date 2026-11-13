package com.islamicstudies.academy

sealed interface ImportState {
    data object Loading : ImportState
    data object Ready : ImportState
    data class Failed(val message: String) : ImportState
}
