package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    public Main() {
        setTitle("Tool Launcher");
        setSize(400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ThemeUtil.applyTheme(this);
        SwingUtilities.updateComponentTreeUI(this);
        ListPanel listPanel = new ListPanel() {
            @Override
            protected void initializeContent() {
                JPanel automations = new JPanel(new GridLayout(0, 1, 10, 10));
                JPanel other = new JPanel(new GridLayout(0, 1, 10, 10));
                JPanel files = new JPanel(new GridLayout(0, 1, 10, 10));
                JPanel audio = new JPanel(new GridLayout(0, 1, 10, 10));
                //automations.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                automations.add(addButton("Auto Clicker", e -> AutoClicker.main(new String[]{})));
                automations.add(addButton("Mouse Shadowing", e -> MouseShadowing.main(new String[]{})));
                automations.add(addButton("Pixel Monitoring", e -> GetPixel.main(new String[]{})));
                automations.add(addButton("Auto Clipboard", e -> AutoClipboard.main(new String[]{})));
                addCategory("Automations", automations);


                other.add(addButton("Pdf Page Combining", e -> CombinePDFGui.main(new String[]{})));
                other.add(addButton("Images format converter", e -> ConvertJpgToPngGUI.main(new String[]{})));
                addCategory("Other", other);

                files.add(addButton("File Unziper", e -> UnzipAndDelete.main(new String[]{})));
                files.add(addButton("File Mover", e -> FileMoverGUI.main(new String[]{})));
                files.add(addButton("File Renamer", e -> RenameFilesGUI.main(new String[]{})));
                files.add(addButton("Rename plex media", e -> RenameMedia.main(new String[]{})));
                files.add(addButton("File Name Shortener", e -> RenameFilesAndFoldersByLengthGUI.main(new String[]{})));
                addCategory("File Managers", files);

                audio.add(addButton("Video audio extractor", e -> ExtractAudioFromVideo.main(new String[]{})));
                audio.add(addButton("Remove silent", e -> RemoveSilent.main(new String[]{})));
                audio.add(addButton("Combine Audio Files", e -> CombineAudioFiles.main(new String[]{})));
                addCategory("Audio", audio);

                refreshContentPanel();
            }
        };




        add(listPanel, BorderLayout.CENTER);
        listPanel.initializeContent();

    }

    private JButton addButton(String label, ActionListener actionListener) {
        JButton button = ThemeUtil.createStyledButton(label);
        button.addActionListener(actionListener);
        return button;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
