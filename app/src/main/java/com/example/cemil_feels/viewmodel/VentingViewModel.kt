package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VentingViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood.asStateFlow()

    private val _storyText = MutableStateFlow("")
    val storyText: StateFlow<String> = _storyText.asStateFlow()

    private val _charCounter = MutableStateFlow("0/1000")
    val charCounter: StateFlow<String> = _charCounter.asStateFlow()

    sealed class VentingEvent {
        data class NavigateToRecommendations(val story: String?, val mood: String?) : VentingEvent()
        data class ShowValidationToast(val message: String) : VentingEvent()
    }

    private val _eventFlow = MutableSharedFlow<VentingEvent>()
    val eventFlow: SharedFlow<VentingEvent> = _eventFlow.asSharedFlow()

    fun toggleMood(moodName: String) {
        _selectedMood.value = if (_selectedMood.value == moodName) null else moodName
    }

    fun onStoryTextChanged(text: String) {
        _storyText.value = text
        _charCounter.value = "${text.length}/1000"
    }

    fun submitMood() {
        val story = _storyText.value.trim()
        val mood = _selectedMood.value

        if (story.isEmpty() && mood == null) {
            viewModelScope.launch {
                _eventFlow.emit(VentingEvent.ShowValidationToast("Silakan pilih perasaanmu atau tulis ceritamu terlebih dahulu!"))
            }
            return
        }

        viewModelScope.launch {
            _eventFlow.emit(VentingEvent.NavigateToRecommendations(story.ifEmpty { null }, mood))
        }
    }
}
