package cmp.skeleton.app.feature.beta.presentation

internal data class BetaState(
    val message: String,
    val tapCount: Int = 0,
)

internal sealed interface BetaAction {
    data object OnTapClick : BetaAction
}
