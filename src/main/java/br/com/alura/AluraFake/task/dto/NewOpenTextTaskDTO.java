package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public class NewOpenTextTaskDTO {

    @NotNull(message = "Course ID cannot be null")
    @Positive(message = "Course ID must be positive")
    private Long courseId;

    @NotNull(message = "Statement cannot be null")
    @NotBlank(message = "Statement cannot be blank")
    @Length(min = 4, max = 255, message = "Statement must be between 4 and 255 characters")
    private String statement;

    @NotNull(message = "Order cannot be null")
    @Positive(message = "Order must be positive")
    private Integer order;

    public NewOpenTextTaskDTO() {}

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}

