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

    private static final Color BG_COLOR = new Color(43, 43, 43);
    private static final Color EDITOR_BG = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(169, 183, 198);
    private static final Color ACCENT_COLOR = new Color(75, 110, 175);
    private static final Color KEYWORD_COLOR = new Color(204, 120, 50);

    public MainScreen() {
        keywordProcessor = new KeywordProcessor(new Keywords());
        scriptExecutor = new ScriptExecutor();
        errorLinkProcessor = new ErrorLinkProcessor();

        // Initialize components
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BG_COLOR);

        editPane = new JTextPane();
        setupTextPane(editPane, true);

        outputPane = new JTextPane();
        setupTextPane(outputPane, false);

        errorPane = new JTextPane();
        setupTextPane(errorPane, false);

        statusLabel = new JLabel("READY");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(ACCENT_COLOR);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        runButton = new JButton("RUN");
        runButton.setBackground(new Color(60, 140, 60)); // Distinct green for Run
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        runButton.setFocusPainted(false);
        runButton.setContentAreaFilled(false);
        runButton.setOpaque(true);
        runButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 110, 45), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        runButton.addActionListener(e -> runScript());

        // Toolbar / Header
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_COLOR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        toolbar.add(statusLabel, BorderLayout.WEST);
        toolbar.add(runButton, BorderLayout.EAST);
        contentPane.add(toolbar, BorderLayout.NORTH);

        // Panes and Splitters
        JScrollPane editScrollPane = createScrollPane(editPane);
        JScrollPane outputScrollPane = createScrollPane(outputPane);
        JScrollPane errorScrollPane = createScrollPane(errorPane);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputScrollPane, errorScrollPane);
        rightSplitPane.setDividerLocation(300);
        rightSplitPane.setContinuousLayout(true);
        rightSplitPane.setBorder(null);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editScrollPane, rightSplitPane);
        mainSplitPane.setDividerLocation(500);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(null);

        contentPane.add(mainSplitPane, BorderLayout.CENTER);

        setupDocumentListener();
        setupErrorPane();
    }

    private void setupTextPane(JTextPane pane, boolean editable) {
        pane.setEditable(editable);
        pane.setBackground(EDITOR_BG);
        pane.setForeground(TEXT_COLOR);
        pane.setCaretColor(TEXT_COLOR);
        pane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        pane.setMargin(new Insets(10, 10, 10, 10));
    }

    private JScrollPane createScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(BG_COLOR, 1));
        return scrollPane;
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private void runScript() {
        String script = editPane.getText();
        statusLabel.setText("RUNNING...");
        statusLabel.setBackground(ACCENT_COLOR);
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
                                StyleConstants.setForeground(defaultAttr, TEXT_COLOR);
                                doc.setCharacterAttributes(0, fullText.length(), defaultAttr, true);

                                SimpleAttributeSet linkAttr = new SimpleAttributeSet();
                                StyleConstants.setForeground(linkAttr, new Color(88, 157, 246));
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
                statusLabel.setText(scriptExecutor.getStatusMessage(result).toUpperCase());
                if (result.getExitCode() == 0) {
                    statusLabel.setBackground(new Color(60, 140, 60)); // Success green
                } else {
                    statusLabel.setBackground(new Color(180, 50, 50)); // Fail red
                }
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
                StyleConstants.setForeground(defaultAttr, TEXT_COLOR);
                StyleConstants.setBold(defaultAttr, false);
                doc.setCharacterAttributes(lineStart, Math.max(0, lineEnd - lineStart), defaultAttr, true);

                // Apply keyword style
                SimpleAttributeSet keywordAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(keywordAttr, KEYWORD_COLOR);
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
