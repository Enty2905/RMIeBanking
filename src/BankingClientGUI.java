import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class BankingClientGUI extends JFrame {
    private BankingService bankingService;
    private String currentAccount;
    private String fullName;
    private BankingCallback callback;
    
    private JLabel balanceLabel;
    private JTextField amountField;
    private JTextField recipientAccountField;
    private JTextArea transactionArea;
    private JButton refreshHistoryButton;
    
    // Modern Banking Theme Colors
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);      // Blue
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);      // Darker Blue
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);      // Green
    private static final Color WARNING_COLOR = new Color(234, 179, 8);      // Yellow
    private static final Color DANGER_COLOR = new Color(239, 68, 68);       // Red
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);        // Dark
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // Gray
    private static final Color BORDER_COLOR = new Color(226, 232, 240);     // Light Border
    
    public BankingClientGUI(BankingService bankingService, String accountNumber, String fullName) {
        this.bankingService = bankingService;
        this.currentAccount = accountNumber;
        this.fullName = fullName;
        initializeGUI();
        registerCallback();
        loadTransactionHistory();
        updateBalance();
        
        // Hủy đăng ký callback khi đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                unregisterCallback();
                dispose();
            }
        });
    }
    
    /**
     * Inner class implement BankingCallback để nhận thông báo từ server
     */
    private class BankingCallbackImpl extends UnicastRemoteObject implements BankingCallback {
        public BankingCallbackImpl() throws RemoteException {
            super();
        }
        
        @Override
        public void notifyTransferReceived(String fromAccount, double amount, String content, double newBalance) throws RemoteException {
            // Cập nhật UI trên EDT thread
            SwingUtilities.invokeLater(() -> {
                // Hiển thị thông báo popup
                String message = String.format(
                    "Bạn đã nhận được %.2f VNĐ từ tài khoản %s\n" +
                    "Nội dung: %s\n" +
                    "Số dư mới: %.2f VNĐ",
                    amount, fromAccount, content, newBalance
                );
                
                JOptionPane.showMessageDialog(
                    BankingClientGUI.this,
                    message,
                    "Thông báo nhận tiền",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Cập nhật số dư
                balanceLabel.setText(String.format("%.2f VNĐ", newBalance));
                
                // Làm mới lịch sử giao dịch
                loadTransactionHistory();
            });
        }
    }
    
    private void registerCallback() {
        try {
            callback = new BankingCallbackImpl();
            bankingService.registerCallback(currentAccount, callback);
        } catch (RemoteException e) {
            System.err.println("Lỗi đăng ký callback: " + e.getMessage());
        }
    }
    
    private void unregisterCallback() {
        try {
            if (bankingService != null && currentAccount != null) {
                bankingService.unregisterCallback(currentAccount);
            }
        } catch (RemoteException e) {
            System.err.println("Lỗi hủy đăng ký callback: " + e.getMessage());
        }
    }
    
    private void initializeGUI() {
        setTitle("E-Banking - " + fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        
        // Main panel với gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(238, 242, 255), 
                                                           0, getHeight(), new Color(248, 250, 252));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel với Content và History
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        
        // Content panel (giao dịch)
        JPanel contentPanel = createContentPanel();
        centerPanel.add(contentPanel, BorderLayout.NORTH);
        
        // Transaction history panel
        JPanel historyPanel = createHistoryPanel();
        centerPanel.add(historyPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 0, 20, 0),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        // Left side - Logo and Account info
        JPanel leftPanel = new JPanel(new BorderLayout(15, 0));
        leftPanel.setOpaque(false);
        
        // Logo
        JLabel logoLabel = new JLabel("🏦");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        leftPanel.add(logoLabel, BorderLayout.WEST);
        
        // Account info
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel welcomeLabel = new JLabel("Xin chào, " + fullName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(welcomeLabel);
        
        JLabel accountLabel = new JLabel("Số tài khoản: " + currentAccount);
        accountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accountLabel.setForeground(TEXT_SECONDARY);
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(accountLabel);
        
        leftPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Right side - Balance card
        JPanel balanceCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 
                                                           getWidth(), getHeight(), new Color(99, 102, 241));
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        balanceCard.setOpaque(false);
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));
        balanceCard.setBorder(new EmptyBorder(15, 25, 15, 25));
        balanceCard.setPreferredSize(new Dimension(220, 75));
        
        JLabel balanceTitle = new JLabel("Số dư khả dụng");
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        balanceTitle.setForeground(new Color(255, 255, 255, 200));
        balanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        balanceCard.add(balanceTitle);
        balanceCard.add(Box.createVerticalStrut(5));
        
        balanceLabel = new JLabel("0 VNĐ");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        balanceCard.add(balanceLabel);
        
        // Logout button
        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(balanceCard, BorderLayout.CENTER);
        
        JButton logoutButton = createIconButton("🚪", "Đăng xuất");
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn đăng xuất?", "Xác nhận", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                unregisterCallback();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        rightPanel.add(logoutButton, BorderLayout.EAST);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Left side - Input fields
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setPreferredSize(new Dimension(350, 200));
        
        // Title
        JLabel titleLabel = new JLabel("💰 Thực hiện giao dịch");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(titleLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        
        // Amount field
        JLabel amountLabel = new JLabel("Số tiền (VNĐ)");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        amountLabel.setForeground(TEXT_PRIMARY);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(amountLabel);
        inputPanel.add(Box.createVerticalStrut(8));
        
        amountField = createStyledTextField("Nhập số tiền...");
        inputPanel.add(amountField);
        inputPanel.add(Box.createVerticalStrut(18));
        
        // Recipient account field
        JLabel recipientLabel = new JLabel("Tài khoản người nhận (chuyển khoản)");
        recipientLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recipientLabel.setForeground(TEXT_PRIMARY);
        recipientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(recipientLabel);
        inputPanel.add(Box.createVerticalStrut(8));
        
        recipientAccountField = createStyledTextField("Nhập số tài khoản...");
        inputPanel.add(recipientAccountField);
        
        panel.add(inputPanel, BorderLayout.WEST);
        
        // Right side - Action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(45, 20, 10, 0));
        
        JButton queryButton = createActionButton("📊", "Vấn tin", new Color(59, 130, 246));
        queryButton.addActionListener(e -> queryAccount());
        buttonPanel.add(queryButton);
        
        JButton depositButton = createActionButton("💵", "Nạp tiền", SUCCESS_COLOR);
        depositButton.addActionListener(e -> deposit());
        buttonPanel.add(depositButton);
        
        JButton withdrawButton = createActionButton("💸", "Rút tiền", WARNING_COLOR);
        withdrawButton.addActionListener(e -> withdraw());
        buttonPanel.add(withdrawButton);
        
        JButton transferButton = createActionButton("📤", "Chuyển khoản", new Color(139, 92, 246));
        transferButton.addActionListener(e -> transfer());
        buttonPanel.add(transferButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(156, 163, 175));
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2d.drawString(placeholder, 12, 26);
                    g2d.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(300, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY_COLOR);
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                field.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                field.repaint();
            }
        });
        
        return field;
    }
    
    private JButton createActionButton(String icon, String text, Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setLayout(new BoxLayout(button, BoxLayout.Y_AXIS));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 85));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(Color.WHITE);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        button.add(Box.createVerticalGlue());
        button.add(iconLabel);
        button.add(Box.createVerticalStrut(5));
        button.add(textLabel);
        button.add(Box.createVerticalGlue());
        
        Color hoverColor = color.darker();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JButton createIconButton(String icon, String tooltip) {
        JButton button = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        button.setToolTipText(tooltip);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(new Color(241, 245, 249));
        button.setForeground(TEXT_SECONDARY);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(45, 45));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(226, 232, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(241, 245, 249));
            }
        });
        
        return button;
    }
    
    // Custom rounded border class
    private static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Title and refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("📋 Lịch sử giao dịch");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        refreshHistoryButton = createSmallButton("🔄 Làm mới");
        refreshHistoryButton.addActionListener(e -> loadTransactionHistory());
        titlePanel.add(refreshHistoryButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Transaction area với modern styling
        transactionArea = new JTextArea(10, 50);
        transactionArea.setEditable(false);
        transactionArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        transactionArea.setBackground(new Color(248, 250, 252));
        transactionArea.setForeground(TEXT_PRIMARY);
        transactionArea.setLineWrap(true);
        transactionArea.setWrapStyleWord(true);
        transactionArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(transactionArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Custom scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(new Color(241, 245, 249));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 32));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(226, 232, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(241, 245, 249));
            }
        });
        
        return button;
    }
    
    // Modern ScrollBar UI
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(248, 250, 252);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y + 2, 
                                                 thumbBounds.width - 4, thumbBounds.height - 4, 6, 6));
            g2d.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
    
    private void updateBalance() {
        try {
            String result = bankingService.queryAccount(currentAccount);
            if (result.contains("Số dư hiện tại:")) {
                String balanceStr = result.replace("Số dư hiện tại:", "").trim();
                balanceLabel.setText(balanceStr + " VNĐ");
            }
        } catch (RemoteException e) {
            balanceLabel.setText("Lỗi");
        }
    }
    
    private void loadTransactionHistory() {
        try {
            List<Transaction> transactions = bankingService.getTransactionHistory(currentAccount);
            transactionArea.setText("");
            if (transactions.isEmpty()) {
                transactionArea.setText("Chưa có giao dịch nào.");
            } else {
                for (Transaction t : transactions) {
                    transactionArea.append(t.toString() + "\n");
                }
            }
        } catch (RemoteException e) {
            transactionArea.setText("Lỗi tải lịch sử: " + e.getMessage());
        }
    }
    
    private void queryAccount() {
        try {
            String result = bankingService.queryAccount(currentAccount);
            updateBalance();
            JOptionPane.showMessageDialog(this, result, "Vấn tin tài khoản", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deposit() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String result = bankingService.deposit(currentAccount, amount);
            updateBalance();
            loadTransactionHistory();
            amountField.setText("");
            JOptionPane.showMessageDialog(this, result, "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void withdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String result = bankingService.withdraw(currentAccount, amount);
            if (result.contains("Số dư không đủ")) {
                JOptionPane.showMessageDialog(this, result, "Lỗi", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                updateBalance();
                loadTransactionHistory();
                amountField.setText("");
                JOptionPane.showMessageDialog(this, result, "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void transfer() {
        String toAccount = recipientAccountField.getText().trim();
        if (toAccount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tài khoản nhận", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (toAccount.equals(currentAccount)) {
            JOptionPane.showMessageDialog(this, "Không thể chuyển cho chính mình", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String content = JOptionPane.showInputDialog(this, 
                "Nội dung chuyển khoản:", "Trả tiền");
            if (content == null) {
                return; // User cancelled
            }
            if (content.trim().isEmpty()) {
                content = "Chuyển khoản";
            }
            
            String result = bankingService.transfer(currentAccount, toAccount, amount, content);
            if (result.contains("Số dư không đủ") || result.contains("Không tìm thấy")) {
                JOptionPane.showMessageDialog(this, result, "Lỗi", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                // Cập nhật số dư và lịch sử
                updateBalance();
                loadTransactionHistory();
                amountField.setText("");
                recipientAccountField.setText("");
                JOptionPane.showMessageDialog(this, result, "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
