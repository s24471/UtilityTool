package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class RenameFilesGUI extends JFrame {
    private JTextArea logArea;
    private JButton selectFolderButton, startRenamingButton;
    private File selectedDirectory;

    public RenameFilesGUI() {
        super("Rename Files Sequentially");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLayout(new BorderLayout());
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        selectFolderButton = ThemeUtil.createStyledButton("Select Folder");
        startRenamingButton = ThemeUtil.createStyledButton("Start Renaming");
        startRenamingButton.setEnabled(false);

        selectFolderButton.addActionListener(this::selectFolder);
        startRenamingButton.addActionListener(this::startRenaming);

        JPanel topPanel = new JPanel();
        topPanel.add(selectFolderButton);
        topPanel.add(startRenamingButton);

        logArea = ThemeUtil.createStyledTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = ThemeUtil.createStyledScrollPane(logArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void selectFolder(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            log("Selected directory: " + selectedDirectory.getPath());
            startRenamingButton.setEnabled(true);
        }
    }

    private void startRenaming(ActionEvent e) {
        if (selectedDirectory != null) {
            renameFilesInDirectory(selectedDirectory);
        }
    }

    private void renameFilesInDirectory(File folder) {
        File[] files = folder.listFiles();

        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            int id = 1;

            for (File file : files) {
                if (file.isFile()) {
                    String newFileName = String.format("%s%s%s.%s", folder.getPath(), File.separator, id, getFileExtension(file.getName()));
                    File newFile = new File(newFileName);
                    if (file.renameTo(newFile)) {
                        log("Renamed " + file.getName() + " to " + newFile.getName());
                    } else {
                        log("Failed to rename " + file.getName());
                    }
                    id++;
                }
            }
        } else {
            log("No files found or selected directory is empty.");
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RenameFilesGUI::new);
    }
}
