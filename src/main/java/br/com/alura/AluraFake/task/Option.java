package br.com.alura.AluraFake.task;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "option")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Length(min = 4, max = 80)
    @Column(nullable = false, length = 80)
    private String option;

    @NotNull
    @Column(nullable = false)
    private Boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    public Option(String option, Boolean isCorrect, Task task) {
        this.option = option;
        this.isCorrect = isCorrect;
        this.task = task;
    }

    @Deprecated
    protected Option() {}

    public Long getId() {
        return id;
    }

    public String getOption() {
        return option;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Task getTask() {
        return task;
    }
}
