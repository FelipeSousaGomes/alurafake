// CourseReportItemDTO.java
package br.com.alura.AluraFake.course;

import java.time.LocalDateTime;

public class CourseReportItemDTO {
    private Long id;
    private String title;
    private Status status;
    private LocalDateTime publishedAt;
    private Integer taskCount;

    public CourseReportItemDTO(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.status = course.getStatus();
        this.publishedAt = course.getPublishedAt();
        this.taskCount = course.getTasks() != null ? course.getTasks().size() : 0;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Integer getTaskCount() {
        return taskCount;
    }
}
