package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class ListPanel extends JPanel {
    protected JPanel contentPanel;
    protected JScrollPane scrollPane;
    protected Map<String, JPanel> categoryPanels;
    protected Map<String, JPanel> contentPanels;
    protected Set<String> expandedCategories;

    public ListPanel() {
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setPreferredSize(new Dimension(400, 650));


        add(scrollPane, BorderLayout.CENTER);

        categoryPanels = new LinkedHashMap<>();
        contentPanels = new LinkedHashMap<>();
        expandedCategories = new HashSet<>();
        initializeContent();
    }

    protected class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(100, 100, 100);
            this.trackColor = new Color(50, 50, 50);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    protected abstract void initializeContent();

    protected void addCategory(String category, JPanel panel) {
        JPanel categoryPanel = createCategoryPanel(category);
        categoryPanels.put(category, categoryPanel);
        contentPanels.put(category, panel);
    }

    protected void refreshContentPanel() {
        contentPanel.removeAll();

        for (String category : categoryPanels.keySet()) {
            JPanel categoryPanel = categoryPanels.get(category);
            contentPanel.add(categoryPanel);

            if (expandedCategories.contains(category)) {
                JPanel contentPanel = contentPanels.get(category);
                this.contentPanel.add(contentPanel);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createCategoryPanel(String category) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        categoryPanel.setBackground(new Color(30, 30, 30));

        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setForeground(new Color(180, 220, 255));
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        categoryLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        categoryPanel.add(categoryLabel, BorderLayout.CENTER);

        categoryPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                toggleCategory(category);
            }
        });

        return categoryPanel;
    }

    private void toggleCategory(String category) {
        if (expandedCategories.contains(category)) {
            expandedCategories.remove(category);
        } else {
            expandedCategories.add(category);
        }
        refreshContentPanel();
    }
}
