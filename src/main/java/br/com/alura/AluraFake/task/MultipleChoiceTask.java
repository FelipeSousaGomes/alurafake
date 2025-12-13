package br.com.alura.AluraFake.task;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceTask extends Task{


    @Deprecated
    public MultipleChoiceTask() {
    }

    @ElementCollection
    private List<String> options;

    @Override
    public Type getType() {
        return Type.MULTIPLE_CHOICE;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }


}
