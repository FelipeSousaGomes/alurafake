package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceTask extends Task {

    public MultipleChoiceTask(String statement, Integer order, Course course) {
        super(statement, order, course);
    }

    @Deprecated
    protected MultipleChoiceTask() {
    }

    @Override
    public Type getType() {
        return Type.MULTIPLE_CHOICE;
    }

}
