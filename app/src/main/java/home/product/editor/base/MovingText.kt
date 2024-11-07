package home.product.editor.base

import android.widget.TextView

fun TextView.setMovingText(text: String) = this.post {
    val textWidth = this.paint.measureText(text)
    val spaceWidth = this.paint.measureText(" ")
    val requiredAdditionalSpace = ((this.width - textWidth) / spaceWidth).toInt()
    this.text = StringBuilder(text).apply {
        for (i in 0..requiredAdditionalSpace) {
            append(" ")
        }
    }
    this.isSelected = true
}