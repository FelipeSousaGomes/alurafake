package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceTaskDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {


    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @PostMapping("/task/new/opentext")
    public ResponseEntity<OpenTextTask> newOpenTextExercise(@Valid @RequestBody NewOpenTextTaskDTO dto) {
        OpenTextTask task = taskService.createOpenTextTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PostMapping("/task/new/singlechoice")
    public ResponseEntity<SingleChoiceTask> newSingleChoice(@Valid @RequestBody NewSingleChoiceTaskDTO dto) {
        SingleChoiceTask task = taskService.createSingleChoiceTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }


    @PostMapping("/task/new/multiplechoice")
    public ResponseEntity newMultipleChoice() {
        return ResponseEntity.ok().build();
    }

}