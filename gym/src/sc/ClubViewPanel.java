package sc;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 會員中心面板：整合課程與停車方案功能
 */
public class ClubViewPanel extends JPanel {
    private final GymManagementSystem system;

    private final JButton logoutButton       = new JButton("登出");
    private final JButton checkInButton      = new JButton("打卡");
    private final JComboBox<GymClub> clubBox = new JComboBox<>();

    private final JList<Course> courseList   = new JList<>();
    private final JButton enrollButton       = new JButton("報名課程");

    private final JList<ParkingPlan> parkingList = new JList<>();
    private final JButton purchaseButton         = new JButton("購買停車方案");

    private boolean first = true;

    public ClubViewPanel(GymManagementSystem system) {
        this.system = system;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("選擇健身房："));
        reloadClubs();
        clubBox.setPreferredSize(new Dimension(150, 30));
        clubBox.setFont(UIManager.getFont("ComboBox.font"));
        topPanel.add(clubBox);
        styleButton(checkInButton);
        topPanel.add(checkInButton);

        add(topPanel, BorderLayout.NORTH);

        // 課程區塊
        JPanel coursePanel = new JPanel(new BorderLayout(5, 5));
        coursePanel.setBorder(BorderFactory.createTitledBorder("課程選擇"));
        coursePanel.setOpaque(false);
        courseList.setVisibleRowCount(5);
        coursePanel.add(new JScrollPane(courseList), BorderLayout.CENTER);
        styleButton(enrollButton);
        coursePanel.add(enrollButton, BorderLayout.SOUTH);

        // 停車方案區塊
        JPanel parkingPanel = new JPanel(new BorderLayout(5, 5));
        parkingPanel.setBorder(BorderFactory.createTitledBorder("停車方案"));
        parkingPanel.setOpaque(false);
        parkingList.setVisibleRowCount(5);
        parkingPanel.add(new JScrollPane(parkingList), BorderLayout.CENTER);
        styleButton(purchaseButton);
        parkingPanel.add(purchaseButton, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 10));
        center.setOpaque(false);
        center.add(coursePanel);
        center.add(parkingPanel);

        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        south.setOpaque(false);
        styleButton(logoutButton);
        south.add(logoutButton);
        add(south, BorderLayout.SOUTH);

        bindActions();
    }

    private void bindActions() {
        checkInButton.addActionListener(e -> {
            GymClub sel = (GymClub) clubBox.getSelectedItem();
            if (sel == null) return;
            if (system.checkIn(sel)) {
                JOptionPane.showMessageDialog(this, "打卡成功！");
            } else {
                JOptionPane.showMessageDialog(this,
                    "無權使用該健身房", "拒絕", JOptionPane.WARNING_MESSAGE);
            }
        });

        enrollButton.addActionListener(e -> {
            Course c = courseList.getSelectedValue();
            
            
        });

        purchaseButton.addActionListener(e -> {
            ParkingPlan p = parkingList.getSelectedValue();
            if (p == null) return; // 不提示錯誤
            system.purchaseParking(p); // 嘗試購買，成功與否都不提示
        });

    }

    private void reloadClubs() {
        clubBox.removeAllItems();
        Member current = system.getCurrentUser();
        if (current == null) return;

        for (GymClub g : system.getGymClubs()) {
            if (current.canAccessRegion(g.getRegion())) {
                clubBox.addItem(g);
            }
        }
        clubBox.setSelectedIndex(-1); // 不預設選取任何項目
    }


    public void refresh() {
        reloadClubs();
        List<Course> courses = system.getCourses(); // ← 使用正確函數名稱
        courseList.setListData(courses.toArray(new Course[0]));

        List<ParkingPlan> plans = system.getParkingPlans();
        parkingList.setListData(plans.toArray(new ParkingPlan[0]));

        
    }

    public JButton getEnrollCourseButton()    { return enrollButton; }
    public JButton getPurchaseParkingButton() { return purchaseButton; }
    public JButton getLogoutButton()          { return logoutButton; }
    public JButton getCheckInButton()         { return checkInButton; }
    public JComboBox<GymClub> getClubBox()    { return clubBox; }

    private void styleButton(JButton btn) {
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setPreferredSize(new Dimension(120, 30));
    }
}
