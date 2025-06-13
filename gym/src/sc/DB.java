package sc;

import java.sql.*;

public class DB {
	private static final String URL = "jdbc:sqlite:gym.db";

	static {
		try (Connection c = DriverManager.getConnection(URL); Statement s = c.createStatement()) {

			s.execute("""
					  CREATE TABLE IF NOT EXISTS member(
					    member_id TEXT PRIMARY KEY,
					    name      TEXT,
					    email     TEXT UNIQUE,
					    password  TEXT,
					    regions   TEXT              -- 直接列在欄位清單
					  );
					""");


			s.execute("""
					   CREATE TABLE IF NOT EXISTS admin(
					     admin_id TEXT PRIMARY KEY,
					     name     TEXT,
					     password TEXT
					   );
					""");

			// ⭐ 只有第一次才會插入預設管理員
			s.execute("""
					   INSERT OR IGNORE INTO admin
					   VALUES ('admin','系統管理員','123');
					""");

			s.execute("""
					  CREATE TABLE IF NOT EXISTS course(
					    course_id  TEXT PRIMARY KEY,
					    title      TEXT,
					    instructor TEXT,
					    schedule   TEXT,
					    fee        REAL
					  );
					""");

			s.execute("""
					CREATE TABLE IF NOT EXISTS enrollment(
					  member_id  TEXT,
					  course_id  TEXT,
					  enroll_at  TEXT,                 -- 存 ISO 日期字串
					  PRIMARY KEY(member_id, course_id),
					  FOREIGN KEY(member_id) REFERENCES member(member_id),
					  FOREIGN KEY(course_id) REFERENCES course(course_id)
					  );
					""");

			s.execute("""
					  CREATE TABLE IF NOT EXISTS payment(
					    id         INTEGER PRIMARY KEY AUTOINCREMENT,
					    member_id  TEXT,
					    amount     REAL,
					    pay_time   TEXT
					  );
					""");
			s.execute("""
					CREATE TABLE IF NOT EXISTS gym_club(
					club_id   TEXT PRIMARY KEY,
					name  TEXT,
					region    TEXT);
										  );
										""");
			s.execute("""
					CREATE TABLE IF NOT EXISTS attendance(
					  member_id TEXT,
					  club_id   TEXT,
					  check_in  TEXT,            -- yyyy-MM-dd HH:mm:ss
					  PRIMARY KEY(member_id, club_id, check_in)
					);
					""");
			s.execute("""
					-- A. 停車方案 master
					CREATE TABLE IF NOT EXISTS parking_plan (
					  plan_id TEXT PRIMARY KEY,   -- 例如 M1, Q2
					  name    TEXT,               -- 月租A / 季租B…
					  price   REAL
					);
			
					""");
			s.execute("""
					-- B. 購買紀錄 (多對多)
					CREATE TABLE IF NOT EXISTS member_parking (
					  member_id TEXT,
					  plan_id   TEXT,
					  buy_time  TEXT,
					  PRIMARY KEY(member_id, plan_id, buy_time),
					  FOREIGN KEY(member_id) REFERENCES member(member_id),
					  FOREIGN KEY(plan_id)   REFERENCES parking_plan(plan_id)
					);
					""");


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Connection getConn() throws SQLException {
		return DriverManager.getConnection(URL);
	}
}
