package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.NewMultipleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, CourseRepository courseRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public OpenTextTask createOpenTextTask(NewOpenTextTaskDTO dto) {
        Course course = validateAndGetCourse(dto.getCourseId(), dto.getStatement());
        prepareTaskOrder(course.getId(), dto.getOrder());

        OpenTextTask task = new OpenTextTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());

        return taskRepository.save(task);
    }

    @Transactional
    public SingleChoiceTask createSingleChoiceTask(NewSingleChoiceTaskDTO dto) {
        Course course = validateAndGetCourse(dto.getCourseId(), dto.getStatement());
        validateSingleChoiceOptions(dto.getOptions(), dto.getStatement());
        prepareTaskOrder(course.getId(), dto.getOrder());

        SingleChoiceTask task = new SingleChoiceTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());
        task.setOptions(mapOptions(dto.getOptions(), task));

        return taskRepository.save(task);
    }

    @Transactional
    public MultipleChoiceTask createMultipleChoiceTask(NewMultipleChoiceTaskDTO dto) {
        Course course = validateAndGetCourse(dto.getCourseId(), dto.getStatement());
        validateMultipleChoiceOptions(dto.getOptions(), dto.getStatement());
        prepareTaskOrder(course.getId(), dto.getOrder());

        MultipleChoiceTask task = new MultipleChoiceTask();
        task.setCourse(course);
        task.setStatement(dto.getStatement());
        task.setTaskOrder(dto.getOrder());
        task.setOptions(mapOptions(dto.getOptions(), task));

        return taskRepository.save(task);
    }

    private Course validateAndGetCourse(Long courseId, String statement) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new IllegalArgumentException("Course is not in BUILDING status");
        }

        if (taskRepository.existsByCourseAndStatement(course, statement)) {
            throw new IllegalArgumentException("Statement already exists in this course");
        }

        return course;
    }

    private void prepareTaskOrder(Long courseId, Integer newOrder) {
        validateOrderSequence(courseId, newOrder);
        adjustTaskOrders(courseId, newOrder);
    }

    private List<Option> mapOptions(List<OptionDTO> dtos, Task task) {
        return dtos.stream()
                .map(dto -> {
                    Option option = new Option();
                    option.setOptionText(dto.getOption());
                    option.setIsCorrect(dto.getIsCorrect());
                    option.setTask(task);
                    return option;
                })
                .toList();
    }

    private void validateSingleChoiceOptions(List<OptionDTO> options, String statement) {
        validateOptionsNotEmpty(options);

        long correctCount = options.stream()
                .filter(OptionDTO::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new IllegalArgumentException("Single choice task must have exactly one correct option");
        }

        validateUniqueOptions(options, statement);
    }

    private void validateMultipleChoiceOptions(List<OptionDTO> options, String statement) {
        validateOptionsNotEmpty(options);

        long correctCount = options.stream()
                .filter(OptionDTO::getIsCorrect)
                .count();

        if (correctCount < 2) {
            throw new IllegalArgumentException("Multiple choice task must have at least two correct options");
        }

        if (correctCount == options.size()) {
            throw new IllegalArgumentException("Multiple choice task must have at least one incorrect option");
        }

        validateUniqueOptions(options, statement);
    }

    private void validateOptionsNotEmpty(List<OptionDTO> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }
    }

    private void validateUniqueOptions(List<OptionDTO> options, String statement) {
        Set<String> optionTexts = new HashSet<>();
        String normalizedStatement = normalizeText(statement);

        for (OptionDTO option : options) {
            String normalizedOption = normalizeText(option.getOption());

            validateNoDuplicateOption(optionTexts, normalizedOption);
            validateOptionNotEqualToStatement(normalizedOption, normalizedStatement);
        }
    }

    private String normalizeText(String text) {
        return text.trim().toLowerCase();
    }

    private void validateNoDuplicateOption(Set<String> optionTexts, String normalizedOption) {
        if (!optionTexts.add(normalizedOption)) {
            throw new IllegalArgumentException("Options cannot be duplicated");
        }
    }

    private void validateOptionNotEqualToStatement(String normalizedOption, String normalizedStatement) {
        if (normalizedOption.equals(normalizedStatement)) {
            throw new IllegalArgumentException("Option cannot be equal to statement");
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


