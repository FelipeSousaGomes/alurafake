package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OPEN_TEXT")
public class OpenTextTask extends Task {

    public OpenTextTask(String statement, Integer order, Course course) {
        super(statement, order, course);
    }

    @Deprecated
    protected OpenTextTask() {
    }

    @Override
    public Type getType() {
        return Type.OPEN_TEXT;
    }
}
