package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoClipboard extends JFrame implements NativeKeyListener {
    private JTextArea textArea;
    private JButton startButton, closeButton;
    private JCheckBox autoEnterCheckBox;
    private List<String> lines;
    private int currentIndex = 0;
    private boolean isCopied = false;
    private Highlighter highlighter;
    private Object highlightTag;

    public AutoClipboard() {
        createUI();
        setupNativeHook();
    }

    private void createUI() {
        setTitle("Auto Clipboard");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textArea = ThemeUtil.createStyledTextArea(10, 50);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        startButton = ThemeUtil.createStyledButton("Start");
        closeButton = ThemeUtil.createStyledButton("Close");
        autoEnterCheckBox = ThemeUtil.createStyledCheckBox("Auto Enter");

        southPanel.add(startButton);
        southPanel.add(autoEnterCheckBox);
        southPanel.add(closeButton);

        add(southPanel, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcessing);
        closeButton.addActionListener(e -> System.exit(0));

        highlighter = textArea.getHighlighter();
        setVisible(true);
    }

    private void startProcessing(ActionEvent e) {
        lines = new ArrayList<>(List.of(textArea.getText().split("\n")));
        currentIndex = 0;
        isCopied = false;

        if (!lines.isEmpty()) {
            copyNextLine();
        } else {
            JOptionPane.showMessageDialog(this, "Pole tekstowe jest puste!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyNextLine() {
        if (currentIndex < lines.size()) {
            String textToCopy = lines.get(currentIndex);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(textToCopy), null);
            System.out.println("Copied: " + textToCopy);
            isCopied = true;
            highlightCurrentLine();
            currentIndex++;

            if (autoEnterCheckBox.isSelected()) {
                pressEnterKey();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Koniec linii do kopiowania!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void highlightCurrentLine() {
        highlighter.removeAllHighlights();
        if (currentIndex < lines.size()) {
            try {
                int start = textArea.getLineStartOffset(currentIndex);
                int end = textArea.getLineEndOffset(currentIndex);
                highlightTag = highlighter.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            } catch (Exception ignored) {}
        }
    }

    private void pressEnterKey() {
        SwingUtilities.invokeLater(() -> {
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupNativeHook() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF); // Wyłączenie logów JNativeHook
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            JOptionPane.showMessageDialog(this, "Błąd podczas inicjalizacji globalnego nasłuchiwania klawiszy!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_V && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0)) {
            System.out.println("AAAA");
            if (isCopied) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                isCopied = false;
                copyNextLine();
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoClipboard::new);
    }
}
