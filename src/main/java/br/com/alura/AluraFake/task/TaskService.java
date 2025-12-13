package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.NewMultipleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;

    public TaskService(TaskRepository taskRepository, CourseRepository courseRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public OpenTextTask createOpenTextTask(NewOpenTextTaskDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new IllegalArgumentException("Course is not in BUILDING status");
        }

        if (taskRepository.existsByCourseAndStatement(course, dto.getStatement())) {
            throw new IllegalArgumentException("Statement already exists in this course");
        }

        validateOrderSequence(course.getId(), dto.getOrder());

        adjustTaskOrders(course.getId(), dto.getOrder());

        OpenTextTask task = new OpenTextTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());

        return taskRepository.save(task);
    }

    @Transactional
    public SingleChoiceTask createSingleChoiceTask(NewSingleChoiceTaskDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new IllegalArgumentException("Course is not in BUILDING status");
        }

        if (taskRepository.existsByCourseAndStatement(course, dto.getStatement())) {
            throw new IllegalArgumentException("Statement already exists in this course");
        }


        validateSingleChoiceOptions(dto.getOptions(), dto.getStatement());


        validateOrderSequence(course.getId(), dto.getOrder());
        adjustTaskOrders(course.getId(), dto.getOrder());

        SingleChoiceTask task = new SingleChoiceTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());


        List<Option> options = dto.getOptions().stream()
                .map(optionDTO -> {
                    Option option = new Option();
                    option.setOptionText(optionDTO.getOption());
                    option.setIsCorrect(optionDTO.getIsCorrect());
                    option.setTask(task);
                    return option;
                })
                .toList();

        task.setOptions(options);

        return taskRepository.save(task);
    }

    private void validateSingleChoiceOptions(List<OptionDTO> options, String statement) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }
        long correctCount = options.stream()
                .filter(OptionDTO::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new IllegalArgumentException("Single choice task must have exactly one correct option");
        }
        Set<String> optionTexts = new HashSet<>();
        for (OptionDTO option : options) {
            String normalizedOption = option.getOption().trim().toLowerCase();

            if (!optionTexts.add(normalizedOption)) {
                throw new IllegalArgumentException("Options cannot be duplicated");
            }
            if (normalizedOption.equals(statement.trim().toLowerCase())) {
                throw new IllegalArgumentException("Option cannot be equal to statement");
            }
        }
    }



    @Transactional
    public MultipleChoiceTask createMultipleChoiceTask(NewMultipleChoiceTaskDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new IllegalArgumentException("Course is not in BUILDING status");
        }

        if (taskRepository.existsByCourseAndStatement(course, dto.getStatement())) {
            throw new IllegalArgumentException("Statement already exists in this course");
        }

        validateMultipleChoiceOptions(dto.getOptions(), dto.getStatement());

        validateOrderSequence(course.getId(), dto.getOrder());
        adjustTaskOrders(course.getId(), dto.getOrder());

        MultipleChoiceTask task = new MultipleChoiceTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());

        List<Option> options = dto.getOptions().stream()
                .map(optionDTO -> {
                    Option option = new Option();
                    option.setOptionText(optionDTO.getOption());
                    option.setIsCorrect(optionDTO.getIsCorrect());
                    option.setTask(task);
                    return option;
                })
                .toList();

        task.setOptions(options);

        return taskRepository.save(task);
    }





    private void validateMultipleChoiceOptions(List<OptionDTO> options, String statement) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }

        long correctCount = options.stream()
                .filter(OptionDTO::getIsCorrect)
                .count();

        if (correctCount < 2) {
            throw new IllegalArgumentException("Multiple choice task must have at least two correct options");
        }

        Set<String> optionTexts = new HashSet<>();
        for (OptionDTO option : options) {
            String normalizedOption = option.getOption().trim().toLowerCase();

            if (!optionTexts.add(normalizedOption)) {
                throw new IllegalArgumentException("Options cannot be duplicated");
            }
            if (normalizedOption.equals(statement.trim().toLowerCase())) {
                throw new IllegalArgumentException("Option cannot be equal to statement");
            }
        }
    }




    private void validateOrderSequence(Long courseId, Integer newOrder) {
        Long taskCount = taskRepository.countByCourseId(courseId);

        if (newOrder > taskCount + 1) {
            throw new IllegalArgumentException(
                    "Invalid order: cannot skip sequence. Maximum allowed order is " + (taskCount + 1)
            );
        }
    }

    private void adjustTaskOrders(Long courseId, Integer newOrder) {
        List<Task> tasksToAdjust = taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(courseId, newOrder);
        tasksToAdjust.forEach(task -> task.setTaskOrder(task.getTaskOrder() + 1));
        taskRepository.saveAll(tasksToAdjust);
    }
}


