package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String statement;
    @Column(name = "task_order")
    private Integer taskOrder;
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
    @JsonManagedReference
    private List<Option> options = new ArrayList<>();

    protected Task( String statement, Integer taskOrder, Course course) {
        this.statement = statement;
        this.taskOrder = taskOrder;
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


    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Course getCourse() {
        return course;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public abstract Type getType();

}
