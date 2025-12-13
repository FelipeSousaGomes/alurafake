package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {


    boolean existsByCourseAndStatement(Course course, String statement);

    Long countByCourseId(Long courseId);

    List<Task> findByCourseIdAndTaskOrderGreaterThanEqual(Long courseId, Integer taskOrder);
}
