package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileMoverGUI extends JFrame {
    private JTextField sourceField, destinationField, extensionField;
    private JTextArea logArea;
    private JButton moveButton, selectSourceButton, selectDestinationButton;
    private JCheckBox anyFileCheckbox;

    public FileMoverGUI() {
        setTitle("File Mover");
        setSize(600, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 3, 5, 5));

        sourceField = ThemeUtil.createStyledTextField("",20);
        destinationField = ThemeUtil.createStyledTextField("",20);
        extensionField = ThemeUtil.createStyledTextField(".png", 5);
        anyFileCheckbox = ThemeUtil.createStyledCheckBox("Any file", false);

        selectSourceButton = ThemeUtil.createStyledButton("Select Source");
        selectDestinationButton = ThemeUtil.createStyledButton("Select Destination");
        moveButton = ThemeUtil.createStyledButton("Move Files");

        selectSourceButton.addActionListener(this::selectSource);
        selectDestinationButton.addActionListener(this::selectDestination);
        moveButton.addActionListener(this::moveFiles);

        inputPanel.add(ThemeUtil.createStyledLabel("Source Directory:"));
        inputPanel.add(sourceField);
        inputPanel.add(selectSourceButton);

        inputPanel.add(ThemeUtil.createStyledLabel("Destination Directory:"));
        inputPanel.add(destinationField);
        inputPanel.add(selectDestinationButton);

        inputPanel.add(ThemeUtil.createStyledLabel("File Extension:"));
        inputPanel.add(extensionField);
        inputPanel.add(moveButton);

        inputPanel.add(anyFileCheckbox);

        logArea = ThemeUtil.createStyledTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = ThemeUtil.createStyledScrollPane(logArea);

        add(inputPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void selectSource(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            sourceField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void selectDestination(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destinationField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void moveFiles(ActionEvent e) {
        Path sourceDir = Paths.get(sourceField.getText());
        Path destinationDir = Paths.get(destinationField.getText());
        String extension = extensionField.getText().startsWith(".") ? extensionField.getText() : "." + extensionField.getText();

        new Thread(() -> {
            try {
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (anyFileCheckbox.isSelected() || file.toString().endsWith(extension)) {
                            moveFile(file, destinationDir);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                log("Error: " + ex.getMessage());
            }
        }).start();
    }

    private void moveFile(Path file, Path destinationDir) throws IOException {
        Path destinationFile = destinationDir.resolve(file.getFileName());
        Files.move(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        log("Moved: " + file + " -> " + destinationFile);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileMoverGUI::new);
    }
}
