// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/MemberSession.kt
package hearthseed.core.data

/**
 * Who is using the app. Hearthseed has no sign-in screen yet: the app runs
 * as a demo member. The test server accepts any "demo-token-..." value;
 * a real token would come from a sign-in flow and secure storage (Chapter 21).
 */
data class MemberSession(
    val memberId: String,
    val displayName: String,
    val token: String,
) {
    companion object {
        val Demo = MemberSession("mem-003", "Cleo Marsh", "demo-token-cleo")
    }
}
