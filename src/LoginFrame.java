import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class LoginFrame extends JFrame {
    private BankingService bankingService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String loggedInAccount;
    private String loggedInFullName;
    
    // Cấu hình server
    private static String SERVER_HOST = "localhost";
    private static int SERVER_PORT = 1099;
    private static String SERVICE_NAME = "BankingService";
    
    // Màu sắc chủ đạo - Modern Banking Theme
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);      // Blue
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);      // Darker Blue
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);        // Dark
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // Gray
    private static final Color BORDER_COLOR = new Color(226, 232, 240);     // Light Border
    
    // Static block để load config
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        Properties props = new Properties();
        try {
            // Thử load từ file config.properties
            FileInputStream fis = new FileInputStream("config.properties");
            props.load(fis);
            fis.close();
            
            SERVER_HOST = props.getProperty("server.host", "localhost");
            SERVER_PORT = Integer.parseInt(props.getProperty("server.port", "1099"));
            SERVICE_NAME = props.getProperty("service.name", "BankingService");
            
            System.out.println("Đã load cấu hình từ config.properties");
            System.out.println("Server: " + SERVER_HOST + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.out.println("Không tìm thấy config.properties, sử dụng cấu hình mặc định (localhost)");
        }
    }
    
    public static String getServerURL() {
        return "rmi://" + SERVER_HOST + ":" + SERVER_PORT + "/" + SERVICE_NAME;
    }
    
    public LoginFrame() {
        initializeGUI();
        connectToServer();
    }
    
    private void initializeGUI() {
        setTitle("E-Banking - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel với gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(238, 242, 255), 
                                                           0, getHeight(), new Color(224, 231, 255));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        // Panel header với logo và tiêu đề
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Logo icon (bank icon using Unicode)
        JLabel logoLabel = new JLabel("🏦");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(12));
        
        JLabel titleLabel = new JLabel("E-BANKING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Ngân hàng số - An toàn & Tiện lợi");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);
        
        // Card panel cho form
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        
        // Card layout cho Login và Register
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Shadow effect
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        
        // Panel đăng nhập
        JPanel loginPanel = createLoginPanel();
        cardPanel.add(loginPanel, "LOGIN");
        
        // Panel đăng ký
        JPanel registerPanel = createRegisterPanel();
        cardPanel.add(registerPanel, "REGISTER");
        
        cardWrapper.add(cardPanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardWrapper, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Hiển thị màn hình đăng nhập đầu tiên
        cardLayout.show(cardPanel, "LOGIN");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Đăng nhập");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JLabel descLabel = new JLabel("Nhập thông tin đăng nhập của bạn");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(25));
        
        // Username
        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        usernameLabel.setForeground(TEXT_PRIMARY);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(8));
        
        usernameField = createStyledTextField("Nhập tên đăng nhập...");
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(18));
        
        // Password
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));
        
        passwordField = createStyledPasswordField("Nhập mật khẩu...");
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(28));
        
        // Login Button (Primary)
        JButton loginButton = createPrimaryButton("Đăng nhập");
        loginButton.addActionListener(e -> performLogin());
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(12));
        
        // Register Link
        JPanel registerLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerLinkPanel.setOpaque(false);
        registerLinkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerLinkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel noAccountLabel = new JLabel("Chưa có tài khoản?");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noAccountLabel.setForeground(TEXT_SECONDARY);
        registerLinkPanel.add(noAccountLabel);
        
        JButton registerLinkButton = createLinkButton("Đăng ký ngay");
        registerLinkButton.addActionListener(e -> cardLayout.show(cardPanel, "REGISTER"));
        registerLinkPanel.add(registerLinkButton);
        
        panel.add(registerLinkPanel);
        
        // Enter key để đăng nhập
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> performLogin());
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Title
        JLabel titleLabel = new JLabel("Đăng ký tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JLabel descLabel = new JLabel("Tạo tài khoản mới để sử dụng dịch vụ");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(22));
        
        // Full Name
        JLabel fullNameLabel = new JLabel("Họ và tên");
        fullNameLabel.setForeground(TEXT_PRIMARY);
        fullNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fullNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fullNameLabel);
        panel.add(Box.createVerticalStrut(8));
        
        fullNameField = createStyledTextField("Nhập họ và tên...");
        panel.add(fullNameField);
        panel.add(Box.createVerticalStrut(15));
        
        // Username
        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        usernameLabel.setForeground(TEXT_PRIMARY);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JTextField regUsernameField = createStyledTextField("Nhập tên đăng nhập...");
        panel.add(regUsernameField);
        panel.add(Box.createVerticalStrut(15));
        
        // Password
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JPasswordField regPasswordField = createStyledPasswordField("Nhập mật khẩu...");
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(25));
        
        // Register Button (Primary)
        JButton registerButton = createPrimaryButton("Tạo tài khoản");
        registerButton.addActionListener(e -> {
            performRegister(regUsernameField.getText(), 
                          new String(regPasswordField.getPassword()),
                          fullNameField.getText());
        });
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(12));
        
        // Back Link
        JPanel backLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        backLinkPanel.setOpaque(false);
        backLinkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        backLinkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel hasAccountLabel = new JLabel("Đã có tài khoản?");
        hasAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hasAccountLabel.setForeground(TEXT_SECONDARY);
        backLinkPanel.add(hasAccountLabel);
        
        JButton backLinkButton = createLinkButton("Đăng nhập");
        backLinkButton.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));
        backLinkPanel.add(backLinkButton);
        
        panel.add(backLinkPanel);
        
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
                    g2d.drawString(placeholder, 12, 24);
                    g2d.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(300, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY_COLOR);
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                field.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                field.repaint();
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(156, 163, 175));
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2d.drawString(placeholder, 12, 24);
                    g2d.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(300, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY_COLOR);
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                field.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                field.repaint();
            }
        });
        
        return field;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(300, 44));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 64, 175));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_HOVER);
            }
        });
        
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(null);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(PRIMARY_COLOR);
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
    
    private void connectToServer() {
        new Thread(() -> {
            final int[] retries = {0};
            String serverURL = getServerURL();
            System.out.println("Đang kết nối đến: " + serverURL);
            
            while (retries[0] < 5) {
                try {
                    bankingService = (BankingService) Naming.lookup(serverURL);
                    System.out.println("Kết nối thành công đến server!");
                    SwingUtilities.invokeLater(() -> {
                        // Connection successful
                    });
                    return;
                } catch (Exception e) {
                    retries[0]++;
                    String errorMsg = e.getMessage();
                    System.out.println("Thử kết nối lần " + retries[0] + "/5... Lỗi: " + errorMsg);
                    
                    if (retries[0] >= 5) {
                        SwingUtilities.invokeLater(() -> {
                            StringBuilder message = new StringBuilder();
                            message.append("❌ KHÔNG THỂ KẾT NỐI ĐẾN SERVER\n\n");
                            message.append("Server URL: ").append(serverURL).append("\n");
                            message.append("Lỗi: ").append(errorMsg).append("\n\n");
                            message.append("📋 HƯỚNG DẪN KHẮC PHỤC:\n\n");
                            message.append("1. Trên máy SERVER (IP: ").append(SERVER_HOST).append("):\n");
                            message.append("   ✓ Chạy: java BankingServer\n");
                            message.append("   ✓ Kiểm tra IP hiển thị trên server có đúng không\n");
                            message.append("   ✓ Đảm bảo port ").append(SERVER_PORT).append(" không bị chặn\n\n");
                            message.append("2. Trên máy CLIENT (máy này):\n");
                            message.append("   ✓ Kiểm tra file config.properties:\n");
                            message.append("     server.host=").append(SERVER_HOST).append("\n");
                            message.append("     server.port=").append(SERVER_PORT).append("\n\n");
                            message.append("3. Kiểm tra Firewall:\n");
                            message.append("   ✓ Windows Firewall: Cho phép port ").append(SERVER_PORT).append("\n");
                            message.append("   ✓ Antivirus: Tắt tạm thời để test\n\n");
                            message.append("4. Kiểm tra mạng:\n");
                            message.append("   ✓ Ping đến ").append(SERVER_HOST).append(": ping ").append(SERVER_HOST).append("\n");
                            message.append("   ✓ Cả 2 máy cùng mạng LAN\n");
                            
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                message.toString(),
                                "Lỗi kết nối - " + retries[0] + " lần thử",
                                JOptionPane.ERROR_MESSAGE);
                        });
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
        }).start();
    }
    
    private void performLogin() {
        if (bankingService == null) {
            JOptionPane.showMessageDialog(this,
                "Chưa kết nối đến server. Vui lòng đợi...",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ thông tin",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String result = bankingService.login(username, password);
            if (result.startsWith("SUCCESS:")) {
                String[] parts = result.split(":");
                loggedInAccount = parts[1];
                loggedInFullName = parts.length > 2 ? parts[2] : "";
                
                // Mở màn hình chính
                SwingUtilities.invokeLater(() -> {
                    this.dispose();
                    new BankingClientGUI(bankingService, loggedInAccount, loggedInFullName).setVisible(true);
                });
            } else {
                String errorMsg = result.replace("ERROR:", "");
                JOptionPane.showMessageDialog(this,
                    errorMsg,
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi kết nối: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performRegister(String username, String password, String fullName) {
        if (bankingService == null) {
            JOptionPane.showMessageDialog(this,
                "Chưa kết nối đến server. Vui lòng đợi...",
                "Lỗi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ thông tin",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String result = bankingService.register(username, password, fullName);
            if (result.startsWith("SUCCESS:")) {
                String accountNumber = result.replace("SUCCESS:", "");
                JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công!\nSố tài khoản của bạn: " + accountNumber,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, "LOGIN");
                usernameField.setText(username);
            } else {
                String errorMsg = result.replace("ERROR:", "");
                JOptionPane.showMessageDialog(this,
                    errorMsg,
                    "Lỗi đăng ký",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi kết nối: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}
