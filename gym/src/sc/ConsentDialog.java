package sc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConsentDialog extends JDialog {
    private boolean agreed = false;
    private JCheckBox agreeBox = new JCheckBox("我已閱讀並同意上述條款");

    public ConsentDialog(Frame owner) {
        super(owner, "會員生物識別資料使用暨風險告知同意書", true);

        String consent =
            "會員生物識別資料使用暨風險告知同意書" +
            "1. 資料蒐集目的與範圍" +
            "   本健身房為提供會員門禁、人臉辨識打卡、教練課程運動表現追蹤等服務，將蒐集、處理及利用下列生物識別資料：臉部影像、指紋/靜脈掃描、身體組成分析（InBody）數據及心率感測數據。資料僅用於會員身份辨識、運動安全監控、課程成效分析及保險理賠佐證等目的。" +
            "2. 法律依據" +
            "   本同意書依《個人資料保護法》及相關法規辦理。" +
            "3. 資料保存期間、區域及方式" +
            "   • 保存期間：自會員同意之日起至退會後五年，或依法令應保存之期間。" +
            "   • 保存區域：本公司主機房與合約雲端伺服器（位於臺灣）。" +
            "   • 保存方式：加密儲存，並採取權限分級及異地備援機制。" +
            "4. 資料利用" +
            "   除提供前述服務外，本公司不會任意將生物識別資料提供予任何第三人，惟下列情形除外：" +
            "   (1) 依法令規定或受司法機關、主管機關命令；" +
            "   (2) 為完成保險理賠、醫療救護或維護公共安全所必須；" +
            "   (3) 經會員書面同意者。" +
            "5. 權益行使" +
            "   會員得依個資法第3條隨時行使以下權利：查詢或請求閱覽、製給複本、補充或更正、停止蒐集、處理或利用及請求刪除。會員可致信客服信箱 service@gym.com 申請辦理。" +
            "6. 風險告知" +
            "   會員應確認所提供資料之真實性與完整性，若因資料不實造成辨識失靈或意外風險，概由會員自行承擔。於使用設備（如心率帶、體脂測量儀）時，會員應遵循教練或工作人員指示，若因操作不當導致身體不適，會員同意自行負責並放棄對本公司之任何請求權。" +
            "7. 同意聲明" +
            "   本人已充分瞭解本同意書之內容，並同意本健身房依上述目的範圍蒐集、處理及利用本人之生物識別資料。" +
            "簽署人：____________________________" +
            "日期：_________年____月____日";

        JTextArea textArea = new JTextArea(consent);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JButton yes = new JButton("同意並繼續");
        JButton no  = new JButton("不同意");

        yes.addActionListener(e -> { agreed = true; dispose(); });
        no.addActionListener(e  -> { dispose(); });

        JPanel south = new JPanel(new BorderLayout());
        south.add(agreeBox, BorderLayout.CENTER);
        JPanel btns = new JPanel(); btns.add(yes); btns.add(no);
        south.add(btns, BorderLayout.SOUTH);

        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        setSize(500, 500);
        setLocationRelativeTo(owner);
    }

    public boolean isAgreed() { return agreed && agreeBox.isSelected(); }
    public boolean isAccepted() { return isAgreed(); }
}