package sc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 註冊面板：簡約 Nimbus 風格，保持原有功能驗證與資料封裝
 */
public class RegistrationPanel extends JPanel {
    private final GymManagementSystem system;

    private final JTextField nameField    = new JTextField();
    private final JTextField emailField   = new JTextField();
    private final JPasswordField pwdField = new JPasswordField();

    private final JCheckBox northBox = new JCheckBox("北區");
    private final JCheckBox southBox = new JCheckBox("南區");
    private final JCheckBox eastBox  = new JCheckBox("東區");
    private final JCheckBox westBox  = new JCheckBox("西區");

    private final JButton registerButton = new JButton("註冊");
    private final JButton toLoginButton  = new JButton("返回登入");

    private static final Pattern PWD_STRONG = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final Pattern EMAIL_FMT  = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    public RegistrationPanel(GymManagementSystem system) {
        this.system = system;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JPanel formPanel = new JPanel(gbl);
        formPanel.setOpaque(false);

        // 姓名
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("姓名："), gbc);
        gbc.gridx = 1;
        styleTextField(nameField);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email："), gbc);
        gbc.gridx = 1;
        styleTextField(emailField);
        formPanel.add(emailField, gbc);

        // 密碼
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("密碼："), gbc);
        gbc.gridx = 1;
        styleTextField(pwdField);
        formPanel.add(pwdField, gbc);

        // 可用地區
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("可用地區："), gbc);
        gbc.gridx = 1;
        JPanel regionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        regionPanel.setOpaque(false);
        regionPanel.add(northBox);
        regionPanel.add(southBox);
        regionPanel.add(eastBox);
        regionPanel.add(westBox);
        formPanel.add(regionPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        styleButton(registerButton);
        styleButton(toLoginButton);
        btnPanel.add(registerButton);
        btnPanel.add(toLoginButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(UIManager.getFont("TextField.font"));
        tf.setPreferredSize(new Dimension(200, 30));
    }

    private void styleButton(JButton btn) {
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setPreferredSize(new Dimension(100, 30));
    }

    public JButton getRegisterButton() { return registerButton; }
    public JButton getToLoginButton() { return toLoginButton; }

    public Member getMemberData() {
        String id = "M" + String.format("%03d", system.getNextMemberSeq());
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        Set<Region> regions = EnumSet.noneOf(Region.class);
        if (northBox.isSelected()) regions.add(Region.NORTH);
        if (southBox.isSelected()) regions.add(Region.SOUTH);
        if (eastBox.isSelected())  regions.add(Region.EAST);
        if (westBox.isSelected())  regions.add(Region.WEST);
        return new Member(id, name, email, pwd, regions);
    }

    public boolean isInputValid() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) return false;
        if (!EMAIL_FMT.matcher(email).matches()) return false;
        if (!PWD_STRONG.matcher(pwd).matches()) return false;
        Set<Region> regions = EnumSet.noneOf(Region.class);
        if (northBox.isSelected()) regions.add(Region.NORTH);
        if (southBox.isSelected()) regions.add(Region.SOUTH);
        if (eastBox.isSelected())  regions.add(Region.EAST);
        if (westBox.isSelected())  regions.add(Region.WEST);
        return !regions.isEmpty();
    }
}
