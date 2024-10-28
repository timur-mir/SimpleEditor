package home.product.editor.main

import home.product.editor.base.BaseView

interface MainView : BaseView {
    fun navigateToHome()
    fun navigateToContent()
}