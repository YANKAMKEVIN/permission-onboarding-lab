package com.kevin.permissiononboardinglab

/**
 * The onboarding flow as a pure reducer. The OS dialog is simulated: like Android, a second
 * refusal blocks the prompt for good, and the only way forward is the (simulated) Settings screen.
 */
enum class PermissionStep { Value, Rationale, SystemAsk, Denied, Limited, Granted, Blocked }

data class PermissionFlowState(
    val step: PermissionStep = PermissionStep.Value,
    /** Refusals in the system dialog so far. */
    val denials: Int = 0,
    /** The simulated Settings screen is open (only from [PermissionStep.Blocked]). */
    val settingsOpen: Boolean = false
)

sealed interface PermissionAction {
    /** Main button under the preview. */
    data object Primary : PermissionAction
    /** Secondary ("Not now", "Back"...) button; may be absent on some steps. */
    data object Secondary : PermissionAction
    data object SystemAllow : PermissionAction
    data object SystemAllowSelected : PermissionAction
    data object SystemDeny : PermissionAction
    /** Demo shortcut from the step tabs. */
    data class JumpTo(val step: PermissionStep) : PermissionAction
}

const val DENIALS_BEFORE_BLOCK = 2

fun reduce(state: PermissionFlowState, action: PermissionAction): PermissionFlowState = when (action) {
    PermissionAction.SystemAllow ->
        if (state.step == PermissionStep.SystemAsk) state.copy(step = PermissionStep.Granted) else state

    PermissionAction.SystemAllowSelected ->
        if (state.step == PermissionStep.SystemAsk) state.copy(step = PermissionStep.Limited) else state

    PermissionAction.SystemDeny ->
        if (state.step != PermissionStep.SystemAsk) state
        else {
            val denials = state.denials + 1
            state.copy(
                step = if (denials >= DENIALS_BEFORE_BLOCK) PermissionStep.Blocked else PermissionStep.Denied,
                denials = denials
            )
        }

    is PermissionAction.JumpTo -> PermissionFlowState(
        step = action.step,
        denials = when (action.step) {
            PermissionStep.Blocked -> DENIALS_BEFORE_BLOCK
            PermissionStep.Denied -> 1
            else -> 0
        }
    )

    PermissionAction.Primary -> when (state.step) {
        PermissionStep.Value -> state.copy(step = PermissionStep.Rationale)
        PermissionStep.Rationale -> state.copy(step = PermissionStep.SystemAsk)
        PermissionStep.SystemAsk -> reduce(state, PermissionAction.SystemAllow)
        // Asking again is allowed once; the next refusal blocks the dialog.
        PermissionStep.Denied, PermissionStep.Limited -> state.copy(step = PermissionStep.SystemAsk)
        PermissionStep.Blocked ->
            if (state.settingsOpen) PermissionFlowState(step = PermissionStep.Granted)
            else state.copy(settingsOpen = true)
        PermissionStep.Granted -> PermissionFlowState()
    }

    PermissionAction.Secondary -> when (state.step) {
        // "Not now" before the system dialog is a soft refusal: nothing is burned.
        PermissionStep.Value, PermissionStep.Rationale -> state.copy(step = PermissionStep.Denied)
        PermissionStep.SystemAsk -> reduce(state, PermissionAction.SystemDeny)
        PermissionStep.Blocked ->
            if (state.settingsOpen) state.copy(settingsOpen = false) else PermissionFlowState()
        PermissionStep.Denied, PermissionStep.Limited -> PermissionFlowState()
        PermissionStep.Granted -> state
    }
}

fun primaryLabel(state: PermissionFlowState): String = when (state.step) {
    PermissionStep.Value -> "Continue"
    PermissionStep.Rationale -> "Allow access"
    PermissionStep.SystemAsk -> "Allow"
    PermissionStep.Denied -> "Ask again"
    PermissionStep.Limited -> "Allow all"
    PermissionStep.Blocked -> if (state.settingsOpen) "Turn on" else "Open settings"
    PermissionStep.Granted -> "Restart demo"
}

/** Null when the step has no secondary action. */
fun secondaryLabel(state: PermissionFlowState): String? = when (state.step) {
    PermissionStep.Value, PermissionStep.Rationale -> "Not now"
    PermissionStep.SystemAsk -> "Don't allow"
    PermissionStep.Denied -> "Use manual"
    PermissionStep.Limited -> "Keep limited"
    PermissionStep.Blocked -> if (state.settingsOpen) "Back" else "Use manual"
    PermissionStep.Granted -> null
}
