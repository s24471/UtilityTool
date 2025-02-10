package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

public class RemoveSilent extends JFrame {
    private JTextField directoryPathField;
    private JTextArea logArea;
    private JButton startButton;
    private JProgressBar progressBar;
    private ExecutorService executor;

    public RemoveSilent() {
        setTitle("Analyze Audio Files");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        directoryPathField = ThemeUtil.createStyledTextField("",20);
        logArea = ThemeUtil.createStyledTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);
        startButton = ThemeUtil.createStyledButton("Start Analysis");
        progressBar = ThemeUtil.createStyledProgressBar();

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(ThemeUtil.createStyledLabel("Directory:"));
        panel.add(directoryPathField);
        panel.add(startButton);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcess);

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private void startProcess(ActionEvent e) {
        File dir = new File(directoryPathField.getText());
        File[] files = dir.listFiles((d, name) -> name.endsWith(".mp3") || name.endsWith(".wav"));
        if (files != null && files.length > 0) {
            progressBar.setMaximum(files.length);
            progressBar.setValue(0);
            for (File file : files) {
                executor.submit(() -> analyzeAudio(file));
            }
        } else {
            log("No MP3 or WAV files found in the directory.");
        }
        executor.shutdown();
    }

    private void analyzeAudio(File file) {
        String command = String.format("ffmpeg -i \"%s\" -af astats=reset=1 -f null -", file.getAbsolutePath());
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            List<Double> rmsLevels = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.contains("RMS level dB")) {
                    String rmsValue = line.substring(line.indexOf("RMS level dB") + 13).trim();
                    if (!rmsValue.equals("inf")) {
                        rmsLevels.add(Double.parseDouble(rmsValue));
                    }
                }
            }
            process.waitFor();
            if (rmsLevels.isEmpty() || isAudioFileSilentOrStatic(rmsLevels)) {
                if (file.delete()) {
                    log("Deleted: " + file.getName() + " (Silent or Static Noise)");
                } else {
                    log("Failed to delete file: " + file.getName());
                }
            } else {
                log("File is not silent and has varying noise levels: " + file.getName());
            }
        } catch (IOException | InterruptedException ex) {
            log("Error processing file " + file.getName() + ": " + ex.getMessage());
        }
        progressBar.setValue(progressBar.getValue() + 1);
    }

    private boolean isAudioFileSilentOrStatic(List<Double> rmsLevels) {
        double sum = 0.0;
        for (Double level : rmsLevels) {
            sum += level;
        }
        double average = sum / rmsLevels.size();
        double variance = 0.0;
        for (Double level : rmsLevels) {
            variance += Math.pow(level - average, 2);
        }
        double standardDeviation = Math.sqrt(variance / rmsLevels.size());
        return standardDeviation < 3;
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RemoveSilent::new);
    }
}
