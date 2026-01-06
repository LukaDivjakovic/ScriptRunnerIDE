import forms.MainScreen
import javax.swing.*

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    // Launch the main user interface on the Event Dispatch Thread
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

