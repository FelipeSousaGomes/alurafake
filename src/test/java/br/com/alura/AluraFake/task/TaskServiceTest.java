package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
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
    }


      // OPEN TEXT TEST
    @Test
    void createOpenTextTask_shouldCreateSuccessfully_whenDataIsValid() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(1);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 1)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(OpenTextTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTextTask result = taskService.createOpenTextTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(dto.getOrder(), result.getTaskOrder());
        assertEquals(course, result.getCourse());
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

    @Test
    void createOpenTextTask_shouldThrowException_whenCourseIsNotInBuildingStatus() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(1);

        when(course.getStatus()).thenReturn(Status.PUBLISHED);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createOpenTextTask(dto)
        );

        assertEquals("Course is not in BUILDING status", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createOpenTextTask_shouldThrowException_whenStatementAlreadyExists() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(1);

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createOpenTextTask(dto)
        );

        assertEquals("Statement already exists in this course", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createOpenTextTask_shouldThrowException_whenOrderSkipsSequence() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que aprendemos na aula de hoje?");
        dto.setOrder(5);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createOpenTextTask(dto)
        );

        assertEquals("Invalid order: cannot skip sequence. Maximum allowed order is 3", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createOpenTextTask_shouldAdjustExistingTaskOrders_whenInsertingInMiddle() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Nova tarefa no meio");
        dto.setOrder(2);

        OpenTextTask existingTask1 = new OpenTextTask();
        existingTask1.setTaskOrder(2);
        existingTask1.setCourse(course);
        existingTask1.setStatement("Tarefa existente 1");

        OpenTextTask existingTask2 = new OpenTextTask();
        existingTask2.setTaskOrder(3);
        existingTask2.setCourse(course);
        existingTask2.setStatement("Tarefa existente 2");

        List<Task> existingTasks = Arrays.asList(existingTask1, existingTask2);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(3L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 2)).thenReturn(existingTasks);
        when(taskRepository.save(any(OpenTextTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTextTask result = taskService.createOpenTextTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(2, result.getTaskOrder());

        assertEquals(3, existingTask1.getTaskOrder());
        assertEquals(4, existingTask2.getTaskOrder());

        verify(taskRepository, times(1)).saveAll(existingTasks);
        verify(taskRepository, times(1)).save(any(OpenTextTask.class));
    }

    @Test
    void createOpenTextTask_shouldCreateAsFirstTask_whenCourseHasNoTasks() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Primeira tarefa");
        dto.setOrder(1);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 1)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(OpenTextTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTextTask result = taskService.createOpenTextTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(1, result.getTaskOrder());
        verify(taskRepository, times(1)).save(any(OpenTextTask.class));
        verify(taskRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void createOpenTextTask_shouldCreateAtEnd_whenOrderIsAfterExistingTasks() {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Última tarefa");
        dto.setOrder(4);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(3L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 4)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(OpenTextTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenTextTask result = taskService.createOpenTextTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(4, result.getTaskOrder());
        verify(taskRepository, times(1)).save(any(OpenTextTask.class));
        verify(taskRepository, times(1)).saveAll(Collections.emptyList());
    }


    // SINGLE CHOICE TESTS
    @Test
    void createSingleChoiceTask_shouldCreateSuccessfully_whenDataIsValid() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é a capital do Brasil?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("São Paulo", false),
                new OptionDTO("Brasília", true),
                new OptionDTO("Rio de Janeiro", false)
        ));

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 1)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(SingleChoiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SingleChoiceTask result = taskService.createSingleChoiceTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(dto.getOrder(), result.getTaskOrder());
        assertEquals(course, result.getCourse());
        assertEquals(3, result.getOptions().size());
        verify(taskRepository, times(1)).save(any(SingleChoiceTask.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenCourseNotFound() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(999L);
        dto.setStatement("Qual é a capital do Brasil?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("São Paulo", false),
                new OptionDTO("Brasília", true)
        ));

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Course not found", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenCourseIsNotInBuildingStatus() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é a capital do Brasil?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("São Paulo", false),
                new OptionDTO("Brasília", true)
        ));

        when(course.getStatus()).thenReturn(Status.PUBLISHED);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Course is not in BUILDING status", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenStatementAlreadyExists() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é a capital do Brasil?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("São Paulo", false),
                new OptionDTO("Brasília", true)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Statement already exists in this course", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }


   }