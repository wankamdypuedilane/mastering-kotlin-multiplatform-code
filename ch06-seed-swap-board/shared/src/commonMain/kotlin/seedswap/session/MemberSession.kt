// ch06-seed-swap-board/shared/src/commonMain/.../session/MemberSession.kt
package seedswap.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** App-wide state: who is using the board. One instance per process. */
class MemberSession {                    // AppContainer holds the one instance
    private val _member = MutableStateFlow<String?>(null)
    val member: StateFlow<String?> = _member.asStateFlow()

    fun signIn(name: String) {
        _member.value = name.trim().ifEmpty { null }
    }

    fun signOut() {
        _member.value = null
    }
}

/** One of several ViewModels that observe the same session. */
class BoardHeaderViewModel(session: MemberSession) : ViewModel() {
    val greeting: StateFlow<String> = session.member
        .map { name ->
            if (name == null) "Browsing as a guest" else "Signed in as $name"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
}
