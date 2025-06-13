package sc;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton toRegisterButton;
    private JButton adminLoginButton;

    public LoginPanel(GymManagementGUI mainFrame, GymManagementSystem system) {
        setLayout(null);

        // 背景圖片
        JLabel background = new JLabel(new ImageIcon("login.jpg"));
        background.setBounds(0, 0, 800, 600);
        add(background);
        background.setLayout(null);

        JLabel titleLabel = new JLabel("會員登入", JLabel.CENTER);
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 30));
        titleLabel.setBounds(0, 50, 800, 40);  // ⭐ 完整置中
        background.add(titleLabel);

        JLabel emailLabel = new JLabel("Email：");
        emailLabel.setBounds(280, 150, 60, 25); // 調整對齊
        background.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(350, 150, 200, 25);
        emailField.setHorizontalAlignment(JTextField.CENTER);
        background.add(emailField);

        JLabel passwordLabel = new JLabel("密碼：");
        passwordLabel.setBounds(280, 200, 60, 25);
        background.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(350, 200, 200, 25);
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        background.add(passwordField);

        loginButton = new JButton("登入");
        loginButton.setBounds(300, 260, 100, 30);
        background.add(loginButton);

        toRegisterButton = new JButton("註冊");
        toRegisterButton.setBounds(420, 260, 100, 30);
        background.add(toRegisterButton);

        adminLoginButton = new JButton("管理員登入");
        adminLoginButton.setBounds(340, 320, 140, 30);
        background.add(adminLoginButton);
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getToRegisterButton() {
        return toRegisterButton;
    }

    public JButton getAdminLoginButton() {
        return adminLoginButton;
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getUsername() {
        return getEmail(); // 保持相容性
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
    }
}
