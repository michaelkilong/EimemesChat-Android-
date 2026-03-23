package com.eimemes.chat.ui.personalization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eimemes.chat.domain.model.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PersonalizationUiState(
    val tone: String = "Friendly",
    val nickname: String = "",
    val occupation: String = "",
    val customInstructions: String = "",
    val saving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalizationUiState())
    val state: StateFlow<PersonalizationUiState> = _state.asStateFlow()

    init { loadPreferences() }

    private fun loadPreferences() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                val doc = firestore.collection("users").document(uid).get().await()
                val prefs = doc.get("preferences") as? Map<*, *>
                prefs?.let {
                    _state.update { s -> s.copy(
                        tone = it["tone"] as? String ?: "Friendly",
                        nickname = it["nickname"] as? String ?: "",
                        occupation = it["occupation"] as? String ?: "",
                        customInstructions = it["customInstructions"] as? String ?: ""
                    )}
                }
            }
        }
    }

    fun setTone(v: String) = _state.update { it.copy(tone = v) }
    fun setNickname(v: String) = _state.update { it.copy(nickname = v) }
    fun setOccupation(v: String) = _state.update { it.copy(occupation = v) }
    fun setCustomInstructions(v: String) = _state.update { it.copy(customInstructions = v) }

    fun save(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .set(mapOf("preferences" to mapOf(
                        "tone" to _state.value.tone,
                        "nickname" to _state.value.nickname,
                        "occupation" to _state.value.occupation,
                        "customInstructions" to _state.value.customInstructions
                    )), SetOptions.merge()).await()
                _state.update { it.copy(saving = false) }
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(saving = false, error = e.message) }
            }
        }
    }
}
