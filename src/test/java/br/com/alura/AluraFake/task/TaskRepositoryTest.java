package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByCourseAndStatement_ShouldReturnTrue_WhenExists() {
        Course course = createAndPersistCourse("inst1@email.com", "Curso Java");
        createAndPersistTask(course, "O que é Java?", 1);

        boolean exists = taskRepository.existsByCourseAndStatement(course, "O que é Java?");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCourseAndStatement_ShouldReturnFalse_WhenNotExists() {
        Course course = createAndPersistCourse("inst2@email.com", "Curso Python");
        createAndPersistTask(course, "O que é Java?", 1);

        boolean exists = taskRepository.existsByCourseAndStatement(course, "O que é Python?");

        assertThat(exists).isFalse();
    }

    @Test
    void countByCourseId_ShouldReturnCorrectCount() {
        Course course = createAndPersistCourse("inst3@email.com", "Curso Principal");
        createAndPersistTask(course, "Task 1", 1);
        createAndPersistTask(course, "Task 2", 2);
        createAndPersistTask(course, "Task 3", 3);

        Course otherCourse = createAndPersistCourse("inst4@email.com", "Outro Curso");
        createAndPersistTask(otherCourse, "Task Outro Curso", 1);

        Long count = taskRepository.countByCourseId(course.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void findByCourseIdAndTaskOrderGreaterThanEqual_ShouldReturnTasks() {
        Course course = createAndPersistCourse("inst5@email.com", "Curso Order");
        createAndPersistTask(course, "Task 1", 1);
        createAndPersistTask(course, "Task 2", 2);
        createAndPersistTask(course, "Task 3", 3);
        createAndPersistTask(course, "Task 4", 4);

        List<Task> result = taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(course.getId(), 2);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Task::getStatement)
                .containsExactlyInAnyOrder("Task 2", "Task 3", "Task 4");
        assertThat(result).extracting(Task::getTaskOrder).doesNotContain(1);
    }

    @Test
    void findByCourseIdOrderByTaskOrderAsc_ShouldReturnOrderedList() {
        Course course = createAndPersistCourse("inst6@email.com", "Curso Sort");
        createAndPersistTask(course, "Terceira", 3);
        createAndPersistTask(course, "Primeira", 1);
        createAndPersistTask(course, "Segunda", 2);

        List<Task> result = taskRepository.findByCourseIdOrderByTaskOrderAsc(course.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStatement()).isEqualTo("Primeira");
        assertThat(result.get(1).getStatement()).isEqualTo("Segunda");
        assertThat(result.get(2).getStatement()).isEqualTo("Terceira");
    }

    private Course createAndPersistCourse(String email, String title) {
        User instructor = new User("Instrutor", email, Role.INSTRUCTOR);
        entityManager.persist(instructor);

        Course course = new Course(title, "Desc", instructor);
        entityManager.persist(course);
        return course;
    }

    private void createAndPersistTask(Course course, String statement, int order) {
        OpenTextTask task = new OpenTextTask();
        task.setStatement(statement);
        task.setCourse(course);
        task.setTaskOrder(order);
        entityManager.persist(task);
    }
}