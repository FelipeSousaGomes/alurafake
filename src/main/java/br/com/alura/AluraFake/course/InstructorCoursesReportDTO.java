
package br.com.alura.AluraFake.course;

import java.util.List;

public class InstructorCoursesReportDTO {
    private List<CourseReportItemDTO> courses;
    private Long totalPublishedCourses;

    public InstructorCoursesReportDTO(List<CourseReportItemDTO> courses, Long totalPublishedCourses) {
        this.courses = courses;
        this.totalPublishedCourses = totalPublishedCourses;
    }

    public List<CourseReportItemDTO> getCourses() {
        return courses;
    }

    public Long getTotalPublishedCourses() {
        return totalPublishedCourses;
    }
}
