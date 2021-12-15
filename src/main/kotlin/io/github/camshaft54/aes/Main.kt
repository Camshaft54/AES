package io.github.camshaft54.aes

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import io.github.camshaft54.aes.panels.AESPanel
import io.github.camshaft54.aes.panels.CBCPanel
import java.awt.Color
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Main code for Swing GUI window
 */
fun main() {
    // Use darklaf to get Darcula-style dark mode for look and feel of Swing
    LafManager.install()
    LafManager.install(DarculaTheme())

    val aesPanel = AESPanel()
    val cbcPanel = CBCPanel()
    val rootPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = Color.DARK_GRAY
        add(aesPanel)
        add(Box.createVerticalStrut(10))
        add(cbcPanel)
    }
    val frame = JFrame("AES").apply { add(rootPanel) }

    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.size = Dimension(600, 400)
    frame.pack()
    frame.isVisible = true
    frame.isResizable = false
}
