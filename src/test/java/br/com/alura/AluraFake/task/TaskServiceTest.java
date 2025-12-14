package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private TaskService taskService;

    private Course course;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        course = spy(new Course("Java", "Curso de Java", instructor));
        when(course.getId()).thenReturn(1L);
    }



    @Test
    void createOpenTextTask_shouldCreateSuccessfully_whenDataIsValid() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        lenient().when(taskRepository.countByCourseId(any())).thenReturn(0L);
        lenient().when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(any(), any())).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(OpenTextTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTextTask result = taskService.createOpenTextTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(dto.getOrder(), result.getTaskOrder());
        verify(taskRepository, times(1)).save(any(OpenTextTask.class));
    }

    @Test
    void createOpenTextTask_shouldThrowException_whenCourseNotFound() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(999L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(1);

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createOpenTextTask(dto)
        );

        assertEquals("Course not found", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }


   }