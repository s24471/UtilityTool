package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

public class ConvertJpgToPngGUI extends JFrame {
    private JTextArea logArea;
    private JCheckBox deleteAfterConversionCheckbox, chooseOutputFolderCheckbox, recursiveCheckbox;
    private JCheckBox convertJpgCheckbox, convertJpegCheckbox, convertWebpCheckbox, convertPngCheckbox;
    private JComboBox<String> targetFormatComboBox;
    private JButton selectDirectoryButton, startConversionButton;
    private JProgressBar progressBar;
    private File selectedDirectory;
    private File outputDirectory;
    private ExecutorService executor;

    public ConvertJpgToPngGUI() {
        super("Convert Image Format");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 150);
        setLayout(new BorderLayout());

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        selectDirectoryButton = ThemeUtil.createStyledButton("Select Directory");
        startConversionButton = ThemeUtil.createStyledButton("Start Conversion");
        startConversionButton.setEnabled(false);
        deleteAfterConversionCheckbox = ThemeUtil.createStyledCheckBox("Delete original files after conversion");
        chooseOutputFolderCheckbox = ThemeUtil.createStyledCheckBox("Choose output folder");
        recursiveCheckbox = ThemeUtil.createStyledCheckBox("Include Subfolders");

        convertJpgCheckbox = ThemeUtil.createStyledCheckBox("Convert JPG", true);
        convertJpegCheckbox = ThemeUtil.createStyledCheckBox("Convert JPEG", true);
        convertWebpCheckbox = ThemeUtil.createStyledCheckBox("Convert WEBP", true);
        convertPngCheckbox = ThemeUtil.createStyledCheckBox("Convert PNG", true);

        targetFormatComboBox = ThemeUtil.createStyledComboBox(new String[] { "png", "jpg" });

        selectDirectoryButton.addActionListener(this::selectDirectory);
        startConversionButton.addActionListener(this::startConversion);
        chooseOutputFolderCheckbox.addActionListener(this::toggleOutputFolderSelection);

        topPanel.add(selectDirectoryButton);
        topPanel.add(startConversionButton);
        topPanel.add(deleteAfterConversionCheckbox);
        topPanel.add(chooseOutputFolderCheckbox);
        topPanel.add(recursiveCheckbox);
        topPanel.add(ThemeUtil.createStyledLabel("Convert:"));
        topPanel.add(convertJpgCheckbox);
        topPanel.add(convertJpegCheckbox);
        topPanel.add(convertWebpCheckbox);
        topPanel.add(convertPngCheckbox);
        topPanel.add(ThemeUtil.createStyledLabel("To:"));
        topPanel.add(targetFormatComboBox);

        logArea = ThemeUtil.createStyledTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = ThemeUtil.createStyledScrollPane(logArea);

        progressBar = ThemeUtil.createStyledProgressBar();
        progressBar.setStringPainted(true);

        add(topPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
    }

    private void selectDirectory(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            log("Selected directory: " + selectedDirectory.getAbsolutePath());
            startConversionButton.setEnabled(true);
            if (!chooseOutputFolderCheckbox.isSelected()) {
                outputDirectory = selectedDirectory;
            }
        } else {
            log("No directory selected");
        }
    }

    private void startConversion(ActionEvent e) {
        if (chooseOutputFolderCheckbox.isSelected() && outputDirectory == null) {
            JOptionPane.showMessageDialog(this, "Please select an output folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!chooseOutputFolderCheckbox.isSelected()) {
            outputDirectory = selectedDirectory;
        }

        int totalFiles = countFilesToConvert(selectedDirectory);
        progressBar.setMaximum(totalFiles);

        convertImagesInDirectory(selectedDirectory);
    }

    private void toggleOutputFolderSelection(ActionEvent e) {
        if (chooseOutputFolderCheckbox.isSelected()) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Select Output Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                outputDirectory = chooser.getSelectedFile();
                log("Selected output directory: " + outputDirectory.getAbsolutePath());
            }
        } else {
            outputDirectory = selectedDirectory;
        }
    }

    private int countFilesToConvert(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && shouldConvert(file)) {
                    count++;
                } else if (file.isDirectory() && recursiveCheckbox.isSelected()) {
                    count += countFilesToConvert(file); // Recursively count files in subdirectories
                }
            }
        }
        return count;
    }

    private void convertImagesInDirectory(File directory) {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isFile() && shouldConvert(child)) {
                    executor.submit(() -> convertFile(child));
                } else if (child.isDirectory() && recursiveCheckbox.isSelected()) {
                    convertImagesInDirectory(child);
                }
            }
        } else {
            log("Directory does not exist or is not a directory");
        }
    }

    private boolean shouldConvert(File file) {
        String fileName = file.getName().toLowerCase();
        return (convertJpgCheckbox.isSelected() && fileName.endsWith(".jpg")) ||
                (convertJpegCheckbox.isSelected() && fileName.endsWith(".jpeg")) ||
                (convertWebpCheckbox.isSelected() && fileName.endsWith(".webp")) ||
                (convertPngCheckbox.isSelected() && fileName.endsWith(".png"));
    }

    private void convertFile(File child) {
        try {
            BufferedImage image = ImageIO.read(child);
            if (image == null) {
                log("Unsupported file format or corrupt file: " + child.getName());
                updateProgressBar();
                return;
            }

            String targetFormat = (String) targetFormatComboBox.getSelectedItem();

            String outputFileName = child.getName().replaceAll("\\.(jpg|jpeg|webp|png)$", "." + targetFormat);

            File output;
            if (chooseOutputFolderCheckbox.isSelected()) {
                output = new File(outputDirectory, outputFileName);
            } else {
                output = new File(child.getParentFile(), outputFileName);
            }

            ImageIO.write(image, targetFormat, output);
            log("Converted: " + child.getName() + " to " + output.getName());

            if (deleteAfterConversionCheckbox.isSelected()) {
                child.delete();
                log("Deleted: " + child.getName());
            }
            updateProgressBar();
        } catch (IOException e) {
            log("Error processing " + child.getName() + ": " + e.getMessage());
            updateProgressBar();
        }
    }

    private void updateProgressBar() {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progressBar.getValue() + 1));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConvertJpgToPngGUI::new);
    }
}
