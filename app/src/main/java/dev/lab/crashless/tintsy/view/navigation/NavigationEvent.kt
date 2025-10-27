package dev.lab.crashless.tintsy.view.navigation

sealed class NavigationEvent {
    data object NavigateToSuccess: NavigationEvent()
    data object NavigateToFailure: NavigationEvent()
}