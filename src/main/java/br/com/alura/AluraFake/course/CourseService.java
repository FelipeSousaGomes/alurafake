package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.MultipleChoiceTask;
import br.com.alura.AluraFake.task.OpenTextTask;
import br.com.alura.AluraFake.task.SingleChoiceTask;
import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    public CourseService(CourseRepository courseRepository, TaskRepository taskRepository) {
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void publishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new IllegalArgumentException("Course must be in BUILDING status to be published");
        }

        List<Task> tasks = taskRepository.findByCourseIdOrderByTaskOrderAsc(courseId);

        validateTaskTypes(tasks);
        validateSequentialOrder(tasks);

        course.publish();
        courseRepository.save(course);
    }

    public InstructorCoursesReportDTO getInstructorCoursesReport(Long instructorId) {
        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        List<CourseReportItemDTO> courseItems = courses.stream()
                .map(CourseReportItemDTO::new)
                .toList();

        Long totalPublished = courses.stream()
                .filter(course -> course.getStatus() == Status.PUBLISHED)
                .count();

        return new InstructorCoursesReportDTO(courseItems, totalPublished);
    }


    private void validateTaskTypes(List<Task> tasks) {
        boolean hasOpenText = tasks.stream().anyMatch(t -> t instanceof OpenTextTask);
        boolean hasSingleChoice = tasks.stream().anyMatch(t -> t instanceof SingleChoiceTask);
        boolean hasMultipleChoice = tasks.stream().anyMatch(t -> t instanceof MultipleChoiceTask);

        if (!hasOpenText) {
            throw new IllegalArgumentException("Course must have at least one OpenText task");
        }
        if (!hasSingleChoice) {
            throw new IllegalArgumentException("Course must have at least one SingleChoice task");
        }
        if (!hasMultipleChoice) {
            throw new IllegalArgumentException("Course must have at least one MultipleChoice task");
        }
    }

    private void validateSequentialOrder(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTaskOrder() != i + 1) {
                throw new IllegalArgumentException("Task order must be sequential without gaps");
            }
        }
    }
}
