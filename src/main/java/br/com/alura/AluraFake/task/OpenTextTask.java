package br.com.alura.AluraFake.task;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OPEN_TEXT")
public class OpenTextTask extends Task {

    @Deprecated
    public OpenTextTask() {
    }

    @Override
    public Type getType() {
        return Type.OPEN_TEXT;
    }
}
