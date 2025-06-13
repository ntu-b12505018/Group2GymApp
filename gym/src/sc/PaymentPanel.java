package sc;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * 付款面板：簡約 Nimbus 風格，顯示金額並提供付款/返回功能
 */
public class PaymentPanel extends JPanel {
    private final GymManagementSystem system;
    private final JTextField amountField = new JTextField(10);
    private final JButton payButton      = new JButton("付款");
    private final JButton backButton     = new JButton("返回");

    private Runnable onPaymentSuccess; // ✅ 支援付款完成回呼

    public PaymentPanel(GymManagementSystem system) {
        this.system = system;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 金額標籤與欄位
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("金額："), gbc);
        gbc.gridx = 1;
        styleTextField(amountField);
        amountField.setEditable(false);
        add(amountField, gbc);

        // 按鈕群組
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        styleButton(payButton);
        styleButton(backButton);
        btnPanel.add(payButton);
        btnPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(btnPanel, gbc);

        // 綁定付款事件（付款成功 → 回呼）
        payButton.addActionListener(e -> {
            Payment pay = getPaymentData();
            if (system.processPayment(pay)) {
                JOptionPane.showMessageDialog(this, "付款成功");
                if (onPaymentSuccess != null) {
                    onPaymentSuccess.run(); // ✅ 執行付款成功後的自定義動作
                }
            } else {
                JOptionPane.showMessageDialog(this, "付款失敗");
            }
        });
    }

    /** 設定付款金額 */
    public void setAmount(double fee) {
        amountField.setText(String.valueOf(fee));
    }

    /** 提供付款資訊 */
    public Payment getPaymentData() {
        double amt = Double.parseDouble(amountField.getText());
        String memberId = system.getCurrentMemberId();
        return new Payment(memberId, amt, LocalDateTime.now());
    }

    /** 設定付款成功後要執行的動作 */
    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    public JButton getPayButton()  { return payButton; }
    public JButton getBackButton() { return backButton; }

    private void styleTextField(JTextField tf) {
        tf.setFont(UIManager.getFont("TextField.font"));
        tf.setPreferredSize(new Dimension(120, 30));
    }

    private void styleButton(JButton btn) {
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setPreferredSize(new Dimension(100, 30));
    }
}
