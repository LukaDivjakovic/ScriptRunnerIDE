import forms.MainScreen
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Script Runner IDE")
        val mainScreen = MainScreen()
        frame.contentPane = mainScreen.contentPane
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.extendedState = JFrame.MAXIMIZED_BOTH
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}

