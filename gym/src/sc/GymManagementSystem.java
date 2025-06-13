package sc;

import java.util.*;
import sc.DB;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;


public class GymManagementSystem {
	private List<Member> members;
	private List<Course> courses;
	private List<Attendance> attendanceRecords;
	private List<Payment> payments;
	private List<GymClub> gymClubs;
	private List<ParkingPlan> parkingPlans;
	private Member currentUser;

	public GymManagementSystem() {
		members = new ArrayList<>();
		courses = new ArrayList<>();
		attendanceRecords = new ArrayList<>();
		payments = new ArrayList<>();
		gymClubs = new ArrayList<>();
		parkingPlans = new ArrayList<>();
		loadSampleData();
	}

	private void loadSampleData() {
		try (Connection c = DB.getConn(); Statement s = c.createStatement()) {

			
			/* ---------- 課程樣本 ---------- */
			ResultSet rc = s.executeQuery("SELECT COUNT(*) FROM course");
			if (rc.next() && rc.getInt(1) == 0) {
				s.execute("""
						  INSERT INTO course (course_id,title,instructor,schedule,fee) VALUES
						    ('C001','瑜珈課程','Instructor A','週一 10:00',500),
						    ('C002','有氧運動','Instructor B','週三 18:00',400);
						""");
			}

			s.execute("""
		            CREATE TABLE IF NOT EXISTS course_schedule (
		                id INTEGER PRIMARY KEY AUTOINCREMENT,
		                course_id TEXT NOT NULL,
		                date TEXT NOT NULL,
		                FOREIGN KEY(course_id) REFERENCES course(course_id)
		            );
		        """);
			
			/* ---------- 健身房樣本 ---------- */
			ResultSet rg = s.executeQuery("SELECT COUNT(*) FROM gym_club");
			if (rg.next() && rg.getInt(1) == 0) {
			    String sql = "INSERT INTO gym_club (club_id,name,region) VALUES (?,?,?)";
			    try (PreparedStatement ps = c.prepareStatement(sql)) {
			        Map<Region, List<String>> names = Map.of(
			            Region.NORTH, List.of("台北信義館", "台北內湖館", "桃園中壢館", "新竹北館", "基隆仁愛館"),
			            Region.SOUTH, List.of("台南東區館", "高雄左營館", "高雄鳳山館", "屏東潮州館", "嘉義西區館"),
			            Region.EAST,  List.of("花蓮中正館", "花蓮吉安館", "台東市館", "台東成功館", "宜蘭羅東館"),
			            Region.WEST,  List.of("彰化員林館", "台中西屯館", "台中大平館", "南投草屯館", "雲林斗六館")
			        );
			        for (Region r : names.keySet()) {
			            List<String> list = names.get(r);
			            for (int i = 0; i < list.size(); i++) {
			                String id = r.toString().charAt(0) + "0" + (i + 1); // N01, S01...
			                String name = list.get(i);
			                ps.setString(1, id);
			                ps.setString(2, name);
			                ps.setString(3, r.toString());
			                ps.addBatch();
			            }
			        }
			        ps.executeBatch();
			    }
			}

			// ---------- 停車方案樣本 ----------
			ResultSet rp = s.executeQuery("SELECT COUNT(*) FROM parking_plan");
			if (rp.next() && rp.getInt(1) == 0) {
			    s.execute("""
			      INSERT INTO parking_plan(plan_id,name,price) VALUES
			        ('M1','月租 A',1000),
			        ('M2','月租 B',1200),
			        ('Q1','季租 A',2700),
			        ('Y1','年租 A',9900);
			    """);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// --------- 為 GUI 提供的方法 ---------
		/**
		 * 會員註冊
		 */
		private int memberSeq = 3; // 已有 M001~M003
		public int getNextMemberSeq() {
		    try (Connection c = DB.getConn();
		         Statement s = c.createStatement();
		         ResultSet rs = s.executeQuery("SELECT MAX(CAST(SUBSTR(member_id, 2) AS INTEGER)) FROM member")) {
		        if (rs.next()) {
		            return rs.getInt(1) + 1;
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return 1; // 如果資料表是空的
		}


		public boolean registerMember(Member m) {
			String sql = """
					  INSERT INTO member(member_id,name,email,password,regions)
					  VALUES (?,?,?,?,?)""";

					try (Connection c = DB.getConn();
					     PreparedStatement ps = c.prepareStatement(sql)) {

					    ps.setString(1, m.getMemberId());
					    ps.setString(2, m.getName().trim());
					    ps.setString(3, m.getEmail().trim().toLowerCase());
					    ps.setString(4, m.getPassword());                // 真專案請做雜湊
					    ps.setString(5, Member.regionsToString(m.getRegions()));  // ★ 寫入
					    ps.executeUpdate();
					    return true;
					}catch (SQLException e) {
					    e.printStackTrace();  // ⬅️ 加上這行！
					    return false;
			}
		}

		/**
		 * 會員登入，並設定當前會員
		 */
		public boolean login(String account, String pwd) {
			String sql = """
					SELECT * FROM member
					WHERE (member_id = ? OR email = ?) AND password = ?""";
			try (Connection c = DB.getConn(); PreparedStatement ps = c.prepareStatement(sql)) {

				ps.setString(1, account);
				ps.setString(2, account);
				ps.setString(3, pwd);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
				    Set<Region> regSet = Member.parseRegions(rs.getString("regions")); // ★ 解析
				    currentUser = new Member(
				        rs.getString("member_id"),
				        rs.getString("name"),
				        rs.getString("email"),
				        rs.getString("password"),
				        regSet
				    );
				    return true;
				}
			} catch (SQLException ignored) {
			}
			return false;
		}

		/**
		 * 取得當前會員 ID，供 GUI 組裝付款資料
		 */
		public String getCurrentMemberId() {
		    return currentUser != null ? currentUser.getMemberId() : null;
		}



		public boolean loginAdmin(String id, String pwd) {
			String sql = "SELECT password FROM admin WHERE admin_id = ?";
			try (Connection c = DB.getConn(); // ← 需要 java.sql.Connection
					PreparedStatement ps = c.prepareStatement(sql)) {

				ps.setString(1, id);
				ResultSet rs = ps.executeQuery(); // ← 需要 java.sql.ResultSet
				return rs.next() && pwd.equals(rs.getString("password"));

			} catch (SQLException e) { // ← 需要 java.sql.SQLException
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * 報名課程
		 */
		public boolean enrollCourse(Course c) {
		    if (currentUser == null || c == null) return false;

		    String sql = """
		        INSERT OR IGNORE INTO enrollment(member_id, course_id, enroll_at)
		        VALUES (?,?,datetime('now','localtime'))""";
		    try (Connection conn = DB.getConn();
		         PreparedStatement ps = conn.prepareStatement(sql)) {
		        ps.setString(1, currentUser.getMemberId());
		        ps.setString(2, c.getCourseId());
		        ps.executeUpdate();
		        return true;
		    } catch (SQLException e) { e.printStackTrace(); }
		    return false;
		}
		
		public boolean enrollCourseSchedule(CourseSchedule cs) {
		    if (currentUser == null || cs == null) return false;
		    String sql = "INSERT OR IGNORE INTO enrollment_schedule(member_id, course_id, date) VALUES (?,?,?)";
		    try (Connection c = DB.getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
		        ps.setString(1, currentUser.getMemberId());
		        ps.setString(2, cs.getCourseId());
		        ps.setString(3, cs.getDate().toString());
		        ps.executeUpdate();
		        return true;
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}


		//給會員查詢自己選的課
		public List<Course> getCoursesForCurrentUser() {
		    if (currentUser == null) return List.of();
		    List<Course> list = new ArrayList<>();
		    String sql = """
		        SELECT c.* FROM enrollment e
		        JOIN course c ON e.course_id = c.course_id
		        WHERE e.member_id = ?""";
		    try (Connection conn = DB.getConn();
		         PreparedStatement ps = conn.prepareStatement(sql)) {
		        ps.setString(1, currentUser.getMemberId());
		        ResultSet rs = ps.executeQuery();
		        while (rs.next()) {
		            list.add(new Course(
		                rs.getString("course_id"),
		                rs.getString("title"),
		                rs.getString("instructor"),
		                rs.getString("schedule"),
		                rs.getDouble("fee")));
		        }
		    } catch (SQLException e) { e.printStackTrace(); }
		    return list;
		}
		
		public List<CourseSchedule> getEnrolledSchedulesForCurrentUser(String courseId, int year, int month) {
		    List<CourseSchedule> list = new ArrayList<>();
		    if (currentUser == null) return list;

		    String ym = String.format("%04d-%02d", year, month);

		    String sql = """
		        SELECT id, course_id, date FROM course_schedule
		        WHERE course_id = ? AND date LIKE ? AND date IN (
		            SELECT date FROM enrollment_schedule
		            WHERE member_id = ? AND course_id = ?
		        )
		    """;

		    try (Connection c = DB.getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
		        ps.setString(1, courseId);
		        ps.setString(2, ym + "%");
		        ps.setString(3, currentUser.getMemberId());
		        ps.setString(4, courseId);

		        ResultSet rs = ps.executeQuery();
		        while (rs.next()) {
		            list.add(new CourseSchedule(
		                rs.getInt("id"),
		                rs.getString("course_id"),
		                LocalDate.parse(rs.getString("date"))
		            ));
		        }

		    } catch (SQLException e) {
		        e.printStackTrace();
		    }

		    return list;
		}


		//給管理員查詢選課
		public List<String[]> getAllEnrollments() {
		    List<String[]> list = new ArrayList<>();
		    String sql = """
		        SELECT e.member_id, m.name, e.course_id, c.title, e.enroll_at
		        FROM enrollment e
		        JOIN member m  ON e.member_id = m.member_id
		        JOIN course c  ON e.course_id = c.course_id
		        ORDER BY e.enroll_at DESC""";
		    try (Connection conn = DB.getConn();
		         Statement st = conn.createStatement();
		         ResultSet rs = st.executeQuery(sql)) {
		        while (rs.next()) {
		            list.add(new String[]{
		                rs.getString("member_id"),
		                rs.getString("name"),
		                rs.getString("course_id"),
		                rs.getString("title"),
		                rs.getString("enroll_at")
		            });
		        }
		    } catch (SQLException e) { e.printStackTrace(); }
		    return list;
		}


		/**
		 * 取得課程費用
		 */
		public double getCourseFee(Course c) {
			return c.getFee();
		}

		/**
		 * 取得所有停車方案
		 */
		public List<ParkingPlan> getParkingPlans(){
		    List<ParkingPlan> list = new ArrayList<>();
		    String sql = "SELECT * FROM parking_plan ORDER BY plan_id";
		    try(Connection c = DB.getConn();
		        Statement s = c.createStatement();
		        ResultSet rs = s.executeQuery(sql)){
		        while(rs.next()){
		            list.add(new ParkingPlan(
		                rs.getString("plan_id"),
		                rs.getString("name"),
		                rs.getDouble("price")
		            ));
		        }
		    }catch(SQLException e){ e.printStackTrace(); }
		    return list;
		}

		/**
		 * 購買停車方案
		 */
		public boolean purchaseParking(ParkingPlan p){
		    if (currentUser == null || p == null) return false;

		    String sql = """
		    		  INSERT INTO member_parking(member_id,plan_id,buy_time)
		    		  VALUES (?,?,?)""";
    		try (Connection c = DB.getConn();
    		     PreparedStatement ps = c.prepareStatement(sql)) {

    		    ps.setString(1, currentUser.getMemberId());
    		    ps.setString(2, p.getPlanId());

    		    // 使用毫秒等級時間戳避免衝突
    		    String timeStr = LocalDateTime.now()
    		                       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    		    ps.setString(3, timeStr);

    		    ps.executeUpdate();
    		    return true;
    		} catch(SQLException e) {
    		    e.printStackTrace();
    		    return false;
    		}

		}
		
		/**
		 * 管理員查全部購買紀錄
		 */
		public List<String[]> getAllParkingPurchases(){
		    List<String[]> list = new ArrayList<>();
		    String sql = """
		      SELECT mp.member_id, m.name,
		             mp.plan_id,  pp.name   AS plan_name,
		             pp.price,    mp.buy_time
		      FROM   member_parking mp
		      JOIN   member       m  ON mp.member_id = m.member_id
		      JOIN   parking_plan pp ON mp.plan_id   = pp.plan_id
		      ORDER  BY mp.buy_time DESC""";
		    try(Connection c = DB.getConn();
		        Statement  s = c.createStatement();
		        ResultSet   rs= s.executeQuery(sql)){
		        while(rs.next()){
		            list.add(new String[]{
		                rs.getString("member_id"),
		                rs.getString("name"),
		                rs.getString("plan_id"),
		                rs.getString("plan_name"),
		                String.valueOf(rs.getDouble("price")),
		                rs.getString("buy_time")
		            });
		        }
		    }catch(SQLException e){ e.printStackTrace();}
		    return list;
		}


		/**
		 * 處理付款
		 */
		public boolean processPayment(Payment pay) {
			String sql = "INSERT INTO payment(member_id, amount, pay_time) VALUES (?,?,?)";
			try (Connection c = DB.getConn(); PreparedStatement ps = c.prepareStatement(sql)) {

				ps.setString(1, pay.getMemberId());
				ps.setDouble(2, pay.getAmount());
				ps.setString(3, pay.getPayTimeStr());
				ps.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean deleteMember(String memberId) {
		    try (Connection conn = DB.getConn()) {
		        conn.setAutoCommit(false); // 開始交易，避免半途失敗

		        // 建議同步刪除關聯資料（若無 ON DELETE CASCADE）
		        try (
		            PreparedStatement delAttendance = conn.prepareStatement("DELETE FROM attendance WHERE member_id = ?");
		            PreparedStatement delEnroll     = conn.prepareStatement("DELETE FROM enrollment WHERE member_id = ?");
		            PreparedStatement delPayment    = conn.prepareStatement("DELETE FROM payment WHERE member_id = ?");
		        	PreparedStatement delParking = conn.prepareStatement("DELETE FROM member_parking WHERE member_id = ?");
		            PreparedStatement delMember     = conn.prepareStatement("DELETE FROM member WHERE member_id = ?");
		        ) {
		            delAttendance.setString(1, memberId);
		            delEnroll.setString(1, memberId);
		            delPayment.setString(1, memberId);
		            delParking.setString(1, memberId);
		            delMember.setString(1, memberId);

		            delAttendance.executeUpdate();
		            delEnroll.executeUpdate();
		            delPayment.executeUpdate();
		            delParking.executeUpdate();
		            int affected = delMember.executeUpdate(); // 回傳值只有這裡重要

		            conn.commit(); // 交易成功
		            return affected > 0;
		        } catch (SQLException e) {
		            conn.rollback(); // 失敗就還原
		            e.printStackTrace();
		            return false;
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}


		// --------- 控制台舊版互動方法，可保留或移除 ---------
		public Member findMemberByEmail(String email) {
			for (Member m : members) {
				if (m.getEmail().equalsIgnoreCase(email))
					return m;
			}
			return null;
		}

		public void memberLogin() {
			Scanner sc = new Scanner(System.in);
			System.out.print("請輸入電子郵件: ");
			String email = sc.nextLine();
			Member member = findMemberByEmail(email);
			if (member == null) {
				System.out.println("查無此會員！");
				return;
			}
			System.out.print("請輸入密碼: ");
			String pwd = sc.nextLine();
			if (member.authenticate(pwd)) {
				System.out.println("歡迎光臨, " + member.getName() + "!");
				memberOperations(member);
			} else {
				System.out.println("密碼不正確。");
			}
		}

		public void memberOperations(Member member) {
			Scanner sc = new Scanner(System.in);
			boolean exit = false;
			while (!exit) {
				System.out.println("\n【會員操作選單】");
				System.out.println("1. 查看課程");
				System.out.println("2. 報名課程");
				System.out.println("3. 考勤打卡");
				System.out.println("4. 付款");
				System.out.println("5. 查看授權俱樂部");
				System.out.println("6. 登出");
				System.out.print("請選擇操作: ");
				String choice = sc.nextLine();
				switch (choice) {
				case "1" -> viewCourses();
				case "2" -> signUpCourse(member);
				case "3" -> {               // 考勤打卡
				    showAccessibleClubs(member);
				    System.out.print("輸入健身房 ID 進行打卡：");
				    String cid = sc.nextLine().trim();
				    GymClub sel = gymClubs.stream()
				                          .filter(g -> g.getClubId().equalsIgnoreCase(cid))
				                          .findFirst().orElse(null);
				    if (sel == null) {
				        System.out.println("找不到該健身房");
				    } else if (checkIn(sel)) {
				        System.out.println("打卡成功！");
				    } else {
				        System.out.println("您無權使用此健身房！");
				    }
				}

				case "4" -> makePayment(member);
				case "5" -> showAccessibleClubs(member);
				case "6" -> exit = true;
				default -> System.out.println("選項無效！");
				}
			}
		}

		public void viewCourses() {
			System.out.println("\n【課程列表】");
			for (Course c : courses) {
				System.out.println(c);
			}
		}

		public void signUpCourse(Member member) {
			viewCourses();
			Scanner sc = new Scanner(System.in);
			System.out.print("請輸入要報名的課程編號: ");
			String courseId = sc.nextLine();
			Course selected = null;
			for (Course c : courses) {
				if (c.getCourseId().equalsIgnoreCase(courseId)) {
					selected = c;
					break;
				}
			}
			if (selected != null) {
				member.getEnrolledCourses().add(selected);
				System.out.println(member.getName() + " 已成功報名課程: " + selected.getTitle());
			} else {
				System.out.println("查無此課程。");
			}
		}

		public boolean checkIn(GymClub club){
		    if (currentUser == null || club == null) return false;
		    if (!currentUser.canAccessRegion(club.getRegion())) return false;

		    String sql = "INSERT INTO attendance(member_id, club_id, check_in) VALUES (?,?,datetime('now','localtime'))";
		    try (Connection c = DB.getConn();
		         PreparedStatement ps = c.prepareStatement(sql)) {
		        ps.setString(1, currentUser.getMemberId());
		        ps.setString(2, club.getClubId());
		        ps.executeUpdate();
		        return true;
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}



		public void makePayment(Member member) {
		    Scanner sc = new Scanner(System.in);
		    System.out.print("請輸入付款金額: ");
		    double amount = sc.nextDouble();

		    // 建立付款物件：memberId、金額、時間
		    Payment pay = new Payment(
		    	    member.getMemberId(),
		    	    amount,
		    	    LocalDateTime.now());

		    // 寫進資料庫
		    if (processPayment(pay)) {
		        System.out.println("付款成功：" + pay);
		    } else {
		        System.out.println("付款失敗！");
		    }
		}

		public void memberRegistration() {
			Scanner sc = new Scanner(System.in);
			System.out.print("輸入會員ID: ");
			String id = sc.nextLine();
			System.out.print("輸入姓名: ");
			String name = sc.nextLine();
			System.out.print("輸入電子郵件: ");
			String email = sc.nextLine();
			System.out.print("設定密碼: ");
			String pwd = sc.nextLine();
			System.out.print("輸入地區代碼 (N/S/E/W/ALL): ");
			String regionInput = sc.nextLine().toUpperCase();
			Set<Region> regions;
			switch (regionInput) {
			case "N" -> regions = Set.of(Region.NORTH);
			case "S" -> regions = Set.of(Region.SOUTH);
			case "E" -> regions = Set.of(Region.EAST);
			case "W" -> regions = Set.of(Region.WEST);
			case "ALL" -> regions = Set.of(Region.values());
			default -> regions = Set.of(Region.NORTH);
			}
			members.add(new Member(id, name, email, pwd, regions));
			System.out.println("註冊成功：" + name);
		}

		public void showAccessibleClubs(Member member) {
			System.out.println("\n您可使用的健身俱樂部：");
			for (GymClub club : gymClubs) {
				if (member.canAccessRegion(club.getRegion())) {
					System.out.println(club);
				}
			}
		}
		
		public boolean addCourse(Course c) {
		    try (Connection conn = DB.getConn();
		         PreparedStatement ps = conn.prepareStatement(
		             "INSERT INTO course(course_id, title, instructor, schedule) VALUES (?, ?, ?, ?)")) {
		        ps.setString(1, c.getCourseId());
		        ps.setString(2, c.getTitle());
		        ps.setString(3, c.getInstructor());
		        ps.setString(4, c.getSchedule());
		        ps.executeUpdate();
		        return true;
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}

		public boolean updateCourse(String courseId, String newTitle, String newInstructor, String newSchedule, double newFee) {
		    try (Connection conn = DB.getConn();
		         PreparedStatement ps = conn.prepareStatement(
		             "UPDATE course SET title = ?, instructor = ?, schedule = ?, fee = ? WHERE course_id = ?")) {
		        ps.setString(1, newTitle);
		        ps.setString(2, newInstructor);
		        ps.setString(3, newSchedule);
		        ps.setDouble(4, newFee);
		        ps.setString(5, courseId);
		        return ps.executeUpdate() > 0;
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}

		
		public boolean insertCourseSchedule(String courseId, String dateStr) {
		    try (Connection c = DB.getConn();
		         PreparedStatement ps = c.prepareStatement(
		             "INSERT INTO course_schedule (course_id, date) VALUES (?, ?)")) {
		        ps.setString(1, courseId);
		        ps.setString(2, dateStr); // 格式要是 yyyy-MM-dd
		        ps.executeUpdate();
		        return true;
		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}


		public static void main(String[] args) {
			GymManagementSystem system = new GymManagementSystem();
			system.memberLogin();
		}

		public List<GymClub> getGymClubs() {
		    List<GymClub> list = new ArrayList<>();
		    String sql = "SELECT club_id, name, region FROM gym_club ORDER BY club_id";
		    try (Connection c = DB.getConn();
		         Statement  s = c.createStatement();
		         ResultSet  rs = s.executeQuery(sql)) {

		        while (rs.next()) {
		            list.add(new GymClub(
		                rs.getString("club_id"),
		                rs.getString("name"),
		                Region.valueOf(rs.getString("region"))
		            ));
		        }
		    } catch (SQLException e) { e.printStackTrace(); }
		    return list;
		}


		public List<Member> getMembers() {
			return members;
		}

		public List<Course> getCourses() {
			List<Course> list = new ArrayList<>();
			String sql = "SELECT * FROM course";
			try (Connection c = DB.getConn(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {

				while (rs.next()) {
					list.add(new Course(rs.getString("course_id"), rs.getString("title"), rs.getString("instructor"),
							rs.getString("schedule"), rs.getDouble("fee") // 如果你的 Course 無 fee 欄位，這行可省
					));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		public List<Payment> getPayments() {
			List<Payment> list = new ArrayList<>();
			String sql = "SELECT member_id, amount, pay_time FROM payment ORDER BY pay_time DESC";
			try (Connection c = DB.getConn(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {

				while (rs.next()) {
					list.add(new Payment(rs.getString("member_id"), rs.getDouble("amount"), LocalDateTime
							.parse(rs.getString("pay_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		public List<Attendance> getAttendanceRecords(){
		    List<Attendance> list = new ArrayList<>();
		    String sql = """
		        SELECT a.member_id, a.club_id, c.name AS club_name, a.check_in
		        FROM attendance a
		        JOIN gym_club c ON a.club_id = c.club_id
		        ORDER BY a.check_in DESC
		        """;
		    try (Connection c = DB.getConn();
		         Statement s = c.createStatement();
		         ResultSet rs = s.executeQuery(sql)) {

		        while (rs.next()) {
		            Attendance att = new Attendance(
		                rs.getString("member_id"),
		                rs.getString("club_id"),
		                LocalDateTime.parse(rs.getString("check_in"),
		                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
		            );
		            att.setClubName(rs.getString("club_name"));  // ✅ 加入名稱
		            list.add(att);
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return list;
		}

		public List<CourseSchedule> getSchedulesForCourse(String courseId, int year, int month) {
		    List<CourseSchedule> list = new ArrayList<>();
		    String ym = String.format("%04d-%02d", year, month); // e.g., "2025-06"

		    try (Connection conn = DB.getConn();
		         PreparedStatement stmt = conn.prepareStatement("""
		             SELECT id, course_id, date FROM course_schedule
		             WHERE course_id = ? AND date LIKE ?
		         """)) {

		        stmt.setString(1, courseId);
		        stmt.setString(2, ym + "%"); // e.g., LIKE '2025-06%'

		        ResultSet rs = stmt.executeQuery();
		        while (rs.next()) {
		            int id = rs.getInt("id");
		            String cid = rs.getString("course_id");
		            LocalDate date = LocalDate.parse(rs.getString("date"));
		            list.add(new CourseSchedule(id, cid, date));
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    return list;
		}

		
		public Member getCurrentUser() {
		    return currentUser;
		}
		
		
}
