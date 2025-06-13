package sc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {
    private final String memberId;
    private final double amount;
    private final LocalDateTime payTime;

    private static final DateTimeFormatter F =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Payment(String memberId, double amount, LocalDateTime time) {
        this.memberId = memberId;
        this.amount   = amount;
        this.payTime  = time;
    }

    /* ---------- Getter ---------- */
    public String getMemberId()          { return memberId; }
    public double getAmount()            { return amount; }
    public LocalDateTime getPayTime()    { return payTime; }
    /** 給寫入 DB 用 */
    public String getPayTimeStr()        { return F.format(payTime); }

    @Override
    public String toString() {
        return memberId + " 付款 " + amount + " 於 " + getPayTimeStr();
    }
}
