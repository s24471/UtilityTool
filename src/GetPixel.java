package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GetPixel extends JFrame implements NativeKeyListener {
    private JButton getButton, toggleButton, setPixelHotkeyButton, setButtonHotkeyButton;
    private JLabel pixelHotkeyLabel, buttonHotkeyLabel, statusLabel, colorLabel;
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private Robot robot;
    private volatile boolean active = false;
    private Color targetColor = null;
    private Point pixelPoint = null;
    private final int colorTolerance = 10;
    private int pixelHotkey = NativeKeyEvent.VC_P;
    private int buttonHotkey = NativeKeyEvent.VC_O;
    private int mouseButton = InputEvent.BUTTON3_DOWN_MASK;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Map<Integer, String> mouseButtonMap = new HashMap<>();

    public GetPixel() {
        super("Get Pixel Color");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        mouseButtonMap.put(InputEvent.BUTTON1_DOWN_MASK, "Left Click");
        mouseButtonMap.put(InputEvent.BUTTON2_DOWN_MASK, "Middle Click");
        mouseButtonMap.put(InputEvent.BUTTON3_DOWN_MASK, "Right Click");

        initUI();
        setupNativeHook();
        setVisible(true);
    }

    private void initUI() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        getButton = ThemeUtil.createStyledButton("Set Pixel Target");
        getButton.addActionListener(this::capturePixelTarget);

        toggleButton = ThemeUtil.createStyledButton("Toggle Monitoring");
        toggleButton.setEnabled(false);
        toggleButton.addActionListener(this::toggleMonitoring);

        setPixelHotkeyButton = ThemeUtil.createStyledButton("Set Pixel Hotkey");
        setPixelHotkeyButton.addActionListener(e -> configureHotkey("Pixel", keyCode -> {
            pixelHotkey = keyCode;
            pixelHotkeyLabel.setText("Pixel Hotkey: " + NativeKeyEvent.getKeyText(pixelHotkey));
        }));

        setButtonHotkeyButton = ThemeUtil.createStyledButton("Set Button Hotkey");
        setButtonHotkeyButton.addActionListener(e -> configureHotkey("Button", keyCode -> {
            buttonHotkey = keyCode;
            buttonHotkeyLabel.setText("Button Hotkey: " + NativeKeyEvent.getKeyText(buttonHotkey));
        }));

        pixelHotkeyLabel = ThemeUtil.createStyledLabel("Pixel Hotkey: " + NativeKeyEvent.getKeyText(pixelHotkey));
        buttonHotkeyLabel = ThemeUtil.createStyledLabel("Button Hotkey: " + NativeKeyEvent.getKeyText(buttonHotkey));
        statusLabel = ThemeUtil.createStyledLabel("Ready");
        colorLabel = ThemeUtil.createStyledLabel("No color selected");

        controlPanel.add(getButton);
        controlPanel.add(setPixelHotkeyButton);
        controlPanel.add(toggleButton);
        controlPanel.add(setButtonHotkeyButton);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        infoPanel.add(pixelHotkeyLabel);
        infoPanel.add(buttonHotkeyLabel);
        infoPanel.add(colorLabel);

        logArea = ThemeUtil.createStyledTextArea(10, 40);
        logArea.setEditable(false);
        logScrollPane = ThemeUtil.createStyledScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));

        add(controlPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }

    private void setupNativeHook() {
        try {
            robot = new Robot();
            robot.setAutoDelay(0);
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (AWTException | NativeHookException e) {
            updateStatus("Error setting up native hooks: " + e.getMessage());
            System.exit(1);
        }
    }

    private void capturePixelTarget(ActionEvent e) {
        pixelPoint = MouseInfo.getPointerInfo().getLocation();
        targetColor = robot.getPixelColor(pixelPoint.x, pixelPoint.y);
        colorLabel.setText("Target Color: RGB(" + targetColor.getRed() + ", " +
                targetColor.getGreen() + ", " + targetColor.getBlue() + ")  at (" + pixelPoint.x + ", " + pixelPoint.y + ")");
        updateStatus("Target pixel set at (" + pixelPoint.x + ", " + pixelPoint.y + ")");
        toggleButton.setEnabled(true);
    }

    private void toggleMonitoring(ActionEvent e) {
        if (active) {
            stopMonitoring();
        } else {
            startMonitoring();
        }
    }

    private void startMonitoring() {
        active = true;
        updateStatus("Monitoring started.");
        new Thread(this::monitorPixelColor).start();
    }

    private void stopMonitoring() {
        active = false;
        updateStatus("Monitoring stopped.");
        robot.mouseRelease(mouseButton);
    }

    private void monitorPixelColor() {
        while (active) {
            Color currentColor = robot.getPixelColor(pixelPoint.x, pixelPoint.y);
            if (!isColorMatch(currentColor, targetColor)) {
                robot.mousePress(mouseButton);
                updateStatus("Color change detected! Holding " + mouseButtonMap.get(mouseButton));
            } else {
                robot.mouseRelease(mouseButton);
                updateStatus("Color stable. Releasing " + mouseButtonMap.get(mouseButton));
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean isColorMatch(Color c1, Color c2) {
        return Math.abs(c1.getRed() - c2.getRed()) <= colorTolerance &&
                Math.abs(c1.getGreen() - c2.getGreen()) <= colorTolerance &&
                Math.abs(c1.getBlue() - c2.getBlue()) <= colorTolerance;
    }

    private void configureHotkey(String action, HotkeySetter setter) {
        updateStatus("Press any key to set the hotkey for " + action + "...");
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                setter.setHotkey(e.getKeyCode());
                updateStatus(action + " hotkey set to: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                GlobalScreen.removeNativeKeyListener(this);
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {}

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {}
        });
    }

    private void updateStatus(String message) {
        String timestamp = sdf.format(new Date());
        logArea.append(timestamp + " - " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == pixelHotkey) {
            capturePixelTarget(null);
        } else if (e.getKeyCode() == buttonHotkey) {
            toggleMonitoring(null);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GetPixel::new);
    }

    interface HotkeySetter {
        void setHotkey(int keyCode);
    }
}
