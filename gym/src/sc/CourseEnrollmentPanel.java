package sc;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class CourseEnrollmentPanel extends JPanel {
    private final GymManagementSystem system;

    private final JComboBox<Course> courseCombo = new JComboBox<>();

    private final DefaultListModel<CourseSchedule> availableModel = new DefaultListModel<>();
    private final JList<CourseSchedule> availableList = new JList<>(availableModel);

    private final DefaultListModel<CourseSchedule> enrolledModel = new DefaultListModel<>();
    private final JList<CourseSchedule> enrolledList = new JList<>(enrolledModel);

    private final JButton enrollButton = new JButton("報名所選場次");
    private final JButton backButton = new JButton("返回");

    private final PaymentPanel paymentPanel;
    private final Runnable switchToPayment;
    private final Runnable switchBackToEnroll;

    public CourseEnrollmentPanel(GymManagementSystem system, PaymentPanel paymentPanel, Runnable switchToPayment, Runnable switchBackToEnroll) {
        this.system = system;
        this.paymentPanel = paymentPanel;
        this.switchToPayment = switchToPayment;
        this.switchBackToEnroll = switchBackToEnroll;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 上方：選課
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("選擇課程："));
        top.add(courseCombo);
        add(top, BorderLayout.NORTH);

        // 中間：左右兩個清單
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 20));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("已報名場次"));
        enrolledList.setVisibleRowCount(10);
        leftPanel.add(new JScrollPane(enrolledList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("可報名場次"));
        availableList.setVisibleRowCount(10);
        rightPanel.add(new JScrollPane(availableList), BorderLayout.CENTER);

        center.add(leftPanel);
        center.add(rightPanel);
        add(center, BorderLayout.CENTER);

        // 下方按鈕
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(enrollButton);
        south.add(backButton);
        add(south, BorderLayout.SOUTH);

        bindEvents();
    }

    private void bindEvents() {
        // 當選擇課程時，自動顯示該課程本月可報名場次與已報名場次
        courseCombo.addActionListener(e -> refreshSchedules());

        enrollButton.addActionListener(e -> {
            CourseSchedule sel = availableList.getSelectedValue();
            if (sel == null) return;  // 不顯示警告
            boolean success = system.enrollCourseSchedule(sel);
            if (success) {
                paymentPanel.setAmount(system.getCourseFee(getSelectedCourse()));
                refreshSchedules(); // 自動刷新就好
                switchToPayment.run();   // 切換畫面
            }
        });
        
     // === 支援付款成功後跳回本頁 ===
        paymentPanel.setOnPaymentSuccess(() -> {
            refreshSchedules();         // 更新報名狀態
            switchBackToEnroll.run();   // ✅ 改為回來
        });

    }
        
    public void refresh() {
        courseCombo.removeAllItems();
        for (Course c : system.getCourses()) {
            courseCombo.addItem(c);
        }
        courseCombo.setSelectedIndex(-1);
        enrolledModel.clear();
        availableModel.clear();
    }

    private void refreshSchedules() {
        Course selected = (Course) courseCombo.getSelectedItem();
        if (selected == null) return;

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<CourseSchedule> all = system.getSchedulesForCourse(selected.getCourseId(), year, month);
        List<CourseSchedule> mine = system.getEnrolledSchedulesForCurrentUser(selected.getCourseId(), year, month);

        enrolledModel.clear();
        for (CourseSchedule cs : mine) enrolledModel.addElement(cs);

        availableModel.clear();
        for (CourseSchedule cs : all) {
            if (!mine.contains(cs)) {
                availableModel.addElement(cs);
            }
        }
    }

    public JButton getBackButton() {
        return backButton;
    }

    private Course getSelectedCourse() {
        return (Course) courseCombo.getSelectedItem();
    }
}
