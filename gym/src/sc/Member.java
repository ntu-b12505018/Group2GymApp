package sc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 會員資料物件 */
public class Member {

    private final String memberId;
    private final String name;
    private final String email;
    private final String password;
    private final Set<Region> regions;            // 使用授權地區

    private final List<Course>      enrolledCourses = new ArrayList<>();
    private final List<ParkingPlan> parkingPlans    = new ArrayList<>();

    /* -------- 建構子 -------- */
    public Member(String memberId, String name,
                  String email, String password,
                  Set<Region> regions) {
        this.memberId = memberId;
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.regions  = regions;
    }

    /* 可以再提供一個「從 DB 字串建構」的建構子 */
    public Member(String memberId, String name,
                  String email, String password,
                  String regionStr) {
        this(memberId, name, email, password, parseRegions(regionStr));
    }

    /* -------- Getter -------- */
    public String getMemberId() { return memberId; }
    public String getName()     { return name;     }
    public String getEmail()    { return email;    }
    public String getPassword() { return password; }
    public Set<Region> getRegions() { return regions; }

    public List<Course>      getEnrolledCourses() { return enrolledCourses; }
    public List<ParkingPlan> getParkingPlans()    { return parkingPlans; }

    /* -------- 業務邏輯 -------- */
    public boolean authenticate(String pwd) { return password.equals(pwd); }

    /** 判斷會員是否能進入指定地區 */
    public boolean canAccessRegion(Region r) {
        // 只要會員選的是 ALL，才能進入所有地區
        if (regions.contains(Region.ALL)) return true;

        // 否則只能進入自己註冊的地區
        return regions.contains(r);
    }


    /* -------- 工具 -------- */
    /** 將 Set<Region> 轉成 "NORTH,SOUTH" 之類字串，用於寫入 DB */
    public static String regionsToString(Set<Region> set) {
        return set.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    /** 將 DB 欄位字串轉回 Set<Region> */
    public static Set<Region> parseRegions(String s) {
        if (s == null || s.isBlank()) return Set.of();
        String[] arr = s.split(",");
        Set<Region> rs = EnumSet.noneOf(Region.class);
        for (String token : arr) {
            rs.add(Region.valueOf(token.trim()));
        }
        return rs;
    }

    @Override
    public String toString() {
        return memberId + "：" + name + " (" +
               (regions.isEmpty()
                  ? "無地區限制"
                  : regionsToString(regions)) + ")";
    }
}
