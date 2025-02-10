package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.*;

public class ExtractAudioFromVideo extends JFrame {
    private JTextField sourceDirField, destinationDirField;
    private JComboBox<String> formatSelector;
    private JTextArea logArea;
    private JButton startButton;
    private JProgressBar progressBar;
    private ExecutorService executor;

    public ExtractAudioFromVideo() {
        setTitle("Extract Audio from Video");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel inputPanel = new JPanel(new FlowLayout());

        sourceDirField = ThemeUtil.createStyledTextField("",20);
        destinationDirField = ThemeUtil.createStyledTextField("",20);

        String[] formats = {"MP3", "WAV"};
        formatSelector = ThemeUtil.createStyledComboBox(formats);

        startButton = ThemeUtil.createStyledButton("Start Extraction");
        logArea = ThemeUtil.createStyledTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);
        progressBar = ThemeUtil.createStyledProgressBar();

        inputPanel.add(ThemeUtil.createStyledLabel("Source Directory:"));
        inputPanel.add(sourceDirField);
        inputPanel.add(ThemeUtil.createStyledLabel("Destination Directory:"));
        inputPanel.add(destinationDirField);
        inputPanel.add(ThemeUtil.createStyledLabel("Format:"));
        inputPanel.add(formatSelector);
        inputPanel.add(startButton);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcessing);

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private void startProcessing(ActionEvent e) {
        String sourceDir = sourceDirField.getText();
        String destinationDir = destinationDirField.getText();
        String format = (String) formatSelector.getSelectedItem();

        File[] files = new File(sourceDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
        if (files != null && files.length > 0) {
            progressBar.setMaximum(files.length);
            progressBar.setValue(0);
            Arrays.stream(files).forEach(file -> executor.submit(() -> extractAudio(file, new File(destinationDir), format)));
        } else {
            log("No MP4 files found in the directory.");
        }
        executor.shutdown();
    }

    private void extractAudio(File sourceFile, File destinationDir, String format) {
        String extension = format.toLowerCase();
        String outputFileName = new File(destinationDir, sourceFile.getName().replaceAll("\\.mp4$", "." + extension)).getAbsolutePath();
        String command = format.equals("MP3") ?
                String.format("ffmpeg -i \"%s\" -map 0:a:0 -acodec libmp3lame -q:a 0 \"%s\"", sourceFile.getAbsolutePath(), outputFileName) :
                String.format("ffmpeg -i \"%s\" -map 0:a:0 -acodec pcm_s16le \"%s\"", sourceFile.getAbsolutePath(), outputFileName);

        executeCommand(command, sourceFile.getName(), outputFileName);
    }

    private void executeCommand(String command, String sourceName, String outputFileName) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            readProcessOutput(process);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log("Extracted audio from " + sourceName + " to " + outputFileName);
            } else {
                log("Failed to extract audio from " + sourceName);
            }
        } catch (IOException | InterruptedException ex) {
            log("Error during extraction: " + ex.getMessage());
        }
        progressBar.setValue(progressBar.getValue() + 1);
    }

    private void readProcessOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log(line);
            }
        } catch (IOException e) {
            log("Error reading process output: " + e.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExtractAudioFromVideo::new);
    }
}
