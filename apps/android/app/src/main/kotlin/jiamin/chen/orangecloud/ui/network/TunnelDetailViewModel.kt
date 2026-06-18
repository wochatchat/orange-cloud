package jiamin.chen.orangecloud.ui.network

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jiamin.chen.orangecloud.core.auth.AuthRepository
import jiamin.chen.orangecloud.core.auth.Scopes
import jiamin.chen.orangecloud.data.model.Tunnel
import jiamin.chen.orangecloud.data.repository.AccountStore
import jiamin.chen.orangecloud.data.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TunnelDetailUiState(
    val tunnel: Tunnel? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val missingScope: Boolean = false,
)

/** 单条隧道详情：信息 + 内嵌活跃连接（对齐 iOS TunnelDetailView）。 */
@HiltViewModel
class TunnelDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountStore: AccountStore,
    private val securityRepository: SecurityRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val tunnelId: String = checkNotNull(savedStateHandle["tunnelId"])
    val tunnelName: String = savedStateHandle.get<String>("tunnelName").orEmpty()
    private val hasScope = authRepository.hasScope(Scopes.TUNNEL_READ)

    private val _uiState = MutableStateFlow(
        TunnelDetailUiState(isLoading = hasScope, missingScope = !hasScope),
    )
    val uiState: StateFlow<TunnelDetailUiState> = _uiState.asStateFlow()

    init {
        if (hasScope) load()
    }

    fun load() {
        if (!hasScope) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            try {
                accountStore.ensureLoaded()
                val accountId = accountStore.selectedAccountId.value ?: error("no account")
                val tunnel = securityRepository.getTunnel(accountId, tunnelId)
                _uiState.update { it.copy(tunnel = tunnel) }
            } catch (e: Exception) {
                _uiState.update { it.copy(hasError = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
