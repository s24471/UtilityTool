package org.example;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CombinePDFGui extends JFrame {
    private JTextArea logArea;
    private JProgressBar progressBar;

    public CombinePDFGui() {
        setTitle("Combine PDF Pages");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton selectButton = ThemeUtil.createStyledButton("Select PDF");
        selectButton.addActionListener(this::selectPDF);
        topPanel.add(selectButton);

        logArea = ThemeUtil.createStyledTextArea(10, 40);
        logArea.setEditable(false);
        JScrollPane logScrollPane = ThemeUtil.createStyledScrollPane(logArea);

        progressBar = ThemeUtil.createStyledProgressBar();
        progressBar.setStringPainted(true);

        add(topPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void selectPDF(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            combinePDFPages(selectedFile);
        }
    }

    private void combinePDFPages(File file) {
        new Thread(() -> {
            try {
                String src = file.getAbsolutePath();
                String dest = file.getParent() + File.separator + removeExtension(file.getName()) + "combined.pdf";

                PdfReader reader = new PdfReader(src);
                int n = reader.getNumberOfPages();

                Rectangle pageSize = reader.getPageSize(1);
                float width = pageSize.getWidth();
                float height = pageSize.getHeight() * n;

                Document document = new Document(new Rectangle(width, height));
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
                document.open();
                PdfContentByte cb = writer.getDirectContent();

                progressBar.setMaximum(n);
                for (int i = 1; i <= n; i++) {
                    PdfImportedPage page = writer.getImportedPage(reader, i);
                    cb.addTemplate(page, 0, height - pageSize.getHeight() * i);
                    progressBar.setValue(i);
                    log("Processed page " + i + " of " + n);
                }

                document.close();
                reader.close();
                log("PDF pages combined successfully into: " + dest);
            } catch (IOException | DocumentException e) {
                log("Error: " + e.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private String removeExtension(String fileName) {
        return fileName.lastIndexOf('.') > 0 ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CombinePDFGui::new);
    }
}
