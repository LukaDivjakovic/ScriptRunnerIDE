package forms

import javax.swing.*

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Script Runner IDE")
        val mainScreen = MainScreen()
        frame.contentPane = mainScreen.contentPane
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(800, 600)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}

