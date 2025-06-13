package sc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * 停車方案面板：簡約 Nimbus 風格，提供方案列表與購買、返回功能
 */
public class ParkingPanel extends JPanel {
    private final GymManagementSystem system;
    private final DefaultTableModel model;
    private final JTable table;
    private final JButton purchaseButton;
    private final JButton backButton;

    public ParkingPanel(GymManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 資料表
        model = new DefaultTableModel(new String[]{"方案ID", "名稱", "價格"}, 0);
        table = new JTable(model);
        configureTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 按鈕
        purchaseButton = new JButton("購買");
        backButton = new JButton("返回");
        styleButton(purchaseButton);
        styleButton(backButton);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        south.setOpaque(false);
        south.add(backButton);
        south.add(purchaseButton);
        add(south, BorderLayout.SOUTH);

        // 綁定事件
        purchaseButton.addActionListener(e -> onPurchase());
    }

    private void onPurchase() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "請先選擇方案");
            return;
        }
        ParkingPlan plan = getPlan();
        if (plan == null) return;
        boolean ok = system.purchaseParking(plan);
        if (ok) {
            
        } else {
            JOptionPane.showMessageDialog(this, "購買失敗");
        }
    }

    /** 取得使用者選擇的方案 */
    public ParkingPlan getPlan() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        String id = model.getValueAt(row, 0).toString();
        String name = model.getValueAt(row, 1).toString();
        double price = Double.parseDouble(model.getValueAt(row, 2).toString());
        return new ParkingPlan(id, name, price);
    }

    /** 載入最新方案 */
    public void refresh() {
        model.setRowCount(0);
        List<ParkingPlan> plans = system.getParkingPlans();
        for (ParkingPlan p : plans) {
            model.addRow(new Object[]{p.getPlanId(), p.getName(), p.getPrice()});
        }
    }

    public JButton getPurchaseButton() { return purchaseButton; }
    public JButton getBackButton()     { return backButton; }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(table.getRowHeight() + 8);
        table.setFont(UIManager.getFont("Table.font"));
        table.getTableHeader().setFont(UIManager.getFont("TableHeader.font"));
    }

    private void styleButton(JButton btn) {
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setPreferredSize(new Dimension(100, 30));
    }
}
