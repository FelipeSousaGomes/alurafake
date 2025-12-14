package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class OptionDTO {

    @NotNull(message = "Option text cannot be null")
    @NotBlank(message = "Option text cannot be blank")
    @Length(min = 4, max = 80, message = "Option must be between 4 and 80 characters")
    private String option;

    @NotNull(message = "isCorrect cannot be null")
    private Boolean isCorrect;

    public OptionDTO(String option, boolean isCorrect) {
        this.option = option;
        this.isCorrect = isCorrect;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
