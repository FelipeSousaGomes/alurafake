package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.NewMultipleChoiceTaskDTO;
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
        dto.setStatement("Descreva com suas palavras o conceito de herança em Java");
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
        dto.setStatement("Explique a diferença entre interface e classe abstrata");
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
        dto.setStatement("Quais são as vantagens do polimorfismo?");
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
        dto.setStatement("Descreva com suas palavras o conceito de herança em Java");
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
        dto.setStatement("Como você aplicaria os princípios SOLID no seu código?");
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
        dto.setStatement("Explique o conceito de encapsulamento com um exemplo prático");
        dto.setOrder(2);

        OpenTextTask existingTask1 = new OpenTextTask();
        existingTask1.setTaskOrder(2);
        existingTask1.setCourse(course);
        existingTask1.setStatement("Defina o que são métodos estáticos em Java");

        OpenTextTask existingTask2 = new OpenTextTask();
        existingTask2.setTaskOrder(3);
        existingTask2.setCourse(course);
        existingTask2.setStatement("Explique o uso de Collections no Java");

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
        dto.setStatement("O que você entende por programação orientada a objetos?");
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
        dto.setStatement("Faça uma reflexão sobre o que você aprendeu neste curso");
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
        dto.setStatement("Qual modificador de acesso permite que um atributo seja acessado apenas dentro da própria classe?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("public", false),
                new OptionDTO("private", true),
                new OptionDTO("protected", false)
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
        dto.setStatement("Qual palavra-chave é usada para herança em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("implements", false),
                new OptionDTO("extends", true)
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
        dto.setStatement("Qual é o tipo primitivo para números decimais em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("int", false),
                new OptionDTO("double", true)
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
        dto.setStatement("Qual modificador de acesso permite que um atributo seja acessado apenas dentro da própria classe?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("public", false),
                new OptionDTO("private", true)
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

    @Test
    void createSingleChoiceTask_shouldThrowException_whenNoCorrectOption() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual estrutura de dados segue o princípio FIFO?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Stack", false),
                new OptionDTO("Queue", false),
                new OptionDTO("ArrayList", false)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Single choice task must have exactly one correct option", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenMultipleCorrectOptions() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que é JVM?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Java Virtual Machine", true),
                new OptionDTO("Máquina Virtual Java", true),
                new OptionDTO("Java Development Kit", false)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Single choice task must have exactly one correct option", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenOptionsListIsNull() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é a palavra-chave para criar uma constante em Java?");
        dto.setOrder(1);
        dto.setOptions(null);

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Options list cannot be null or empty", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenOptionsListIsEmpty() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual símbolo é usado para comentários de uma linha em Java?");
        dto.setOrder(1);
        dto.setOptions(Collections.emptyList());

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Options list cannot be null or empty", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenDuplicateOptions() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual método é usado para comparar strings em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("equals", true),
                new OptionDTO("EQUALS", false),
                new OptionDTO("compareTo", false)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Options cannot be duplicated", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenOptionEqualsStatement() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("O que é polimorfismo?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Capacidade de um objeto assumir múltiplas formas", true),
                new OptionDTO("O que é polimorfismo?", false),
                new OptionDTO("Herança múltipla", false)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Option cannot be equal to statement", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowException_whenOrderSkipsSequence() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é o retorno do método hashCode()?");
        dto.setOrder(5);
        dto.setOptions(Arrays.asList(
                new OptionDTO("String", false),
                new OptionDTO("int", true),
                new OptionDTO("Object", false)
        ));

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createSingleChoiceTask(dto)
        );

        assertEquals("Invalid order: cannot skip sequence. Maximum allowed order is 3", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldAdjustExistingTaskOrders_whenInsertingInMiddle() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é a diferença entre == e equals()?");
        dto.setOrder(2);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Não há diferença", false),
                new OptionDTO("== compara referências, equals() compara conteúdo", true),
                new OptionDTO("== compara conteúdo, equals() compara referências", false)
        ));

        SingleChoiceTask existingTask1 = new SingleChoiceTask();
        existingTask1.setTaskOrder(2);
        existingTask1.setCourse(course);
        existingTask1.setStatement("Questão existente 1");

        SingleChoiceTask existingTask2 = new SingleChoiceTask();
        existingTask2.setTaskOrder(3);
        existingTask2.setCourse(course);
        existingTask2.setStatement("Questão existente 2");

        List<Task> existingTasks = Arrays.asList(existingTask1, existingTask2);

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(3L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 2)).thenReturn(existingTasks);
        when(taskRepository.save(any(SingleChoiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SingleChoiceTask result = taskService.createSingleChoiceTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(2, result.getTaskOrder());

        assertEquals(3, existingTask1.getTaskOrder());
        assertEquals(4, existingTask2.getTaskOrder());

        verify(taskRepository, times(1)).saveAll(existingTasks);
        verify(taskRepository, times(1)).save(any(SingleChoiceTask.class));
    }

    @Test
    void createSingleChoiceTask_shouldCreateAsFirstTask_whenCourseHasNoTasks() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é o tipo de retorno do método main()?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("int", false),
                new OptionDTO("void", true),
                new OptionDTO("String", false)
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
        assertEquals(1, result.getTaskOrder());
        verify(taskRepository, times(1)).save(any(SingleChoiceTask.class));
        verify(taskRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void createSingleChoiceTask_shouldCreateAtEnd_whenOrderIsAfterExistingTasks() {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual keyword é usada para definir pacotes em Java?");
        dto.setOrder(4);
        dto.setOptions(Arrays.asList(
                new OptionDTO("import", false),
                new OptionDTO("package", true),
                new OptionDTO("module", false)
        ));

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(3L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 4)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(SingleChoiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SingleChoiceTask result = taskService.createSingleChoiceTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(4, result.getTaskOrder());
        verify(taskRepository, times(1)).save(any(SingleChoiceTask.class));
        verify(taskRepository, times(1)).saveAll(Collections.emptyList());
    }


    // MULTIPLE CHOICE TESTS
    @Test
    void createMultipleChoiceTask_shouldCreateSuccessfully_whenDataIsValid() {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Quais são princípios do SOLID?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Single Responsibility", true),
                new OptionDTO("Open/Closed", true),
                new OptionDTO("Singleton Pattern", false),
                new OptionDTO("Liskov Substitution", true)
        ));

        when(course.getId()).thenReturn(1L);
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(false);
        when(taskRepository.countByCourseId(1L)).thenReturn(0L);
        when(taskRepository.findByCourseIdAndTaskOrderGreaterThanEqual(1L, 1)).thenReturn(Collections.emptyList());
        when(taskRepository.save(any(MultipleChoiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MultipleChoiceTask result = taskService.createMultipleChoiceTask(dto);

        assertNotNull(result);
        assertEquals(dto.getStatement(), result.getStatement());
        assertEquals(dto.getOrder(), result.getTaskOrder());
        assertEquals(course, result.getCourse());
        assertEquals(4, result.getOptions().size());
        verify(taskRepository, times(1)).save(any(MultipleChoiceTask.class));
    }

    @Test
    void createMultipleChoiceTask_shouldThrowException_whenCourseNotFound() {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(999L);
        dto.setStatement("Quais são tipos primitivos em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("int", true),
                new OptionDTO("String", false),
                new OptionDTO("double", true)
        ));

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createMultipleChoiceTask(dto)
        );

        assertEquals("Course not found", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createMultipleChoiceTask_shouldThrowException_whenCourseIsNotInBuildingStatus() {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Quais são modificadores de acesso em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("public", true),
                new OptionDTO("private", true),
                new OptionDTO("protected", true)
        ));

        when(course.getStatus()).thenReturn(Status.PUBLISHED);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createMultipleChoiceTask(dto)
        );

        assertEquals("Course is not in BUILDING status", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createMultipleChoiceTask_shouldThrowException_whenStatementAlreadyExists() {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Quais são princípios do SOLID?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Single Responsibility", true),
                new OptionDTO("Open/Closed", true)
        ));

        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.existsByCourseAndStatement(course, dto.getStatement())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createMultipleChoiceTask(dto)
        );

        assertEquals("Statement already exists in this course", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }


   }