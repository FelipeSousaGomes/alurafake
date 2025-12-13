package br.com.alura.AluraFake.task;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue("SINGLE_CHOICE")
public class SingleChoiceTask extends Task {


    @Deprecated
    public SingleChoiceTask() {
    }

    @ElementCollection
    private List<String> options;

    @Override
    public Type getType() {
        return Type.SINGLE_CHOICE;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
