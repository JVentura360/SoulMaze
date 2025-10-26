package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class NameInputDialog extends JDialog {
    private JTextField nameField;
    private String playerName = "";
    private boolean confirmed = false;
    private Image backgroundImage;
    private Font customFont;
    
    public NameInputDialog(JFrame parent) {
        super(parent, "", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true); // Remove title bar
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        // Load custom font
        loadCustomFont();
        
        // Load background image
        loadBackgroundImage();
        
        // Setup dialog
        setupDialog();
        
        // Center on parent
        setLocationRelativeTo(parent);
    }
    
    private void loadCustomFont() {
        try {
            // Load the custom font
            InputStream fontStream = getClass().getResourceAsStream("/assets/Font/belisa_plumilla.ttf");
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                System.out.println("Custom font loaded successfully");
            } else {
                System.out.println("Font file not found, using default font");
                customFont = new Font("Arial", Font.BOLD, 16);
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            customFont = new Font("Arial", Font.BOLD, 16);
        }
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon("src/assets/Images/NameInputDialog.png").getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }
    
    private void setupDialog() {
        setSize(600, 400);
        setLayout(null);
        getRootPane().setBorder(null); // Remove any default borders
        getRootPane().setOpaque(false); // Make root pane transparent
        
        // Create main panel with background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(50, 50, 80));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 600, 400);
        mainPanel.setBorder(null); // Remove panel borders
        mainPanel.setOpaque(false); // Make panel transparent
        add(mainPanel);
        
        // Create title label
        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(customFont.deriveFont(24f));
        titleLabel.setForeground(new Color(255, 165, 0)); // Orange color for title
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(150, 80, 300, 40);
        mainPanel.add(titleLabel);
        
        // Create name input field
        nameField = new JTextField();
        nameField.setFont(customFont.deriveFont(18f));
        nameField.setHorizontalAlignment(SwingConstants.CENTER);
        nameField.setBounds(150, 150, 300, 40);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        // Add placeholder text
        nameField.setText("Enter your name here...");
        nameField.setForeground(Color.GRAY);
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (nameField.getText().equals("Enter your name here...")) {
                    nameField.setText("");
                    nameField.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (nameField.getText().trim().isEmpty()) {
                    nameField.setText("Enter your name here...");
                    nameField.setForeground(Color.GRAY);
                }
            }
        });
        
        mainPanel.add(nameField);
        
        // Create OK button
        JButton okButton = createStyledButton("OKAY", 180, 230, 75, 65);
        okButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && !name.equals("Enter your name here...")) {
                playerName = name;
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a valid name!", "Invalid Name", JOptionPane.WARNING_MESSAGE);
            }
        });
        mainPanel.add(okButton);
        
        // Create Cancel button
        JButton cancelButton = createCancelButton("CANCEL", 360, 230, 75, 65);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        mainPanel.add(cancelButton);
        
        
        // Add Enter key listener to name field
        nameField.addActionListener(e -> okButton.doClick());
        
        // Set focus to name field
        nameField.requestFocusInWindow();
    }
    
    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        // Load and scale button images
        ImageIcon originalIcon = new ImageIcon("src/assets/Images/OkayButton.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        ImageIcon hoverIcon = new ImageIcon("src/assets/Images/OkayButtonHover.png");
        System.out.println("OKAY hover icon loaded: " + (hoverIcon.getImage() != null));
        Image hoverScaled = hoverIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverIcon = new ImageIcon(hoverScaled);
        
        JButton button = new JButton(scaledIcon);
        button.setBounds(x, y, width, height);
        button.setRolloverIcon(scaledHoverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        return button;
    }
    
    private JButton createCancelButton(String text, int x, int y, int width, int height) {
        // Load and scale button images
        ImageIcon originalIcon = new ImageIcon("src/assets/Images/CancelButton.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        ImageIcon hoverIcon = new ImageIcon("src/assets/Images/CancelButtonHover.png");
        System.out.println("CANCEL hover icon loaded: " + (hoverIcon.getImage() != null));
        Image hoverScaled = hoverIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverIcon = new ImageIcon(hoverScaled);
        
        JButton button = new JButton(scaledIcon);
        button.setBounds(x, y, width, height);
        button.setRolloverIcon(scaledHoverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        return button;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
