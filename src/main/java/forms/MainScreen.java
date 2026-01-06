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

    private final KeywordProcessor keywordProcessor;
    private final ScriptExecutor scriptExecutor;
    private final ErrorLinkProcessor errorLinkProcessor;

    private static final Color BG_COLOR = new Color(43, 43, 43);
    private static final Color EDITOR_BG = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(169, 183, 198);
    private static final Color ACCENT_COLOR = new Color(75, 110, 175);
    private static final Color KEYWORD_COLOR = new Color(204, 120, 50);

    public MainScreen() {
        // Initialize backend processors
        keywordProcessor = new KeywordProcessor(new Keywords());
        scriptExecutor = new ScriptExecutor();
        errorLinkProcessor = new ErrorLinkProcessor();

        // Initialize the main application panel
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BG_COLOR);

        // Configure the script editing area
        editPane = new JTextPane();
        setupTextPane(editPane, true);

        // Configure the standard output display area
        outputPane = new JTextPane();
        setupTextPane(outputPane, false);

        // Configure the compiler error display area
        errorPane = new JTextPane();
        setupTextPane(errorPane, false);

        // Setup the execution status label
        statusLabel = new JLabel("READY");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(ACCENT_COLOR);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Configure the script execution trigger button
        runButton = new JButton("RUN");
        runButton.setBackground(new Color(60, 140, 60)); 
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

        // Create the top toolbar containing status and run controls
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_COLOR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        toolbar.add(statusLabel, BorderLayout.WEST);
        toolbar.add(runButton, BorderLayout.EAST);
        contentPane.add(toolbar, BorderLayout.NORTH);

        // Initialize scroll panes for the editor and output views
        JScrollPane editScrollPane = createScrollPane(editPane);
        JScrollPane outputScrollPane = createScrollPane(outputPane);
        JScrollPane errorScrollPane = createScrollPane(errorPane);

        // Add descriptive labels to the editor and display panels
        JPanel labeledEditPane = createLabeledPane("SCRIPT EDITOR", editScrollPane);
        JPanel labeledOutputPane = createLabeledPane("OUTPUT", outputScrollPane);
        JPanel labeledErrorPane = createLabeledPane("ERRORS", errorScrollPane);

        // Create a vertical split pane for output and error displays
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, labeledOutputPane, labeledErrorPane);
        rightSplitPane.setDividerLocation(300);
        rightSplitPane.setContinuousLayout(true);
        rightSplitPane.setBorder(null);

        // Create a horizontal split pane to separate the editor from output views
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, labeledEditPane, rightSplitPane);
        mainSplitPane.setDividerLocation(1000); 
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(null);

        // Assemble the main layout components
        contentPane.add(mainSplitPane, BorderLayout.CENTER);

        // Configure event listeners for user interactions
        setupDocumentListener();
        setupErrorPane();
    }

    private JPanel createLabeledPane(String title, JComponent component) {
        // Create a panel with a title label at the top
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        JLabel label = new JLabel(title);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void setupTextPane(JTextPane pane, boolean editable) {
        // Configure text pane styling and behavior
        pane.setEditable(editable);
        pane.setBackground(EDITOR_BG);
        pane.setForeground(TEXT_COLOR);
        pane.setCaretColor(TEXT_COLOR);
        pane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        pane.setMargin(new Insets(10, 10, 10, 10));
    }

    private JScrollPane createScrollPane(JComponent component) {
        // Create a scroll pane with a consistent border
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(BG_COLOR, 1));
        return scrollPane;
    }

    public JPanel getContentPane() {
        // Return the main content pane
        return contentPane;
    }

    private void runScript() {
        // Execute the script and handle output and errors
        String script = editPane.getText();
        statusLabel.setText("RUNNING...");
        statusLabel.setBackground(ACCENT_COLOR);
        runButton.setEnabled(false);
        outputPane.setText("");
        errorPane.setText("");

        new Thread(() -> {
            ScriptExecutor.ExecutionResult result = scriptExecutor.execute(
                    script,
                    line -> {
                        // Update output pane with execution results
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
                        // Update error pane with execution errors and links
                        SwingUtilities.invokeLater(() -> {
                            try {
                                StyledDocument doc = errorPane.getStyledDocument();
                                int startPos = doc.getLength();
                                doc.insertString(startPos, text, null);

                                String fullText = doc.getText(0, doc.getLength());
                                List<ErrorLink> links = errorLinkProcessor.findLinks(fullText);

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
                // Update UI based on execution result
                statusLabel.setText(scriptExecutor.getStatusMessage(result).toUpperCase());
                if (result.getExitCode() == 0) {
                    statusLabel.setBackground(new Color(60, 140, 60)); 
                } else {
                    statusLabel.setBackground(new Color(180, 50, 50)); 
                }
                runButton.setEnabled(true);
            });
        }).start();
    }

    private void setupErrorPane() {
        // Handle clicks and mouse movement over error links
        errorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to code location on link click
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
                // Change cursor when hovering over links
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
        // Position caret at specific line and column in editor
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

                    Rectangle viewRect = editPane.modelToView2D(targetOffset).getBounds();
                    editPane.scrollRectToVisible(viewRect);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupDocumentListener() {
        // Listen for editor changes to trigger keyword highlighting
        editPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleUpdate(e); }

            @Override
            public void removeUpdate(DocumentEvent e) { handleUpdate(e); }

            @Override
            public void changedUpdate(DocumentEvent e) { handleUpdate(e); }

            private void handleUpdate(DocumentEvent e) {
                // Determine affected lines and re-highlight keywords
                if (e.getType() == DocumentEvent.EventType.CHANGE) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    try {
                        StyledDocument doc = editPane.getStyledDocument();
                        Element root = doc.getDefaultRootElement();

                        int startOffset = e.getOffset();
                        int changedLength = e.getLength(); 
                        int docLength = doc.getLength();

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
                // Apply syntax highlighting to keywords in a line
                StyledDocument doc = editPane.getStyledDocument();

                SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(defaultAttr, TEXT_COLOR);
                StyleConstants.setBold(defaultAttr, false);
                doc.setCharacterAttributes(lineStart, Math.max(0, lineEnd - lineStart), defaultAttr, true);

                SimpleAttributeSet keywordAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(keywordAttr, KEYWORD_COLOR);
                StyleConstants.setBold(keywordAttr, true);

                for (logic.KeywordMatch match : matches) {
                    int start = match.getStart();
                    int length = match.getLength();
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
