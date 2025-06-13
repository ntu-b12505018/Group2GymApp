package sc;

import javax.swing.*;
import java.awt.*;

/**
 * 管理員登入面板，採用簡約 Nimbus 風格，保持原本邏輯功能
 */
public class AdminLoginPanel extends JPanel {

    private final GymManagementSystem system;
    private final JTextField usernameField   = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton       = new JButton("管理員登入");
    private final JButton backButton        = new JButton("返回登入");

    public AdminLoginPanel(GymManagementSystem system) {
        this.system = system;
        initUI();
    }

    /** 初始化介面元件與佈局 */
    private void initUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // 中央表單區塊
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(80, 200, 200, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;

        // 帳號
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("帳號："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        // 密碼
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("密碼："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        // 按鈕群組
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(backButton);
        btnPanel.add(loginButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
        
        usernameField.setPreferredSize(passwordField.getPreferredSize());
        
        Dimension fieldSize = new Dimension(200, 30);
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);


    }

    // --- Getter methods for event binding ---
    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getBackButton() {
        return backButton;
    }
}
