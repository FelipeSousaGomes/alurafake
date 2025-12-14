package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.OpenTextTask;
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
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByInstructorId_ShouldReturnCourses_WhenInstructorExists() {

        User instructor = new User("Paulo Silveira", "paulo@alura.com.br", Role.INSTRUCTOR);
        entityManager.persist(instructor);


        User otherUser = new User("Outro", "outro@alura.com.br", Role.INSTRUCTOR);
        entityManager.persist(otherUser);

        Course javaCourse = new Course("Java OO", "Descrição Java", instructor);
        entityManager.persist(javaCourse);

        OpenTextTask task = new OpenTextTask();
        task.setStatement("O que é Polimorfismo?");
        task.setCourse(javaCourse);
        task.setTaskOrder(1);
        entityManager.persist(task);
        Course pythonCourse = new Course("Python", "Desc Python", otherUser);
        entityManager.persist(pythonCourse);


        entityManager.flush();
        entityManager.clear();


        List<Course> result = courseRepository.findByInstructorId(instructor.getId());


        assertThat(result).hasSize(1);

        Course foundCourse = result.get(0);


        assertThat(foundCourse.getTitle()).isEqualTo("Java OO");
        assertThat(foundCourse.getInstructor().getId()).isEqualTo(instructor.getId());

        assertThat(foundCourse.getTasks()).isNotEmpty();
        assertThat(foundCourse.getTasks().get(0).getStatement()).isEqualTo("O que é Polimorfismo?");
    }

    @Test
    void findByInstructorId_ShouldReturnEmpty_WhenNoCourses() {
        User instructor = new User("Sem Cursos", "sem@alura.com.br", Role.INSTRUCTOR);
        entityManager.persist(instructor);

        List<Course> result = courseRepository.findByInstructorId(instructor.getId());

        assertThat(result).isEmpty();
    }
}