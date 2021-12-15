@file:Suppress("DuplicatedCode")

package io.github.camshaft54.aes.panels

import io.github.camshaft54.aes.algorithms.aesDecrypt
import io.github.camshaft54.aes.algorithms.aesEncrypt
import io.github.camshaft54.aes.algorithms.hexToDecArray
import io.github.camshaft54.aes.algorithms.printHex
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * JPanel for entering and recieving AES encrypted/decrypted data
 */
class AESPanel : JPanel(), ActionListener, DocumentListener {
    // Initialize text areas for key, data, and result
    private val keyTextArea = createTextArea("128-bit Hex Key:")
    private val dataTextArea = createTextArea("128-bit Hex Data:")
    private val resultTextArea = createTextArea("Result:").also { it.isEditable = false }

    // Add buttons add and add them to the action listener
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
        // Use BoxLayout for overall frame and add the various text areas and buttons
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val title = JLabel("AES Hex Encrypt & Decrypt").apply {
            font = Font("JetBrains Mono", Font.PLAIN, 20)
        }
        val titleBox = Box(BoxLayout.X_AXIS).apply {
            add(Box.createHorizontalGlue())
            add(title)
            add(Box.createHorizontalGlue())
        }
        border = EmptyBorder(5, 5, 5, 5)
        // Add parent of parent of parent of textArea because create textArea method returns the textArea which is inside a JScrollPane in a box with a label
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
        // Check input and say invalid input if bad
        SwingUtilities.invokeLater invokeLater@{
            val key = processInput(keyTextArea.text) ?: run {
                resultTextArea.text = "Invalid Input!"
                return@invokeLater
            }
            val data = processInput(dataTextArea.text) ?: run {
                resultTextArea.text = "Invalid Input!"
                return@invokeLater
            }
            // If it is not, perform AES decrypt or decrypt and update the result text to that
            resultTextArea.text = when (e.actionCommand) {
                "Encrypt" -> aesEncrypt(key, data)
                "Decrypt" -> aesDecrypt(key, data)
                else -> return@invokeLater
            }.printHex()
        }
    }

    // If the user changes a text area, check the input fields to see if the input can be encrypted/decrypted
    override fun insertUpdate(e: DocumentEvent) {
        checkFields()
    }

    override fun removeUpdate(e: DocumentEvent) {
        checkFields()
    }

    // Make sure input is either 32 (no spaces hex state) or 35 (hex state with spaces)
    private fun checkFields() {
        if ((keyTextArea.text.length == 32 || keyTextArea.text.length == 35)
            && (dataTextArea.text.length == 32 || dataTextArea.text.length == 35)
        ) {
            encryptButton.isEnabled = true
            decryptButton.isEnabled = true
        } else {
            encryptButton.isEnabled = false
            decryptButton.isEnabled = false
        }
    }

    /**
     * Verify the input as hexadecimal with or without spaces thoroughly before converting it to a 2d array representing the state
     */
    private fun processInput(text: String): Array<Array<Int>>? {
        return if (text.matches("([A-Fa-f0-9]{32})".toRegex())) {
            text.replace(".{8}".toRegex(), "$0 ").hexToDecArray()
        } else if (text.matches("([A-Fa-f0-9]{8} ){3}[A-Fa-f0-9]{8}".toRegex())) {
            text.hexToDecArray()
        } else {
            null
        }
    }

    /**
     * Create a text area contained in a box with a label of the specified name
     */
    private fun createTextArea(name: String): JTextArea {
        val box = Box(BoxLayout.X_AXIS)
        val textArea = JTextArea(1, 35).also {
            // Add textArea to document listener to verify input when user types
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