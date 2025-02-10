package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RenameFilesAndFoldersByLengthGUI extends JFrame {
    private JTextArea logArea;
    private JButton selectFolderButton, startRenamingButton;
    private JTextField maxLengthField;
    private File selectedDirectory;
    private int maxNameLength;

    public RenameFilesAndFoldersByLengthGUI() {
        super("Rename Files and Folders by Length");
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

        maxLengthField = ThemeUtil.createStyledTextField("10", 5);
        JPanel lengthPanel = new JPanel();
        lengthPanel.add(ThemeUtil.createStyledLabel("Max Name Length:"));
        lengthPanel.add(maxLengthField);

        selectFolderButton.addActionListener(this::selectFolder);
        startRenamingButton.addActionListener(this::startRenaming);

        JPanel topPanel = new JPanel();
        topPanel.add(selectFolderButton);
        topPanel.add(lengthPanel);
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
        try {
            maxNameLength = Integer.parseInt(maxLengthField.getText());
            if (maxNameLength <= 0) {
                log("Max length must be a positive integer.");
                return;
            }
        } catch (NumberFormatException ex) {
            log("Invalid number format for max length.");
            return;
        }

        if (selectedDirectory != null) {
            renameFilesAndFoldersInDirectory(selectedDirectory);
        }
    }

    private void renameFilesAndFoldersInDirectory(File folder) {
        File[] files = folder.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    checkAndRenameFileOrFolder(file);
                    renameFilesAndFoldersInDirectory(file);
                } else if (file.isFile()) {
                    checkAndRenameFileOrFolder(file);
                }
            }
        } else {
            log("No files or folders found or selected directory is empty.");
        }

        if (folder.getName().length() > maxNameLength) {
            checkAndRenameFileOrFolder(folder);
        }
    }


    private void checkAndRenameFileOrFolder(File fileOrFolder) {
        String name = fileOrFolder.getName();
        String fileExtension = "";

        if (fileOrFolder.isFile()) {
            fileExtension = getFileExtension(name);
            name = name.substring(0, name.length() - fileExtension.length() - 1);
        }

        if (name.length() > maxNameLength) {
            String truncatedName = name.substring(0, maxNameLength);
            String newName = truncatedName + (fileExtension.isEmpty() ? "" : "." + fileExtension);
            File newFileOrFolder = new File(fileOrFolder.getParent(), newName);

            int count = 1;
            while (newFileOrFolder.exists()) {
                newFileOrFolder = new File(fileOrFolder.getParent(), truncatedName + "_" + count + (fileExtension.isEmpty() ? "" : "." + fileExtension));
                count++;
            }


            if (fileOrFolder.renameTo(newFileOrFolder)) {
                log("Renamed " + fileOrFolder.getName() + " to " + newFileOrFolder.getName());
            }else{
                log("Failed to rename " + fileOrFolder.getName() + " after multiple attempts. Check permissions or if file is open.");
            }
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
        SwingUtilities.invokeLater(RenameFilesAndFoldersByLengthGUI::new);
    }
}
