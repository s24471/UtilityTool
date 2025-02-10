package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.ArrayList;

public class CombineAudioFiles extends JFrame {
    private JTextField directoryPathField;
    private JComboBox<String> formatSelector;
    private JTextArea logArea;
    private JButton startButton;
    private JProgressBar progressBar;
    private ExecutorService executor;

    public CombineAudioFiles() {
        setTitle("Combine Audio Files");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        directoryPathField = ThemeUtil.createStyledTextField("", 20);
        logArea = ThemeUtil.createStyledTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);

        String[] formats = {"MP3", "WAV"};
        formatSelector = ThemeUtil.createStyledComboBox(formats);

        startButton = ThemeUtil.createStyledButton("Combine Files");
        progressBar = ThemeUtil.createStyledProgressBar();

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(ThemeUtil.createStyledLabel("Directory:"));
        panel.add(directoryPathField);
        panel.add(ThemeUtil.createStyledLabel("Format:"));
        panel.add(formatSelector);
        panel.add(startButton);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcess);

        executor = Executors.newSingleThreadExecutor();
    }

    private void startProcess(ActionEvent e) {
        String format = (String) formatSelector.getSelectedItem();
        File dir = new File(directoryPathField.getText());
        File[] files = dir.listFiles((d, name) -> name.endsWith("." + format.toLowerCase()));

        if (files == null || files.length == 0) {
            log("No " + format + " files found in the directory.");
            return;
        }

        executor.submit(() -> combineFiles(dir, files, format));
    }

    private void combineFiles(File dir, File[] files, String format) {
        log("Starting to combine files...");

        try {
            File listFile = new File(dir, "filelist.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(listFile))) {
                for (File file : files) {
                    writer.println("file '" + file.getAbsolutePath().replace("\\", "\\\\") + "'");
                }
            }

            String outputFileName = new File(dir, "combined." + format.toLowerCase()).getAbsolutePath();
            String command = String.format("ffmpeg -f concat -safe 0 -i \"%s\" -c copy \"%s\"", listFile.getAbsolutePath(), outputFileName);

            Process process = Runtime.getRuntime().exec(command);
            readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log("Combined files into " + outputFileName);
            } else {
                log("Failed to combine files. See log for details.");
            }

            listFile.delete();
        } catch (IOException | InterruptedException ex) {
            log("Error during combination: " + ex.getMessage());
        }

        progressBar.setValue(progressBar.getMaximum());
    }

    private void readProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log(line);
                }
            } catch (IOException e) {
                log("Error reading process output: " + e.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CombineAudioFiles::new);
    }
}
