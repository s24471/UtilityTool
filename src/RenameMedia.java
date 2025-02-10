package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenameMedia extends JFrame {
    private JTextField directoryPathField;
    private JTextArea logArea;
    private JButton startButton, closeButton;
    private JProgressBar progressBar;
    private ExecutorService executor;

    public RenameMedia() {
        createUI();
    }

    private void createUI() {
        setTitle("Rename Media Files and Directories");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        directoryPathField = ThemeUtil.createStyledTextField("E:\\Media\\New", 30);
        northPanel.add(ThemeUtil.createStyledLabel("Directory:"));
        northPanel.add(directoryPathField);

        add(northPanel, BorderLayout.NORTH);

        logArea = ThemeUtil.createStyledTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        progressBar = ThemeUtil.createStyledProgressBar();

        JPanel southPanel = new JPanel();
        startButton = ThemeUtil.createStyledButton("Start Renaming");
        closeButton = ThemeUtil.createStyledButton("Close");
        southPanel.add(startButton);
        southPanel.add(closeButton);
        southPanel.add(progressBar);

        add(southPanel, BorderLayout.SOUTH);

        startButton.addActionListener(this::startProcessing);
        closeButton.addActionListener(e -> System.exit(0));

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        setVisible(true);
    }

    private void startProcessing(ActionEvent e) {
        String dirPath = directoryPathField.getText();
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            appendLog("The specified path is not a directory or does not exist.");
            return;
        }

        File[] directories = dir.listFiles(File::isDirectory);
        Arrays.sort(directories);
        int seasonNumber = 1;
        for (File directory : directories) {
            String seasonFolderName = String.format("S%02d", seasonNumber++);
            File newDir = new File(directory.getParent(), seasonFolderName);
            directory.renameTo(newDir);
            appendLog("Renamed directory to: " + newDir.getName());

            File[] files = newDir.listFiles();
            Arrays.sort(files);
            int episodeNumber = 1;
            for (File file : files) {
                if(file.isDirectory()) {
                    continue;
                }
                String oldName = file.getName();
                String extension = getFileExtension(file.getName());
                String newFileName = String.format(newDir.getName().toLowerCase() + "e%02d.%s", episodeNumber++, extension);
                File newFile = new File(newDir, newFileName);
                file.renameTo(newFile);
                appendLog("Renamed file " + oldName + " to: " + newFile.getName());
            }
        }
        appendLog("Renaming completed.");
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }

    private synchronized void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RenameMedia::new);
    }
}
