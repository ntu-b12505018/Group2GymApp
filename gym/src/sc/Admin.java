package sc;

public class Admin {
    private String adminId;
    private String name;
    private String password;

    /**
     * 建構子
     * @param adminId 管理員帳號
     * @param name    管理員姓名
     * @param password 登入密碼
     */
    public Admin(String adminId, String name, String password) {
        this.adminId = adminId;
        this.name    = name;
        this.password = password;
    }
    /** 取得管理員帳號 */
    public String getAdminId() {
        return adminId;
    }

    /** 取得管理員姓名 */
    public String getName() {
        return name;
    }

    
    
    public boolean authenticate(String pwd) {
        return this.password.equals(pwd);
    }

    @Override
    public String toString() {
        return "[" + adminId + "] " + name;
    }
}
