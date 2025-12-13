package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SINGLE_CHOICE")
public class SingleChoiceTask extends Task {

    public SingleChoiceTask(String statement, Integer order, Course course) {
        super(statement, order, course);
    }

    @Deprecated
    protected SingleChoiceTask() {
    }

    @Override
    public Type getType() {
        return Type.SINGLE_CHOICE;
    }

    }

