package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ThemeUtil {


    public static void applyTheme(JFrame frame) {


        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("OptionPane.messageForeground", new Color(200, 200, 200));


        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFocusPainted(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBox.setOpaque(false);
        checkBox.setIcon(createCustomCheckBoxIcon(false));
        checkBox.setSelectedIcon(createCustomCheckBoxIcon(true));
        return checkBox;
    }

    public static JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setFocusPainted(false);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBox.setOpaque(false);
        checkBox.setIcon(createCustomCheckBoxIcon(false));
        checkBox.setSelectedIcon(createCustomCheckBoxIcon(true));
        return checkBox;
    }

    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    public static Icon createCustomCheckBoxIcon(boolean selected) {
        int size = 15;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRoundRect(0, 0, size - 1, size - 1, 4, 4);
        g2d.setColor(selected ? new Color(100, 200, 255) : new Color(60, 60, 60));
        g2d.fillRoundRect(3, 3, size - 7, size - 7, 2, 2);
        g2d.dispose();
        return new ImageIcon(image);
    }

    public static JRadioButton createStyledRadioButton(String text) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFocusPainted(false);
        radioButton.setForeground(Color.WHITE);
        radioButton.setFont(new Font("Arial", Font.PLAIN, 12));
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radioButton.setOpaque(false);
        radioButton.setIcon(createCustomRadioIcon(false));
        radioButton.setSelectedIcon(createCustomRadioIcon(true));
        return radioButton;
    }

    public static Icon createCustomRadioIcon(boolean selected) {
        int size = 15;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(selected ? new Color(100, 200, 255) : new Color(120, 120, 120));
        g2d.fillOval(0, 0, size - 1, size - 1);
        g2d.dispose();
        return new ImageIcon(image);
    }

    public static JSlider createStyledSlider(int min, int max, int initial) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setBackground(new Color(30, 30, 30));
        slider.setForeground(Color.WHITE);
        slider.setMajorTickSpacing(1);

        slider.setPaintLabels(false);
        slider.setPaintTicks(false);
        slider.setSnapToTicks(false);
        slider.setFocusable(false);
        slider.setUI(new javax.swing.plaf.basic.BasicSliderUI(slider) {
            @Override
            public void paintThumb(Graphics g) {
                g.setColor(new Color(100, 200, 255));
                g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }

            @Override
            public void paintTrack(Graphics g) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
            }
        });
        return slider;
    }

    public static JSpinner createStyledSpinner(SpinnerModel model) {
        JSpinner spinner = new JSpinner(model);
        JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        textField.setColumns(3);
        textField.setBackground(new Color(40, 40, 40));
        textField.setForeground(Color.WHITE);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));

        spinner.setOpaque(false);
        spinner.setFocusable(false);
        spinner.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton increaseButton = ((BasicArrowButton) spinner.getComponent(0));
        JButton decreaseButton = ((BasicArrowButton) spinner.getComponent(1));

        increaseButton.setBackground(new Color(30, 30, 30));
        increaseButton.setForeground(Color.WHITE);
        decreaseButton.setBackground(new Color(30, 30, 30));
        decreaseButton.setForeground(Color.WHITE);


        return spinner;
    }
    public static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        return label;
    }
    public static JTextField createStyledTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setBackground(new Color(40, 40, 40));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setMargin(new Insets(5, 5, 5, 5));
        return textField;
    }
    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(new Color(40, 40, 40));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setMargin(new Insets(5, 5, 5, 5));
        return textField;
    }
    public static JTextArea createStyledTextArea() {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setBackground(new Color(40, 40, 40));
        jTextArea.setForeground(Color.WHITE);
        jTextArea.setCaretColor(Color.WHITE);
        jTextArea.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        jTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        jTextArea.setMargin(new Insets(5, 5, 5, 5));
        return jTextArea;
    }
    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea jTextArea = new JTextArea(rows, cols);
        jTextArea.setBackground(new Color(40, 40, 40));
        jTextArea.setForeground(Color.WHITE);
        jTextArea.setCaretColor(Color.WHITE);
        jTextArea.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        jTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        jTextArea.setMargin(new Insets(5, 5, 5, 5));
        return jTextArea;
    }
    public static JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(new Color(40, 40, 40));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        return passwordField;
    }

    public static JScrollPane createStyledScrollPane(Component component, int vPolicy, int hPolicy) {
        JScrollPane scrollPane = new JScrollPane(component, vPolicy, hPolicy);
        scrollPane.setBackground(new Color(40, 40, 40));
        scrollPane.setForeground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();

        if (verticalBar != null) {
            verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new Color(100, 100, 100);
                    this.trackColor = new Color(50, 50, 50);
                }
            });
        }

        if (horizontalBar != null) {
            horizontalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new Color(100, 100, 100);
                    this.trackColor = new Color(50, 50, 50);
                }
            });
        }

        return scrollPane;
    }
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(new Color(40, 40, 40));
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        comboBox.setFocusable(false);
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return comboBox;
    }
    public static JProgressBar createStyledProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setBackground(new Color(40, 40, 40));
        progressBar.setForeground(new Color(100, 200, 255));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        progressBar.setStringPainted(true);
        return progressBar;
    }

    public static JScrollPane createStyledScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBackground(new Color(40, 40, 40));
        scrollPane.setForeground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();

        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 100);
                this.trackColor = new Color(50, 50, 50);
            }
        });

        horizontalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 100);
                this.trackColor = new Color(50, 50, 50);
            }
        });

        return scrollPane;
    }


}
