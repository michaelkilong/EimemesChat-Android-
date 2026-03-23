package com.eimemes.chat.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eimemes.chat.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val user: FirebaseUser? = null,
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.authState.collect { user ->
                _state.update { it.copy(user = user, loading = false) }
            }
        }
    }

    fun getGoogleSignInIntent(webClientId: String) =
        authRepo.getGoogleSignInClient(webClientId).signInIntent

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                authRepo.signInWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Sign-in failed") }
            }
        }
    }

    fun signOut() = authRepo.signOut()
    fun clearError() = _state.update { it.copy(error = null) }
}
