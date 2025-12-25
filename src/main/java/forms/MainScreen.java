package forms;

import logic.KeywordMatch;
import logic.KeywordProcessor;
import logic.Keywords;
import logic.ScriptExecutor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class MainScreen {
    private JPanel contentPane;
    private JTextPane editPane;
    private JTextPane outputPane;
    private JTextPane errorPane;
    private JLabel statusLabel;
    private JButton runButton;

    private KeywordProcessor keywordProcessor;
    private ScriptExecutor scriptExecutor;

    public MainScreen() {
        keywordProcessor = new KeywordProcessor(new Keywords());
        scriptExecutor = new ScriptExecutor();
        // Initialize components
        contentPane = new JPanel(new GridBagLayout());
        editPane = new JTextPane();
        outputPane = new JTextPane();
        errorPane = new JTextPane();
        statusLabel = new JLabel("Ready");
        runButton = new JButton("Run");
        runButton.addActionListener(e -> runScript());

        // Configure components
        editPane.setEditable(true);

        setupDocumentListener();

        outputPane.setEditable(false);
        errorPane.setEditable(false);

        // Optional: make output/error panes look like text areas
        outputPane.setBackground(UIManager.getColor("TextArea.background"));
        errorPane.setBackground(UIManager.getColor("TextArea.background"));

        // Setup layout for main content
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Editor pane on the left (spanning two rows)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        JScrollPane editScrollPane = new JScrollPane(editPane);
        editScrollPane.setPreferredSize(new Dimension(300, 400));
        contentPane.add(editScrollPane, gbc);

        // Right column
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rpc = new GridBagConstraints();
        rpc.insets = new Insets(5, 5, 5, 5);
        rpc.gridx = 0;
        rpc.fill = GridBagConstraints.BOTH;
        rpc.weightx = 1.0;

        // Output pane (top)
        rpc.gridy = 0;
        rpc.weighty = 0.5;
        JScrollPane outputScrollPane = new JScrollPane(outputPane);
        outputScrollPane.setPreferredSize(new Dimension(300, 200));
        rightPanel.add(outputScrollPane, rpc);

        // Center panel with label + button
        rpc.gridy = 1;
        rpc.weighty = 0.0;
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(statusLabel);
        centerPanel.add(runButton);
        centerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        rightPanel.add(centerPanel, rpc);

        // Error pane (bottom)
        rpc.gridy = 2;
        rpc.weighty = 0.5;
        JScrollPane errorScrollPane = new JScrollPane(errorPane);
        errorScrollPane.setPreferredSize(new Dimension(300, 200));
        rightPanel.add(errorScrollPane, rpc);

        // Add right panel to main layout
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        contentPane.add(rightPanel, gbc);
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private void runScript() {
        String script = editPane.getText();
        statusLabel.setText("Running...");
        runButton.setEnabled(false);
        outputPane.setText("");
        errorPane.setText("");

        // Run in a separate thread to keep UI responsive
        new Thread(() -> {
            ScriptExecutor.ExecutionResult result = scriptExecutor.execute(
                    script,
                    line -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                Document doc = outputPane.getDocument();
                                doc.insertString(doc.getLength(), line, null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });
                        return null;
                    },
                    line -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                Document doc = errorPane.getDocument();
                                doc.insertString(doc.getLength(), line, null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });
                        return null;
                    }
            );
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(scriptExecutor.getStatusMessage(result));
                runButton.setEnabled(true);
            });
        }).start();
    }

    private void setupDocumentListener() {
        editPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleUpdate(e);
            }

            private void handleUpdate(DocumentEvent e) {
                if (e.getType() == DocumentEvent.EventType.CHANGE) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    StyledDocument doc = editPane.getStyledDocument();
                    Element root = doc.getDefaultRootElement();
                    int offset = e.getOffset();
                    int lineIndex = root.getElementIndex(offset);
                    Element line = root.getElement(lineIndex);
                    int start = line.getStartOffset();
                    int end = line.getEndOffset();

                    try {
                        String lineText = doc.getText(start, end - start);
                        List<KeywordMatch> matches = keywordProcessor.processLine(lineText, start);
                        highlightKeywords(matches, start, end);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            private void highlightKeywords(List<KeywordMatch> matches, int lineStart, int lineEnd) {
                StyledDocument doc = editPane.getStyledDocument();

                // Reset style for the whole line
                SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(defaultAttr, Color.BLACK);
                StyleConstants.setBold(defaultAttr, false);
                doc.setCharacterAttributes(lineStart, lineEnd - lineStart, defaultAttr, true);

                // Apply keyword style
                SimpleAttributeSet keywordAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(keywordAttr, Color.BLUE);
                StyleConstants.setBold(keywordAttr, true);

                for (KeywordMatch match : matches) {
                    doc.setCharacterAttributes(match.getStart(), match.getLength(), keywordAttr, false);
                }
            }
        });
    }
}
