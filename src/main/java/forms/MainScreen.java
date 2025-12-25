package forms;

import logic.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private ErrorLinkProcessor errorLinkProcessor;

    public MainScreen() {
        keywordProcessor = new KeywordProcessor(new Keywords());
        scriptExecutor = new ScriptExecutor();
        errorLinkProcessor = new ErrorLinkProcessor();
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
        setupErrorPane();

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
                    text -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                StyledDocument doc = errorPane.getStyledDocument();
                                int startPos = doc.getLength();
                                doc.insertString(startPos, text, null);

                                // Get the whole text to find links (it's simpler than incremental for regex)
                                String fullText = doc.getText(0, doc.getLength());
                                List<ErrorLink> links = errorLinkProcessor.findLinks(fullText);

                                // Clear existing link attributes and apply new ones
                                SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
                                doc.setCharacterAttributes(0, fullText.length(), defaultAttr, true);

                                SimpleAttributeSet linkAttr = new SimpleAttributeSet();
                                StyleConstants.setForeground(linkAttr, Color.BLUE);
                                StyleConstants.setUnderline(linkAttr, true);

                                for (ErrorLink link : links) {
                                    SimpleAttributeSet currentLinkAttr = new SimpleAttributeSet(linkAttr);
                                    currentLinkAttr.addAttribute("line", link.getLine());
                                    currentLinkAttr.addAttribute("column", link.getColumn());
                                    doc.setCharacterAttributes(link.getStart(), link.getEnd() - link.getStart(), currentLinkAttr, false);
                                }
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

    private void setupErrorPane() {
        errorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int pos = errorPane.viewToModel2D(e.getPoint());
                if (pos >= 0) {
                    StyledDocument doc = errorPane.getStyledDocument();
                    Element element = doc.getCharacterElement(pos);
                    AttributeSet attrs = element.getAttributes();
                    Object line = attrs.getAttribute("line");
                    Object column = attrs.getAttribute("column");
                    if (line instanceof Integer && column instanceof Integer) {
                        goToLine((Integer) line, (Integer) column);
                    }
                }
            }
        });

        errorPane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = errorPane.viewToModel2D(e.getPoint());
                if (pos >= 0) {
                    StyledDocument doc = errorPane.getStyledDocument();
                    Element element = doc.getCharacterElement(pos);
                    AttributeSet attrs = element.getAttributes();
                    if (attrs.getAttribute("line") != null) {
                        errorPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
                errorPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void goToLine(int line, int column) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = editPane.getStyledDocument();
                Element root = doc.getDefaultRootElement();
                if (line > 0 && line <= root.getElementCount()) {
                    Element lineElement = root.getElement(line - 1);
                    int startOffset = lineElement.getStartOffset();
                    int endOffset = lineElement.getEndOffset();
                    int targetOffset = startOffset + column - 1;
                    if (targetOffset > endOffset) targetOffset = endOffset;

                    editPane.requestFocusInWindow();
                    editPane.setCaretPosition(targetOffset);

                    // Ensure the caret is visible
                    Rectangle viewRect = editPane.modelToView2D(targetOffset).getBounds();
                    editPane.scrollRectToVisible(viewRect);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupDocumentListener() {
        editPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleUpdate(e); }

            @Override
            public void removeUpdate(DocumentEvent e) { handleUpdate(e); }

            @Override
            public void changedUpdate(DocumentEvent e) { handleUpdate(e); }

            private void handleUpdate(DocumentEvent e) {
                if (e.getType() == DocumentEvent.EventType.CHANGE) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    try {
                        StyledDocument doc = editPane.getStyledDocument();
                        Element root = doc.getDefaultRootElement();

                        int startOffset = e.getOffset();
                        int changedLength = e.getLength(); // inserted or removed length
                        int docLength = doc.getLength();

                        // Compute safe end offset for determining last affected line.
                        // Use startOffset + changedLength - 1 (last changed char). Clamp to [0, docLength-1].
                        int endOffsetCandidate = Math.max(0, startOffset + Math.max(0, changedLength) - 1);
                        int endOffsetForIndex = Math.min(Math.max(0, docLength - 1), endOffsetCandidate);

                        int startLineIndex = root.getElementIndex(Math.min(startOffset, Math.max(0, docLength - 1)));
                        int endLineIndex = root.getElementIndex(endOffsetForIndex);

                        for (int lineIndex = startLineIndex; lineIndex <= endLineIndex; lineIndex++) {
                            Element line = root.getElement(lineIndex);
                            int lineStart = line.getStartOffset();
                            int lineEnd = line.getEndOffset();
                            try {
                                String lineText = doc.getText(lineStart, Math.max(0, lineEnd - lineStart));
                                List<logic.KeywordMatch> matches = keywordProcessor.processLine(lineText, lineStart);
                                highlightKeywords(matches, lineStart, lineEnd);
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            private void highlightKeywords(List<logic.KeywordMatch> matches, int lineStart, int lineEnd) {
                StyledDocument doc = editPane.getStyledDocument();

                // Reset style for the whole line
                SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(defaultAttr, Color.BLACK);
                StyleConstants.setBold(defaultAttr, false);
                doc.setCharacterAttributes(lineStart, Math.max(0, lineEnd - lineStart), defaultAttr, true);

                // Apply keyword style
                SimpleAttributeSet keywordAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(keywordAttr, Color.BLUE);
                StyleConstants.setBold(keywordAttr, true);

                for (logic.KeywordMatch match : matches) {
                    int start = match.getStart();
                    int length = match.getLength();
                    // ensure attributes application stays within current document bounds
                    int safeStart = Math.max(lineStart, Math.min(start, doc.getLength()));
                    int safeLen = Math.max(0, Math.min(length, doc.getLength() - safeStart));
                    if (safeLen > 0) {
                        doc.setCharacterAttributes(safeStart, safeLen, keywordAttr, false);
                    }
                }
            }
        });
    }
}
