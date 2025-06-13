package sc;

import java.time.LocalDateTime;

public class Attendance {
    private final String memberId;
    private final String clubId;
    private final LocalDateTime checkInTime;
    private String clubName;  // ✅ 新增欄位

    public Attendance(String mid, String cid, LocalDateTime t){
        this.memberId    = mid;
        this.clubId      = cid;
        this.checkInTime = t;
    }

    // ✅ 新增 setter/getter
    public void setClubName(String name) {
        this.clubName = name;
    }

    public String getClubName() {
        return clubName;
    }

    public String getMemberId()          { return memberId; }
    public String getClubId()            { return clubId; }
    public LocalDateTime getCheckInTime(){ return checkInTime; }

    @Override
    public String toString() {
        return memberId + " 打卡於 " + (clubName != null ? clubName : clubId) + " / " + checkInTime;
    }
}
