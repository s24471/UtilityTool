package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UnzipAndDelete extends JFrame {
    private JTextField directoryPathField;
    private JCheckBox deleteAfterExtractCheckBox, fileWalkerCheckBox;
    private JTextArea logArea;
    private JButton startButton, closeButton;
    private JProgressBar progressBar;
    private ExecutorService executor;

    public UnzipAndDelete() {
        createUI();
    }

    private void createUI() {
        setTitle("Unzip and Delete Tool");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        directoryPathField = ThemeUtil.createStyledTextField("E:\\RGames\\New", 30);
        northPanel.add(ThemeUtil.createStyledLabel("Directory:"));
        northPanel.add(directoryPathField);

        deleteAfterExtractCheckBox = ThemeUtil.createStyledCheckBox("Delete ZIP files after extraction", true);
        fileWalkerCheckBox = ThemeUtil.createStyledCheckBox("File Walker", false);
        northPanel.add(deleteAfterExtractCheckBox);
        northPanel.add(fileWalkerCheckBox);

        add(northPanel, BorderLayout.NORTH);

        logArea = ThemeUtil.createStyledTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        progressBar = ThemeUtil.createStyledProgressBar();

        JPanel southPanel = new JPanel();
        startButton = ThemeUtil.createStyledButton("Start");
        closeButton = ThemeUtil.createStyledButton("Close");
        southPanel.add(startButton);
        southPanel.add(closeButton);

        southPanel.add(progressBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcessing);
        closeButton.addActionListener(e -> System.exit(0));

        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        setVisible(true);
    }

    private void startProcessing(ActionEvent e) {
        String dirPath = directoryPathField.getText();
        boolean deleteAfterExtract = deleteAfterExtractCheckBox.isSelected();
        boolean useFileWalker = fileWalkerCheckBox.isSelected();
        new Thread(() -> processFiles(dirPath, deleteAfterExtract, useFileWalker)).start();
    }

    private void processFiles(String dirPath, boolean deleteAfterExtract, boolean useFileWalker) {
        File dir = new File(dirPath);
        appendLog("Starting processing...");
        if (!dir.exists() || !dir.isDirectory()) {
            appendLog("The specified path is not a directory or does not exist.");
            return;
        }

        List<File> filesToProcess = new ArrayList<>();
        if (useFileWalker) {
            try {
                Files.walkFileTree(Paths.get(dirPath), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().matches(".*\\.(zip|rar|7z|RAR)$")) {
                            filesToProcess.add(file.toFile());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                appendLog("Error walking the file tree: " + e.getMessage());
            }
        } else {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.toString().matches(".*\\.(zip|rar|7z|RAR)$")) {
                        filesToProcess.add(file);
                    }
                }
            }
        }

        progressBar.setMaximum(filesToProcess.size());
        progressBar.setValue(0);

        for (File file : filesToProcess) {
            executor.submit(() -> {
                try {
                    String folderName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    appendLog("Extracting: " + folderName);
                    extractWith7Zip(file, file.getParent() + File.separator + folderName);
                    if (deleteAfterExtract) {
                        Files.delete(file.toPath());
                    }
                    appendLog("Finished: " + folderName);
                    progressBar.setValue(progressBar.getValue() + 1);
                } catch (Exception e) {
                    appendLog("Error processing file: " + file.getName() + "\n" + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            appendLog("Task interrupted");
        }

        appendLog("All tasks completed or stopped.");
    }

    private void extractWith7Zip(File archiveFile, String outputDir) throws IOException, InterruptedException {
        File destDir = new File(outputDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        ProcessBuilder pb = new ProcessBuilder("7z", "x", archiveFile.getAbsolutePath(), "-o" + outputDir, "-mmt");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendLog(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to extract file with 7-Zip: " + archiveFile.getName());
        }
    }

    private synchronized void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UnzipAndDelete::new);
    }
}
