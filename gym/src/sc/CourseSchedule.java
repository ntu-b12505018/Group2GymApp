package sc;

import java.time.LocalDate;

public class CourseSchedule {
    private int id;
    private String courseId;
    private LocalDate date;

    public CourseSchedule(int id, String courseId, LocalDate date) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date.toString(); // 可自訂格式，如顯示為 "6月1日"
    }
    
    

}
