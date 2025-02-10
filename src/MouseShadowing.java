package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MouseShadowing extends JFrame implements NativeKeyListener, NativeMouseListener {

    private JButton recordButton;
    private JCheckBox recordKeyboardCheckBox;
    private JButton loadButton;
    private JTextArea logArea;

    private boolean isRecording = false;
    private boolean ignoreNextMouseClick = false;
    private final List<String> recordedLines = new ArrayList<>();

    private static final File PRESET_FOLDER = new File("mouseShadowing");
    private static final int PANIC_KEY = NativeKeyEvent.VC_ESCAPE;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MouseShadowing::new);
    }

    public MouseShadowing() {
        super("Recording Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(null);

        initNativeHook();
        initUI();
        setVisible(true);
    }

    private void initNativeHook() {
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordButton = ThemeUtil.createStyledButton("Start Recording");
        recordKeyboardCheckBox = ThemeUtil.createStyledCheckBox("Record Keyboard");
        loadButton = ThemeUtil.createStyledButton("Load Preset");

        topPanel.add(recordButton);
        topPanel.add(recordKeyboardCheckBox);
        topPanel.add(loadButton);

        logArea = ThemeUtil.createStyledTextArea(10, 45);
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        recordButton.addActionListener(e -> toggleRecording());
        loadButton.addActionListener(e -> loadPreset());
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        logArea.append("Recording started.\n");
        isRecording = true;
        recordedLines.clear();
        recordButton.setText("Stop Recording");
    }

    private void stopRecording() {
        logArea.append("Recording stopped.\n");
        isRecording = false;
        recordButton.setText("Start Recording");

        ignoreNextMouseClick = true;
        new PresetEditorFrame(recordedLines, "");
    }

    private void loadPreset() {
        if (!PRESET_FOLDER.exists()) {
            PRESET_FOLDER.mkdirs();
        }
        File[] presetFiles = PRESET_FOLDER.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (presetFiles == null || presetFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No presets found in folder: " + PRESET_FOLDER.getAbsolutePath());
            return;
        }

        List<String> names = Arrays.stream(presetFiles)
                .map(File::getName)
                .collect(Collectors.toList());
        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "Select a preset:",
                "Load Preset",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names.toArray(),
                null
        );
        if (chosen == null) {
            return;
        }

        File selectedFile = new File(PRESET_FOLDER, chosen);
        List<String> lines = readPresetLines(selectedFile);
        new PresetEditorFrame(lines, chosen.replace(".txt", ""));
    }

    private List<String> readPresetLines(File file) {
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        if (!isRecording) {
            return;
        }
        if (ignoreNextMouseClick) {
            ignoreNextMouseClick = false;
            return;
        }

        recordedLines.add("wait();");

        Point p = MouseInfo.getPointerInfo().getLocation();
        int x = p.x;
        int y = p.y;

        String clickType;
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1:
                clickType = "left";
                break;
            case NativeMouseEvent.BUTTON3:
                clickType = "middle";
                break;
            case NativeMouseEvent.BUTTON2:
                clickType = "right";
                break;
            default:
                clickType = "unknown";
                break;
        }

        String line = String.format("click(%d,%d,%s);", x, y, clickType);
        recordedLines.add(line);

        SwingUtilities.invokeLater(() -> {
            logArea.append("[RECORDED] " + line + "\n");
        });
    }


    @Override public void nativeMousePressed(NativeMouseEvent e) {}
    @Override public void nativeMouseReleased(NativeMouseEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == PANIC_KEY) {
            SwingUtilities.invokeLater(() -> {
                logArea.append("Panic key pressed! Pausing playback.\n");
            });
            for(PresetEditorFrame presetEditorFrame: PresetEditorFrame.presetEditorFrames)
                presetEditorFrame.stopPlayback();

            return;

        }

        if (!isRecording) {
            return;
        }
        if (!recordKeyboardCheckBox.isSelected()) {
            return;
        }

        recordedLines.add("wait();");

        int mods = e.getModifiers();
        StringBuilder sb = new StringBuilder();
        if ((mods & NativeKeyEvent.CTRL_MASK) != 0) {
            sb.append("ctrl+");
        }
        if ((mods & NativeKeyEvent.SHIFT_MASK) != 0) {
            sb.append("shift+");
        }
        if ((mods & NativeKeyEvent.ALT_MASK) != 0) {
            sb.append("alt+");
        }
        if ((mods & NativeKeyEvent.META_MASK) != 0) {
            sb.append("meta+");
        }

        String keyName = NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase(Locale.ROOT);
        sb.append(keyName);

        String line = String.format("press(\"%s\");", sb);
        recordedLines.add(line);

        SwingUtilities.invokeLater(() -> {
            logArea.append("[RECORDED] " + line + "\n");
        });
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    class PresetEditorFrame extends JFrame {

        public static List<PresetEditorFrame> presetEditorFrames = new ArrayList<>();

        private JTextField presetNameField, delayField;
        private JTextArea scriptArea;
        private JCheckBox loopCheckBox;

        private JButton cancelButton, playPauseButton, saveButton;

        private Thread playbackThread;
        private boolean isPlaying = false;
        private int currentLineIndex = 0;

        private final List<String> localLines;

        public PresetEditorFrame(List<String> lines, String suggestedName) {
            super("Preset Editor");
            presetEditorFrames.add(this);
            localLines = new ArrayList<>(lines);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    stopPlayback();
                    presetEditorFrames.remove(this);
                    super.windowClosing(e);
                }
            });
            setSize(600, 450);
            setLocationRelativeTo(MouseShadowing.this);
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(ThemeUtil.createStyledLabel("Preset Name:"));
            presetNameField = ThemeUtil.createStyledTextField(suggestedName, 10);
            topPanel.add(presetNameField);

            topPanel.add(ThemeUtil.createStyledLabel("Delay (ms):"));
            delayField = ThemeUtil.createStyledTextField("500", 5);
            topPanel.add(delayField);

            loopCheckBox = ThemeUtil.createStyledCheckBox("Loop");
            topPanel.add(loopCheckBox);

            scriptArea = ThemeUtil.createStyledTextArea(15, 50);
            scriptArea.setEditable(true);
            JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(scriptArea);

            StringBuilder sb = new StringBuilder();
            for (String s : localLines) {
                sb.append(s).append("\n");
            }
            scriptArea.setText(sb.toString());

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            cancelButton = ThemeUtil.createStyledButton("Cancel");
            playPauseButton = ThemeUtil.createStyledButton("Play");
            saveButton = ThemeUtil.createStyledButton("Save");
            bottomPanel.add(cancelButton);
            bottomPanel.add(playPauseButton);
            bottomPanel.add(saveButton);

            add(topPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

            cancelButton.addActionListener(e -> onCancel());
            playPauseButton.addActionListener(e -> onPlayPause());
            saveButton.addActionListener(e -> onSave());

            setVisible(true);
        }

        private void onCancel() {
            stopPlayback();
            dispose();
        }

        private void onSave() {
            stopPlayback();

            if (!PRESET_FOLDER.exists()) {
                PRESET_FOLDER.mkdirs();
            }
            String name = presetNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preset name cannot be empty.");
                return;
            }

            List<String> lines = new ArrayList<>();
            for (String line : scriptArea.getText().split("\\r?\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }

            File outFile = new File(PRESET_FOLDER, name + ".txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
                for (String line : lines) {
                    pw.println(line);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(this, "Preset saved to " + outFile.getAbsolutePath());
        }

        private void onPlayPause() {
            if (!isPlaying) {
                startPlayback();
            } else {
                pausePlayback();
            }
        }

        private void startPlayback() {
            localLines.clear();
            for (String line : scriptArea.getText().split("\\r?\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    localLines.add(trimmed);
                }
            }
            currentLineIndex = 0;
            isPlaying = true;
            playPauseButton.setText("Pause");
            scriptArea.setEditable(false);

            playbackThread = new Thread(() -> {
                int delay = 500;
                try {
                    delay = Integer.parseInt(delayField.getText().trim());
                } catch (NumberFormatException ignored) {}

                while (true) {
                    if (!isPlaying) {
                        break;
                    }
                    if (currentLineIndex >= localLines.size()) {
                        if (loopCheckBox.isSelected()) {
                            currentLineIndex = 0;
                        } else {
                            break;
                        }
                    }

                    final int lineIdx = currentLineIndex;
                    String line = localLines.get(lineIdx);

                    SwingUtilities.invokeLater(() -> highlightLine(lineIdx));

                    if (line.startsWith("wait(")) {
                        try {
                            if (line.startsWith("wait();")) {
                                Thread.sleep(delay);
                            }else{
                                System.out.println(line);
                                System.out.println(line.split("wait\\("));
                                System.out.println(line.split("wait\\(")[0]);
                                System.out.println(line.split("wait\\(")[1]);
                                System.out.println(line.split("wait\\(")[1].split("\\)"));
                                System.out.println(line.split("wait\\(")[1].split("\\)")[0]);
                                System.out.println(line.split("wait\\(")[1].split("\\)")[1]);
                                Thread.sleep(Integer.parseInt(line.split("wait\\(")[1].split("\\)")[0]));
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else if (line.startsWith("click(")) {
                        int start = line.indexOf('(');
                        int end = line.indexOf(')');
                        if (start != -1 && end > start) {
                            String inside = line.substring(start + 1, end);
                            String[] parts = inside.split(",");
                            if (parts.length == 3) {
                                try {
                                    int x = Integer.parseInt(parts[0].trim());
                                    int y = Integer.parseInt(parts[1].trim());
                                    String clickType = parts[2].trim();

                                    Robot r = new Robot();
                                    r.mouseMove(x, y);

                                    int buttonMask;
                                    System.out.println(clickType.toLowerCase());
                                    switch (clickType.toLowerCase()) {
                                        case "right":
                                            buttonMask = InputEvent.BUTTON3_DOWN_MASK;
                                            break;
                                        case "middle":
                                            buttonMask = InputEvent.BUTTON2_DOWN_MASK;
                                            break;
                                        case "left":
                                        default:
                                            buttonMask = InputEvent.BUTTON1_DOWN_MASK;
                                            break;
                                    }
                                    System.out.println(buttonMask);
                                    r.mousePress(buttonMask);
                                    TimeUnit.MILLISECONDS.sleep(10);
                                    r.mouseRelease(buttonMask);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    else if (line.startsWith("press(")) {
                        int startQ = line.indexOf('"');
                        int endQ = line.lastIndexOf('"');
                        if (startQ != -1 && endQ > startQ) {
                            String combo = line.substring(startQ+1, endQ);
                            String[] keys = combo.split("\\+");
                            List<Integer> pressed = new ArrayList<>();

                            try {
                                Robot r = new Robot();
                                for (String k : keys) {
                                    k = k.trim().toLowerCase(Locale.ROOT);
                                    int code = getKeyCode(k);
                                    if (code != -1) {
                                        r.keyPress(code);
                                        pressed.add(code);
                                    }
                                }
                                Collections.reverse(pressed);
                                for (int c : pressed) {
                                    r.keyRelease(c);
                                }
                            } catch (AWTException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    currentLineIndex++;
                }

                SwingUtilities.invokeLater(() -> {
                    highlightLine(-1);
                    scriptArea.setEditable(true);
                    playPauseButton.setText("Play");
                    isPlaying = false;
                    playbackThread = null;
                });
            });
            playbackThread.start();
        }

        private void pausePlayback() {
            isPlaying = false;
            playPauseButton.setText("Play");
            scriptArea.setEditable(true);
        }

        private void stopPlayback() {
            isPlaying = false;
            if (playbackThread != null && playbackThread.isAlive()) {
                playbackThread.interrupt();
            }
            playbackThread = null;
            currentLineIndex = 0;
            playPauseButton.setText("Play");
            scriptArea.setEditable(true);
        }

        private void highlightLine(int index) {
            scriptArea.getHighlighter().removeAllHighlights();
            if (index < 0 || index >= localLines.size()) {
                return;
            }
            try {
                int start = scriptArea.getLineStartOffset(index);
                int end = scriptArea.getLineEndOffset(index);

                scriptArea.getHighlighter().addHighlight(
                        start,
                        end,
                        new DefaultHighlighter.DefaultHighlightPainter(Color.BLACK)
                );

                scriptArea.setCaretPosition(start);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private int getKeyCode(String key) {
            switch (key) {
                case "ctrl":  return KeyEvent.VK_CONTROL;
                case "shift": return KeyEvent.VK_SHIFT;
                case "alt":   return KeyEvent.VK_ALT;
                case "win":
                case "meta":  return KeyEvent.VK_META;
                case"space": return KeyEvent.VK_SPACE;
                default:
                    if (key.length() == 1) {
                        return KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));
                    }
                    return -1;
            }
        }
    }
}
