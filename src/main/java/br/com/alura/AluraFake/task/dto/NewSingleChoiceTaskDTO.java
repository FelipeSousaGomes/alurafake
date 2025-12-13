package br.com.alura.AluraFake.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public class NewSingleChoiceTaskDTO {

    @NotNull(message = "Course ID cannot be null")
    @Positive(message = "Course ID must be positive")
    private Long courseId;

    @NotNull(message = "Statement cannot be null")
    @Length(min = 4, max = 255, message = "Statement must be between 4 and 255 characters")
    private String statement;

    @NotNull(message = "Order cannot be null")
    @Positive(message = "Order must be positive")
    private Integer order;

    @NotNull(message = "Options cannot be null")
    @Size(min = 2, max = 5, message = "Must have between 2 and 5 options")
    @Valid
    private List<OptionDTO> options;

    public NewSingleChoiceTaskDTO() {}

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

    public List<OptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDTO> options) {
        this.options = options;
    }
}
