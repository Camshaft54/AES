@file:Suppress("DuplicatedCode")

package io.github.camshaft54.aes.panels

import io.github.camshaft54.aes.algorithms.cbcDecrypt
import io.github.camshaft54.aes.algorithms.cbcEncrypt
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * JPanel with inputs and outputs for entering data and printing the encrypted/decrypted CBC result
 * The code in this file is very similar to AESPanel. The only difference is the input verification and various labels
 */
class CBCPanel : JPanel(), ActionListener, DocumentListener {
    private val keyTextArea = createTextArea("128-bit UTF-8 Key:")
    private val dataTextArea = createTextArea("UTF-8 Data:")
    private val resultTextArea = createTextArea("Result:").also { it.isEditable = false }
    private val encryptButton = JButton("Encrypt").also {
        it.isEnabled = false
        it.addActionListener(this)
    }
    private val decryptButton = JButton("Decrypt").also {
        it.isEnabled = false
        it.addActionListener(this)
    }
    private val buttons = Box(BoxLayout.X_AXIS).apply {
        add(Box.createHorizontalGlue())
        add(encryptButton)
        add(Box.createRigidArea(Dimension(5, 1)))
        add(decryptButton)
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val title = JLabel("CBC Encrypt & Decrypt").apply {
            font = Font("JetBrains Mono", Font.PLAIN, 20)
        }
        val titleBox = Box(BoxLayout.X_AXIS).apply {
            add(Box.createHorizontalGlue())
            add(title)
            add(Box.createHorizontalGlue())
        }
        border = EmptyBorder(5, 5, 5, 5)
        add(titleBox)
        add(keyTextArea.parent.parent.parent)
        add(Box.createVerticalStrut(5))
        add(dataTextArea.parent.parent.parent)
        add(Box.createVerticalStrut(5))
        add(buttons)
        add(Box.createVerticalStrut(5))
        add(resultTextArea.parent.parent.parent)
    }

    override fun actionPerformed(e: ActionEvent) {
        SwingUtilities.invokeLater invokeLater@{
            resultTextArea.text = when (e.actionCommand) {
                "Encrypt" -> cbcEncrypt(keyTextArea.text, dataTextArea.text)
                "Decrypt" -> cbcDecrypt(keyTextArea.text, dataTextArea.text)
                else -> return@invokeLater
            }
        }
    }

    override fun insertUpdate(e: DocumentEvent) {
        checkFields()
    }

    override fun removeUpdate(e: DocumentEvent) {
        checkFields()
    }

    /**
     * Check that key text area is 16 bytes (128-bit) and that the data text area is not empty beforing allowing user to
     * click buttons
     */
    private fun checkFields() {
        if (keyTextArea.text.toByteArray().size == 16 && dataTextArea.text.isNotEmpty()) {
            encryptButton.isEnabled = true
            decryptButton.isEnabled = true
        } else {
            encryptButton.isEnabled = false
            decryptButton.isEnabled = false
        }
    }

    private fun createTextArea(name: String): JTextArea {
        val box = Box(BoxLayout.X_AXIS)
        val textArea = JTextArea(1, 35).also {
            it.document.addDocumentListener(this)
            it.font = Font("JetBrains Mono", Font.PLAIN, 20)
            it.maximumSize = it.preferredSize
        }
        val label = JLabel(name).also {
            it.font = Font("JetBrains Mono", Font.PLAIN, 20)
        }
        box.add(Box.createHorizontalGlue())
        box.add(label)
        box.add(JScrollPane(textArea))
        return textArea
    }

    override fun changedUpdate(e: DocumentEvent) {}
}