package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.infra.security.TokenService;
import br.com.alura.AluraFake.user.UserRepository;
import br.com.alura.AluraFake.task.dto.NewMultipleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.NewOpenTextTaskDTO;
import br.com.alura.AluraFake.task.dto.NewSingleChoiceTaskDTO;
import br.com.alura.AluraFake.task.dto.OptionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;


    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    // OPEN TEXT TASK TESTS

    @Test
    void newOpenTextTask_shouldCreateSuccessfully_whenDataIsValid() throws Exception {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Descreva o conceito de polimorfismo em Java");
        dto.setOrder(1);

        OpenTextTask createdTask = mock(OpenTextTask.class);
        when(createdTask.getId()).thenReturn(1L);
        when(createdTask.getStatement()).thenReturn(dto.getStatement());
        when(createdTask.getTaskOrder()).thenReturn(dto.getOrder());

        when(taskService.createOpenTextTask(any(NewOpenTextTaskDTO.class))).thenReturn(createdTask);

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.statement").value(dto.getStatement()))
                .andExpect(jsonPath("$.taskOrder").value(dto.getOrder()));

        verify(taskService, times(1)).createOpenTextTask(any(NewOpenTextTaskDTO.class));
    }

    @Test
    void newOpenTextTask_shouldReturnBadRequest_whenStatementIsBlank() throws Exception {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("");
        dto.setOrder(1);

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createOpenTextTask(any(NewOpenTextTaskDTO.class));
    }

    @Test
    void newOpenTextTask_shouldReturnBadRequest_whenCourseIdIsNull() throws Exception {
        NewOpenTextTaskDTO dto = new NewOpenTextTaskDTO();
        dto.setCourseId(null);
        dto.setStatement("Explique herança em Java");
        dto.setOrder(1);

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createOpenTextTask(any(NewOpenTextTaskDTO.class));
    }

    // SINGLE CHOICE TASK TESTS

    @Test
    void newSingleChoiceTask_shouldCreateSuccessfully_whenDataIsValid() throws Exception {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é o modificador de acesso mais restritivo em Java?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("public", false),
                new OptionDTO("private", true)
        ));

        SingleChoiceTask createdTask = mock(SingleChoiceTask.class);
        when(createdTask.getId()).thenReturn(1L);
        when(createdTask.getStatement()).thenReturn(dto.getStatement());
        when(createdTask.getTaskOrder()).thenReturn(dto.getOrder());

        when(taskService.createSingleChoiceTask(any(NewSingleChoiceTaskDTO.class))).thenReturn(createdTask);

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.statement").value(dto.getStatement()))
                .andExpect(jsonPath("$.taskOrder").value(dto.getOrder()));

        verify(taskService, times(1)).createSingleChoiceTask(any(NewSingleChoiceTaskDTO.class));
    }

    @Test
    void newSingleChoiceTask_shouldReturnBadRequest_whenOptionsIsNull() throws Exception {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Qual é o tipo primitivo para números inteiros?");
        dto.setOrder(1);
        dto.setOptions(null);

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createSingleChoiceTask(any(NewSingleChoiceTaskDTO.class));
    }

    @Test
    void newSingleChoiceTask_shouldReturnBadRequest_whenStatementIsTooShort() throws Exception {
        NewSingleChoiceTaskDTO dto = new NewSingleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Hi");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(new OptionDTO("test", true)));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createSingleChoiceTask(any(NewSingleChoiceTaskDTO.class));
    }

    // MULTIPLE CHOICE TASK TESTS

    @Test
    void newMultipleChoiceTask_shouldCreateSuccessfully_whenDataIsValid() throws Exception {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Quais são princípios do SOLID?");
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Single Responsibility", true),
                new OptionDTO("Open/Closed", true),
                new OptionDTO("Singleton Pattern", false)
        ));

        MultipleChoiceTask createdTask = mock(MultipleChoiceTask.class);
        when(createdTask.getId()).thenReturn(1L);
        when(createdTask.getStatement()).thenReturn(dto.getStatement());
        when(createdTask.getTaskOrder()).thenReturn(dto.getOrder());

        when(taskService.createMultipleChoiceTask(any(NewMultipleChoiceTaskDTO.class))).thenReturn(createdTask);

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.statement").value(dto.getStatement()))
                .andExpect(jsonPath("$.taskOrder").value(dto.getOrder()));

        verify(taskService, times(1)).createMultipleChoiceTask(any(NewMultipleChoiceTaskDTO.class));
    }

    @Test
    void newMultipleChoiceTask_shouldReturnBadRequest_whenOrderIsNull() throws Exception {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("Quais são estruturas de dados em Java?");
        dto.setOrder(null);
        dto.setOptions(Arrays.asList(
                new OptionDTO("List", true),
                new OptionDTO("Set", true)
        ));

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createMultipleChoiceTask(any(NewMultipleChoiceTaskDTO.class));
    }

    @Test
    void newMultipleChoiceTask_shouldReturnBadRequest_whenStatementIsTooLong() throws Exception {
        NewMultipleChoiceTaskDTO dto = new NewMultipleChoiceTaskDTO();
        dto.setCourseId(1L);
        dto.setStatement("a".repeat(301)); // Excede o limite de 300 caracteres
        dto.setOrder(1);
        dto.setOptions(Arrays.asList(
                new OptionDTO("Option 1", true),
                new OptionDTO("Option 2", true)
        ));

        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createMultipleChoiceTask(any(NewMultipleChoiceTaskDTO.class));
    }
}