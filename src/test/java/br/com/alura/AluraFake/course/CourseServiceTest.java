package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.*;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        course = spy(new Course("Java Avançado", "Curso de Java Avançado", instructor));
    }

    //PUBLISH COURSE TESTS

    @Test
    void publishCourse_shouldPublishSuccessfully_whenCourseHasAllTaskTypes() {
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        OpenTextTask openTextTask = mock(OpenTextTask.class);
        when(openTextTask.getTaskOrder()).thenReturn(1);

        SingleChoiceTask singleChoiceTask = mock(SingleChoiceTask.class);
        when(singleChoiceTask.getTaskOrder()).thenReturn(2);

        MultipleChoiceTask multipleChoiceTask = mock(MultipleChoiceTask.class);
        when(multipleChoiceTask.getTaskOrder()).thenReturn(3);

        List<Task> tasks = Arrays.asList(openTextTask, singleChoiceTask, multipleChoiceTask);
        when(taskRepository.findByCourseIdOrderByTaskOrderAsc(1L)).thenReturn(tasks);

        courseService.publishCourse(1L);

        verify(course, times(1)).publish();
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void publishCourse_shouldThrowException_whenCourseNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(999L)
        );

        assertEquals("Course not found", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_shouldThrowException_whenCourseIsNotInBuildingStatus() {
        when(course.getStatus()).thenReturn(Status.PUBLISHED);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(1L)
        );

        assertEquals("Course must be in BUILDING status to be published", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_shouldThrowException_whenMissingOpenTextTask() {
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        SingleChoiceTask singleChoiceTask = mock(SingleChoiceTask.class);
        MultipleChoiceTask multipleChoiceTask = mock(MultipleChoiceTask.class);

        List<Task> tasks = Arrays.asList(singleChoiceTask, multipleChoiceTask);
        when(taskRepository.findByCourseIdOrderByTaskOrderAsc(1L)).thenReturn(tasks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(1L)
        );

        assertEquals("Course must have at least one OpenText task", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_shouldThrowException_whenMissingSingleChoiceTask() {
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        OpenTextTask openTextTask = mock(OpenTextTask.class);
        MultipleChoiceTask multipleChoiceTask = mock(MultipleChoiceTask.class);

        List<Task> tasks = Arrays.asList(openTextTask, multipleChoiceTask);
        when(taskRepository.findByCourseIdOrderByTaskOrderAsc(1L)).thenReturn(tasks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(1L)
        );

        assertEquals("Course must have at least one SingleChoice task", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_shouldThrowException_whenMissingMultipleChoiceTask() {
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        OpenTextTask openTextTask = mock(OpenTextTask.class);
        SingleChoiceTask singleChoiceTask = mock(SingleChoiceTask.class);

        List<Task> tasks = Arrays.asList(openTextTask, singleChoiceTask);
        when(taskRepository.findByCourseIdOrderByTaskOrderAsc(1L)).thenReturn(tasks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(1L)
        );

        assertEquals("Course must have at least one MultipleChoice task", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void publishCourse_shouldThrowException_whenTaskOrderIsNotSequential() {
        when(course.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        OpenTextTask openTextTask = mock(OpenTextTask.class);
        when(openTextTask.getTaskOrder()).thenReturn(1);

        SingleChoiceTask singleChoiceTask = mock(SingleChoiceTask.class);
        when(singleChoiceTask.getTaskOrder()).thenReturn(3); // Gap aqui - pula o 2

        MultipleChoiceTask multipleChoiceTask = mock(MultipleChoiceTask.class);


        List<Task> tasks = Arrays.asList(openTextTask, singleChoiceTask, multipleChoiceTask);
        when(taskRepository.findByCourseIdOrderByTaskOrderAsc(1L)).thenReturn(tasks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.publishCourse(1L)
        );

        assertEquals("Task order must be sequential without gaps", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
    }

}

