package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class AutoClicker extends JFrame implements NativeKeyListener {
    private JButton toggleButton, setHotkeyButton;
    private JTextField intervalField, randomRangeField;
    private JTextArea logArea;
    private JLabel hotkeyLabel;
    private Robot robot;
    private volatile boolean clicking = false;
    private Thread clickThread;
    private boolean settingHotkey = false;
    private int hotkeyCode = NativeKeyEvent.VC_OPEN_BRACKET;

    public AutoClicker() {
        super("Auto Clicker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        setupComponents();
        registerHooks();

        setVisible(true);
    }

    private void setupComponents() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(4, 2, 5, 5));

        intervalField = ThemeUtil.createStyledTextField("1000", 10);
        randomRangeField = ThemeUtil.createStyledTextField("500", 10);

        settingsPanel.add(ThemeUtil.createStyledLabel("Interval (ms):"));
        settingsPanel.add(intervalField);
        settingsPanel.add(ThemeUtil.createStyledLabel("Random Range (ms):"));
        settingsPanel.add(randomRangeField);

        toggleButton = ThemeUtil.createStyledButton("Start Clicking");
        toggleButton.addActionListener(e -> toggleClicking());
        settingsPanel.add(toggleButton);

        setHotkeyButton = ThemeUtil.createStyledButton("Set Hotkey");
        setHotkeyButton.addActionListener(e -> settingHotkey = true);
        settingsPanel.add(setHotkeyButton);

        hotkeyLabel = ThemeUtil.createStyledLabel("Current Hotkey: [" + NativeKeyEvent.getKeyText(hotkeyCode) + "]");
        settingsPanel.add(hotkeyLabel);

        add(settingsPanel, BorderLayout.NORTH);

        logArea = ThemeUtil.createStyledTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = ThemeUtil.createStyledScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Logs"));
        add(logScrollPane, BorderLayout.CENTER);
    }

    private void registerHooks() {
        try {
            robot = new Robot();
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (AWTException | NativeHookException e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize native hooks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void toggleClicking() {
        clicking = !clicking;
        if (clicking) {
            startClicking();
            toggleButton.setText("Stop Clicking");
            appendLog("Clicking started.");
        } else {
            stopClicking();
            toggleButton.setText("Start Clicking");
            appendLog("Clicking stopped.");
        }
    }

    private void startClicking() {
        clickThread = new Thread(() -> {
            Random rand = new Random();
            int baseInterval = Integer.parseInt(intervalField.getText().trim());
            int randomRange = Integer.parseInt(randomRangeField.getText().trim());

            while (clicking) {
                try {
                    int interval = baseInterval + rand.nextInt(randomRange + 1);
                    Thread.sleep(interval);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    appendLog("Clicked with interval: " + interval + "ms");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        clickThread.start();
    }

    private void stopClicking() {
        clicking = false;
        if (clickThread != null) {
            clickThread.interrupt();
        }
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (settingHotkey) {
            hotkeyCode = e.getKeyCode();
            hotkeyLabel.setText("Current Hotkey: [" + NativeKeyEvent.getKeyText(hotkeyCode) + "]");
            settingHotkey = false;
            appendLog("New hotkey set: " + NativeKeyEvent.getKeyText(hotkeyCode));
            return;
        }
        if (e.getKeyCode() == hotkeyCode) {
            toggleClicking();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoClicker::new);
    }
}
