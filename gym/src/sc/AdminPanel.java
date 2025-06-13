package sc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminPanel extends JPanel {
    private final GymManagementSystem system;
    private final DefaultTableModel memberModel;
    private final JTable memberTable;
    private final DefaultTableModel clubModel;
    private final JTable clubTable;
    private final DefaultTableModel courseModel;
    private final JTable courseTable;
    private final DefaultTableModel paymentModel;
    private final JTable paymentTable;
    private final DefaultTableModel enrollModel;
    private final JTable enrollTable;
    private final DefaultTableModel attendanceModel;
    private final JTable attendanceTable;
    private final DefaultTableModel parkingRecModel;
    private final JTable parkingRecTable;
    private final JButton logoutButton;
    private final JButton deleteMemberButton;
    private final JButton addCourseButton = new JButton("新增課程");
    private final JButton editCourseButton = new JButton("修改課程");	


    public AdminPanel(GymManagementSystem system) {
    	this.system = system;
        logoutButton = new JButton("登出");
        deleteMemberButton = new JButton("註銷會員");

        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        memberModel = new DefaultTableModel(new String[]{"會員ID","姓名","Email","所屬健身房"}, 0);
        memberTable = new JTable(memberModel);
        configureTable(memberTable);
        tabs.addTab("會員管理", new JScrollPane(memberTable));

        clubModel = new DefaultTableModel(new String[]{"俱樂部ID","名稱","地區"}, 0);
        clubTable = new JTable(clubModel);
        configureTable(clubTable);
        tabs.addTab("俱樂部管理", new JScrollPane(clubTable));

        courseModel = new DefaultTableModel(new String[]{"課程ID","標題","教練","時間"}, 0);
        courseTable = new JTable(courseModel);
        configureTable(courseTable);
        
        
     

        // 建立課程管理專用面板（含按鈕）
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        // 課程按鈕：新增 / 修改
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        south.add(addCourseButton);
        south.add(editCourseButton);
        south.add(deleteMemberButton);
        south.add(logoutButton);
        add(south, BorderLayout.SOUTH);


        // 加入到課程分頁
        tabs.addTab("課程管理", coursePanel);

        // 綁定事件：新增課程
        addCourseButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField titleField = new JTextField();
            JTextField instructorField = new JTextField();
            JTextField scheduleField = new JTextField();
            JTextField feeField = new JTextField(); // 新增價格欄位
            JTextField dateField = new JTextField(); // 新增欄位：輸入多個日期


            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("課程ID:")); panel.add(idField);
            panel.add(new JLabel("標題:")); panel.add(titleField);
            panel.add(new JLabel("教練:")); panel.add(instructorField);
            panel.add(new JLabel("時間:")); panel.add(scheduleField);
            panel.add(new JLabel("價格 ($):")); panel.add(feeField); // 價格輸入
            panel.add(new JLabel("開課日期（逗號分隔）:")); panel.add(dateField);
            

            int result = JOptionPane.showConfirmDialog(this, panel, "新增課程", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
            	
            	double fee = 0;
                try {
                    fee = Double.parseDouble(feeField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "價格格式錯誤，請輸入數字");
                    return;
                }
            	
                Course newCourse = new Course(
                    idField.getText(),
                    titleField.getText(),
                    instructorField.getText(),
                    scheduleField.getText(),
                    fee  // 假設費用為 0，如你之後要加入 fee 輸入欄也可以改
                );

                if (system.addCourse(newCourse)) {
                    // 新增課程成功後，建立場次
                	String[] dates = dateField.getText().split(",");
                	for (String d : dates) {
                	    String dateStr = d.trim();
                	    if (!dateStr.isEmpty()) {
                	        try {
                	            // 這裡會檢查格式是否為 yyyy-MM-dd，若錯誤會跳出錯誤訊息
                	            java.time.LocalDate.parse(dateStr); 
                	            system.insertCourseSchedule(newCourse.getCourseId(), dateStr);
                	        } catch (Exception ex) {
                	            JOptionPane.showMessageDialog(this, "日期格式錯誤（必須為 yyyy-MM-dd）：" + dateStr);
                	        }
                	    }
                	}


                    JOptionPane.showMessageDialog(this, "新增成功");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "新增失敗（可能是 ID 重複）");
                }
                
                
            }
            
            


        });

        // 綁定事件：修改課程
        editCourseButton.addActionListener(e -> {
            int row = courseTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先選取要修改的課程");
                return;
            }

            String courseId = (String) courseModel.getValueAt(row, 0);
            String currentTitle = (String) courseModel.getValueAt(row, 1);
            String currentInstructor = (String) courseModel.getValueAt(row, 2);
            String currentSchedule = (String) courseModel.getValueAt(row, 3);
            
            double currentFee = system.getCourses().stream()
            	    .filter(c -> c.getCourseId().equals(courseId))
            	    .findFirst().map(Course::getFee).orElse(0.0);


            JTextField titleField = new JTextField(currentTitle);
            JTextField instructorField = new JTextField(currentInstructor);
            JTextField scheduleField = new JTextField(currentSchedule);
            JTextField dateField = new JTextField();  // ⬅️ 新增欄位：補加開課場次
            JTextField feeField = new JTextField(String.valueOf(currentFee));
            
            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("標題:")); panel.add(titleField);
            panel.add(new JLabel("教練:")); panel.add(instructorField);
            panel.add(new JLabel("時間:")); panel.add(scheduleField);
            panel.add(new JLabel("價格 ($):")); panel.add(feeField);
            panel.add(new JLabel("新增開課日期（可留空）:")); panel.add(dateField);
          
            
            int result = JOptionPane.showConfirmDialog(this, panel, "修改課程", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
            	double fee;
                try {
                    fee = Double.parseDouble(feeField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "價格格式錯誤，請輸入數字");
                    return;
                }
                
            	boolean success = system.updateCourse(courseId, titleField.getText(), instructorField.getText(), scheduleField.getText(),fee);
                if (success) {
                    // 如果有輸入日期，補加場次
                	String[] dates = dateField.getText().split(",");
                	for (String d : dates) {
                	    String dateStr = d.trim();
                	    if (!dateStr.isEmpty()) {
                	        try {
                	            java.time.LocalDate.parse(dateStr); 
                	            system.insertCourseSchedule(courseId, dateStr);
                	        } catch (Exception ex) {
                	            JOptionPane.showMessageDialog(this, "日期格式錯誤（必須為 yyyy-MM-dd）：" + dateStr);
                	        }
                	    }
                	}



                    JOptionPane.showMessageDialog(this, "修改成功");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失敗");
                }
            }
            

        });



        paymentModel = new DefaultTableModel(new String[]{"會員ID","金額","日期"}, 0);
        paymentTable = new JTable(paymentModel);
        configureTable(paymentTable);
        tabs.addTab("付款紀錄", new JScrollPane(paymentTable));

        enrollModel = new DefaultTableModel(new String[]{"會員ID","姓名","課程ID","課程","時間"}, 0);
        enrollTable = new JTable(enrollModel);
        configureTable(enrollTable);
        tabs.addTab("選課紀錄", new JScrollPane(enrollTable));

        attendanceModel = new DefaultTableModel(new String[]{"會員ID","健身房ID","打卡時間"}, 0);
        attendanceTable = new JTable(attendanceModel);
        configureTable(attendanceTable);
        tabs.addTab("出席紀錄", new JScrollPane(attendanceTable));

        parkingRecModel = new DefaultTableModel(new String[]{"會員ID","姓名","方案ID","方案名稱","價格","購買時間"}, 0);
        parkingRecTable = new JTable(parkingRecModel);
        configureTable(parkingRecTable);
        tabs.addTab("停車紀錄", new JScrollPane(parkingRecTable));

        add(tabs, BorderLayout.CENTER);
        
        refresh();
        
        deleteMemberButton.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先選取要註銷的會員");
                return;
            }
            String memberId = (String) memberModel.getValueAt(row, 0); // 第0欄是會員ID

            int confirm = JOptionPane.showConfirmDialog(this,
                "確定要註銷會員 " + memberId + " 嗎？", "確認註銷", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (system.deleteMember(memberId)) {
                    JOptionPane.showMessageDialog(this, "註銷成功");
                    refresh(); // 重新整理表格
                } else {
                    JOptionPane.showMessageDialog(this, "註銷失敗");
                }
            }
        });


    }

    public void refresh() {
        memberModel.setRowCount(0);
        try (Connection conn = DB.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT member_id, name, email, regions FROM member")) {
            while (rs.next()) {
                String regions = formatRegions(rs.getString("regions"));
                memberModel.addRow(new Object[]{rs.getString("member_id"), rs.getString("name"), rs.getString("email"), regions});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        clubModel.setRowCount(0);
        for (GymClub c : system.getGymClubs()) {
            clubModel.addRow(new Object[]{c.getClubId(), c.getName(), c.getRegion()});
        }

        courseModel.setRowCount(0);
        for (Course c : system.getCourses()) {
            courseModel.addRow(new Object[]{c.getCourseId(), c.getTitle(), c.getInstructor(), c.getSchedule()});
        }

        paymentModel.setRowCount(0);
        for (Payment p : system.getPayments()) {
            paymentModel.addRow(new Object[]{p.getMemberId(), p.getAmount(), p.getPayTimeStr()});
        }

        attendanceModel.setRowCount(0);

     // 建立 clubId → clubName 對應表（固定命名）
     Map<String, String> clubMap = Map.ofEntries(
         Map.entry("N01", "台北信義館"),
         Map.entry("N02", "台北內湖館"),
         Map.entry("N03", "桃園中壢館"),
         Map.entry("N04", "新竹北館"),
         Map.entry("N05", "基隆仁愛館"),

         Map.entry("S01", "台南東區館"),
         Map.entry("S02", "高雄左營館"),
         Map.entry("S03", "高雄鳳山館"),
         Map.entry("S04", "屏東潮州館"),
         Map.entry("S05", "嘉義西區館"),

         Map.entry("E01", "花蓮中正館"),
         Map.entry("E02", "花蓮吉安館"),
         Map.entry("E03", "台東市館"),
         Map.entry("E04", "台東成功館"),
         Map.entry("E05", "宜蘭羅東館"),

         Map.entry("W01", "彰化員林館"),
         Map.entry("W02", "台中西屯館"),
         Map.entry("W03", "台中大平館"),
         Map.entry("W04", "南投草屯館"),
         Map.entry("W05", "雲林斗六館")
     );

     for (Attendance a : system.getAttendanceRecords()) {
         String clubDisplay = clubMap.getOrDefault(a.getClubId(), a.getClubId());
         attendanceModel.addRow(new Object[]{
             a.getMemberId(),
             clubDisplay,
             a.getCheckInTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
         });
     }



        enrollModel.setRowCount(0);
        for (String[] row : system.getAllEnrollments()) {
            enrollModel.addRow(row);
        }

        parkingRecModel.setRowCount(0);
        for (String[] row : system.getAllParkingPurchases()) {
            parkingRecModel.addRow(row);
        }
    }

    public JButton getLogoutButton() {
        return logoutButton;
        
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(table.getRowHeight() + 8);
        table.setFont(UIManager.getFont("Table.font"));
        table.getTableHeader().setFont(UIManager.getFont("TableHeader.font"));
    }

    private static String formatRegions(String s) {
        if (s == null || s.isBlank()) return "—";
        Map<String, String> map = Map.of("NORTH","北區","SOUTH","南區","EAST","東區","WEST","西區","ALL","全區");
        return Arrays.stream(s.split(",")).map(token -> map.getOrDefault(token.trim(), token)).collect(Collectors.joining("、"));
    }


}