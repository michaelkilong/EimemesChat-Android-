package com.eimemes.chat.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eimemes.chat.data.remote.ChatApiService
import com.eimemes.chat.data.remote.StreamEvent
import com.eimemes.chat.data.repository.AuthRepository
import com.eimemes.chat.data.repository.ConversationRepository
import com.eimemes.chat.domain.model.Attachment
import com.eimemes.chat.domain.model.Conversation
import com.eimemes.chat.domain.model.Message
import com.eimemes.chat.domain.model.Source
import com.eimemes.chat.util.DateUtil
import com.eimemes.chat.util.HapticUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val activeConvId: String? = null,
    val messages: List<Message> = emptyList(),
    val streamingText: String = "",
    val isStreaming: Boolean = false,
    val isSearching: Boolean = false,
    val sources: List<Source>? = null,
    val inputText: String = "",
    val webSearchEnabled: Boolean = false,
    val attachment: Attachment? = null,
    val sidebarOpen: Boolean = false,
    val error: String? = null,
    val dailyLimitReached: Boolean = false,
    val isFirstSession: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatApi: ChatApiService,
    private val convRepo: ConversationRepository,
    private val authRepo: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            convRepo.observeConversations().collect { convs ->
                _state.update { it.copy(conversations = convs, isFirstSession = convs.isEmpty()) }
            }
        }
    }

    fun newConversation() {
        HapticUtil.light(context)
        _state.update { it.copy(
            activeConvId  = null,
            messages      = emptyList(),
            streamingText = "",
            sources       = null,
            sidebarOpen   = false
        )}
    }

    fun selectConversation(convId: String) {
        HapticUtil.light(context)
        viewModelScope.launch {
            val conv = convRepo.getConversation(convId)
            _state.update { it.copy(
                activeConvId = convId,
                messages     = conv?.messages ?: emptyList(),
                sources      = null,
                sidebarOpen  = false
            )}
        }
    }

    fun deleteConversation(convId: String) {
        HapticUtil.medium(context)
        viewModelScope.launch {
            convRepo.deleteConversation(convId)
            if (_state.value.activeConvId == convId) {
                _state.update { it.copy(activeConvId = null, messages = emptyList()) }
            }
        }
    }

    fun onInputChange(text: String) = _state.update { it.copy(inputText = text) }

    fun toggleWebSearch() {
        HapticUtil.light(context)
        _state.update { it.copy(webSearchEnabled = !it.webSearchEnabled) }
    }

    fun setAttachment(attachment: Attachment?) = _state.update { it.copy(attachment = attachment) }
    fun setSidebarOpen(open: Boolean) = _state.update { it.copy(sidebarOpen = open) }
    fun clearError() = _state.update { it.copy(error = null) }
    fun showError(msg: String) = _state.update { it.copy(error = msg) }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isStreaming) return

        HapticUtil.medium(context)

        val userMsg = Message(role = "user", content = text, time = DateUtil.now())

        _state.update { it.copy(
            inputText     = "",
            messages      = it.messages + userMsg,
            streamingText = "",
            isStreaming   = true,
            isSearching   = false,
            sources       = null,
            error         = null
        )}

        streamJob = viewModelScope.launch {
            try {
                val convId = _state.value.activeConvId ?: convRepo.createConversation().also { id ->
                    _state.update { it.copy(activeConvId = id) }
                }

                val idToken   = authRepo.getIdToken()
                val history   = _state.value.messages.dropLast(1)
                val isFirst   = history.isEmpty()
                val webSearch = _state.value.webSearchEnabled

                convRepo.saveMessage(convId, userMsg)

                chatApi.streamChat(
                    message        = text,
                    history        = history,
                    isFirstMessage = isFirst,
                    idToken        = idToken,
                    attachment     = _state.value.attachment,
                    useWebSearch   = webSearch
                ).collect { event ->
                    when (event) {
                        is StreamEvent.Token -> _state.update { it.copy(
                            streamingText = it.streamingText + event.text,
                            isSearching   = false
                        )}
                        is StreamEvent.Searching -> _state.update { it.copy(isSearching = true) }
                        is StreamEvent.Done -> {
                            val assistantMsg = Message(
                                role       = "assistant",
                                content    = _state.value.streamingText,
                                time       = DateUtil.now(),
                                model      = event.model,
                                disclaimer = event.disclaimer,
                                sources    = event.sources
                            )
                            convRepo.saveMessage(convId, assistantMsg)
                            HapticUtil.success(context)
                            _state.update { it.copy(
                                messages         = it.messages + assistantMsg,
                                streamingText    = "",
                                isStreaming      = false,
                                isSearching      = false,
                                sources          = event.sources,
                                attachment       = null,
                                webSearchEnabled = false
                            )}
                        }
                        is StreamEvent.Title -> {
                            convRepo.updateTitle(convId, event.title)
                        }
                        is StreamEvent.OutputBlocked -> {
                            val blockedMsg = Message(
                                role    = "assistant",
                                content = event.reply.ifBlank { "I can't respond to that." },
                                time    = DateUtil.now()
                            )
                            convRepo.saveMessage(convId, blockedMsg)
                            HapticUtil.error(context)
                            _state.update { it.copy(
                                messages      = it.messages + blockedMsg,
                                streamingText = "",
                                isStreaming   = false,
                                isSearching   = false
                            )}
                        }
                        is StreamEvent.Error -> {
                            HapticUtil.error(context)
                            _state.update { it.copy(
                                isStreaming       = false,
                                isSearching       = false,
                                streamingText     = "",
                                error             = event.message,
                                dailyLimitReached = event.message.contains("limit", ignoreCase = true)
                            )}
                        }
                    }
                }
            } catch (e: Exception) {
                HapticUtil.error(context)
                _state.update { it.copy(
                    isStreaming   = false,
                    isSearching   = false,
                    streamingText = "",
                    error         = e.message ?: "Something went wrong"
                )}
            }
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        val partial = _state.value.streamingText
        if (partial.isNotBlank()) {
            val msg = Message(role = "assistant", content = partial, time = DateUtil.now())
            _state.update { it.copy(messages = it.messages + msg, streamingText = "", isStreaming = false) }
        } else {
            _state.update { it.copy(streamingText = "", isStreaming = false) }
        }
    }
}
