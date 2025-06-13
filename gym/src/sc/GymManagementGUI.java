package sc;

import javax.swing.*;
import java.awt.*;

/**
 * 主視窗：卡片式切換，採用內建 Nimbus 風格（無需額外依賴）
 */
public class GymManagementGUI extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final GymManagementSystem system;

    private final RegistrationPanel     regPanel;
    private final LoginPanel            loginPanel;
    private final ClubViewPanel         clubPanel;
    private CourseEnrollmentPanel coursePanel;
    private final ParkingPanel          parkingPanel;
    private final PaymentPanel          paymentPanel;
    private final AdminLoginPanel       adminLoginPanel;
    private final AdminPanel            adminPanel;
    private final ConsentDialog         consentDialog;

    public GymManagementGUI(GymManagementSystem system) {
        super("健身房管理系統");
        this.system = system;

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fallback to default
        }

        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", new Color(240, 240, 240));
        UIManager.put("Button.foreground", Color.DARK_GRAY);
        UIManager.put("Button.font", new Font("微軟正黑體", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("微軟正黑體", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("微軟正黑體", Font.PLAIN, 14));
        UIManager.put("PasswordField.font", new Font("微軟正黑體", Font.PLAIN, 14));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.border", BorderFactory.createLineBorder(new Color(200, 200, 200)));

        regPanel        = new RegistrationPanel(system);
        loginPanel 		= new LoginPanel(this, system);
        clubPanel       = new ClubViewPanel(system);
        paymentPanel    = new PaymentPanel(system);
        
        parkingPanel    = new ParkingPanel(system);
        adminLoginPanel = new AdminLoginPanel(system);
        adminPanel      = new AdminPanel(system);
        consentDialog   = new ConsentDialog(this);
        coursePanel = new CourseEnrollmentPanel(system, paymentPanel,
        	    () -> cardLayout.show(mainPanel, "PAYMENT"),  // ✅ 大寫，與上面 add() 對應
        	    () -> cardLayout.show(mainPanel, "COURSE")
        	);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(regPanel,        "REGISTER");
        mainPanel.add(loginPanel,      "LOGIN");
        mainPanel.add(clubPanel,       "CLUB");
        mainPanel.add(coursePanel,     "COURSE");
        mainPanel.add(parkingPanel,    "PARKING");
        mainPanel.add(paymentPanel,    "PAYMENT");
        mainPanel.add(adminLoginPanel, "ADMIN_LOGIN");
        mainPanel.add(adminPanel,      "ADMIN_HOME");

        bindEvents();

        setContentPane(mainPanel);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        switchTo("REGISTER");
        setVisible(true);
    }

    private void bindEvents() {
        regPanel.getToLoginButton().addActionListener(e -> switchTo("LOGIN"));
        regPanel.getRegisterButton().addActionListener(e -> {
            consentDialog.setModal(true);
            consentDialog.setVisible(true);
            if (!consentDialog.isAccepted()) {
                JOptionPane.showMessageDialog(this, "請先同意使用條款");
                return;
            }
            Member m = regPanel.getMemberData();
            if (system.registerMember(m)) {
                JOptionPane.showMessageDialog(this, "註冊成功，請登入");
                switchTo("LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "註冊失敗，Email 可能已存在");
            }
        });

        loginPanel.getToRegisterButton().addActionListener(e -> switchTo("REGISTER"));
        loginPanel.getLoginButton().addActionListener(e -> {
            String id = loginPanel.getUsername();
            String pwd = loginPanel.getPassword();
            if (system.login(id, pwd)) {
                clubPanel.refresh();
                switchTo("CLUB");
            } else {
                JOptionPane.showMessageDialog(this, "帳號或密碼錯誤");
            }
        });

        clubPanel.getLogoutButton().addActionListener(e -> switchTo("LOGIN"));
        loginPanel.getAdminLoginButton().addActionListener(e -> switchTo("ADMIN_LOGIN"));       
        
        coursePanel.getBackButton().addActionListener(e -> switchTo("CLUB"));

        parkingPanel.getBackButton().addActionListener(e -> switchTo("CLUB"));
        parkingPanel.getPurchaseButton().addActionListener(e -> {
            ParkingPlan p = parkingPanel.getPlan();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "請先選擇方案");
                return;
            }
            if (system.purchaseParking(p)) {
                JOptionPane.showMessageDialog(this, "方案購買成功");
            } else {
                JOptionPane.showMessageDialog(this, "方案購買失敗");
            }
            paymentPanel.setAmount(p.getPrice());
            switchTo("PAYMENT");
        });

        paymentPanel.getBackButton().addActionListener(e -> switchTo("CLUB"));
        paymentPanel.setOnPaymentSuccess(() -> {
            coursePanel.refresh();        // ✅ 付款成功後刷新報名頁資料
            switchTo("COURSE");           // ✅ 回到課程報名頁
        });       

        adminLoginPanel.getLoginButton().addActionListener(e -> {
            String u = adminLoginPanel.getUsername();
            String p = adminLoginPanel.getPassword();
            if (system.loginAdmin(u, p)) {
                adminPanel.refresh();
                switchTo("ADMIN_HOME");
            } else {
                JOptionPane.showMessageDialog(this, "管理員帳號或密碼錯誤");
            }
        });
        adminLoginPanel.getBackButton().addActionListener(e -> switchTo("LOGIN"));
        
        clubPanel.getEnrollCourseButton().addActionListener(e -> {
            coursePanel.refresh();     // 加這行：填資料
            switchTo("COURSE");        // 再切畫面
        });

        clubPanel.getPurchaseParkingButton().addActionListener(e -> switchTo("PARKING"));

        
        adminPanel.getLogoutButton().addActionListener(e -> switchTo("ADMIN_LOGIN"));
        
        clubPanel.getPurchaseParkingButton().addActionListener(e -> {
            parkingPanel.refresh();    // ← 這一行是關鍵
            switchTo("PARKING");
        });
        
        


    }

    private void switchTo(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GymManagementGUI(new GymManagementSystem()));
    }
}
