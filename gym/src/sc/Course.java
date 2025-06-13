package sc;

public class Course {
    private final String courseId;
    private final String title;
    private final String instructor;
    private final String schedule;
    private final double fee;

    /** 建構子：所有欄位一次到位 */
    public Course(String courseId, String title,
                  String instructor, String schedule,
                  double fee) {
        this.courseId   = courseId;
        this.title      = title;
        this.instructor = instructor;
        this.schedule   = schedule;
        this.fee        = fee;
    }
    
 // 新增課程用的簡易建構子（fee 預設為 0）
    public Course(String courseId, String title, String instructor, String schedule) {
        this(courseId, title, instructor, schedule, 0);
    }


    /* -------- Getter -------- */
    public String  getCourseId()  { return courseId; }
    public String  getTitle()     { return title; }
    public String  getInstructor(){ return instructor; }
    public String  getSchedule()  { return schedule; }
    public double  getFee()       { return fee; }

    @Override
    public String toString() {    // 方便 JList 顯示
        return courseId + " " + title + " ($" + fee + ")";
    }
}
