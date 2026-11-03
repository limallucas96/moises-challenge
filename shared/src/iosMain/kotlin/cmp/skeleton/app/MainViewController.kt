package cmp.skeleton.app

import androidx.compose.ui.window.ComposeUIViewController
import cmp.skeleton.app.di.initKoin
import platform.UIKit.UIViewController

/**
 * Chamado pelo iosApp em Swift (`MainViewControllerKt.MainViewController()`). O nome em
 * PascalCase é o contrato com o Xcode, por isso a regra de nome de função fica suspensa aqui.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
