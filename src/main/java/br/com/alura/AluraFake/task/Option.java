package br.com.alura.AluraFake.task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Length(min = 4, max = 80)
    @Column(nullable = false, length = 80)
    private String optionText;

    @NotNull
    @Column(nullable = false)
    @JsonProperty("isCorrect")
    private Boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonBackReference
    private Task task;

    public Option(String optionText, Boolean isCorrect, Task task) {
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.task = task;
    }

    @Deprecated
    protected Option() {}

    public Long getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
