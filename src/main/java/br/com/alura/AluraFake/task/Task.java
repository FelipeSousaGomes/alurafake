package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Length(min = 4, max = 255)
    @Column(nullable = false, length = 255)
    private String statement;

    @NotNull
    @Positive
    @Column(name = "task_order", nullable = false)
    private Integer order;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    protected Task( String statement, Integer order, Course course) {
        this.statement = statement;
        this.order = order;
        this.course = course;
    }

    @Deprecated
    protected Task() {

    }


    public Long getId() {
        return id;
    }
    

    public String getStatement() {
        return statement;
    }


    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Course getCourse() {
        return course;
    }

    public abstract Type getType();
}
