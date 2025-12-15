package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.infra.security.TokenService;
import br.com.alura.AluraFake.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private CourseService courseService;


    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    @DisplayName("Deve criar curso com sucesso quando usuário é instrutor")
    void createCourse_ShouldReturnCreated_WhenUserIsInstructor() throws Exception {
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java OO");
        newCourseDTO.setDescription("Curso completo de Java");

        User instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);

        // Mock do Principal (simula o usuário logado injetado pelo Spring Security)
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("paulo@alura.com.br");

        when(userRepository.findByEmail("paulo@alura.com.br")).thenReturn(Optional.of(instructor));

        mockMvc.perform(post("/course/new")
                        .principal(principal) // Injeta o mock do usuário logado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated());

        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("Deve retornar Forbidden quando usuário é estudante")
    void createCourse_ShouldReturnForbidden_WhenUserIsStudent() throws Exception {
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java OO");
        newCourseDTO.setDescription("Curso completo de Java");

        User student = new User("Caio", "caio@alura.com.br", Role.STUDENT);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("caio@alura.com.br");

        when(userRepository.findByEmail("caio@alura.com.br")).thenReturn(Optional.of(student));

        mockMvc.perform(post("/course/new")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Apenas instrutores podem criar cursos"));
    }

    @Test
    void createCourse_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Desc");


        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("fantasma@alura.com.br");

        when(userRepository.findByEmail("fantasma@alura.com.br")).thenReturn(Optional.empty());

        mockMvc.perform(post("/course/new")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.message").value("Usuário logado não encontrado no banco"));
    }



    @Test
    @DisplayName("Deve listar todos os cursos")
    void listAllCourses_ShouldReturnList() throws Exception {
        User instructor = new User("Inst", "i@a.com", Role.INSTRUCTOR);
        Course c1 = new Course("Java", "Desc Java", instructor);
        Course c2 = new Course("Spring", "Desc Spring", instructor);

        when(courseRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[1].title").value("Spring"));
    }


    @Test
    @DisplayName("Deve publicar curso com sucesso (204)")
    void publishCourse_ShouldReturnNoContent_WhenSuccess() throws Exception {
        Long courseId = 1L;
        doNothing().when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId))
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).publishCourse(courseId);
    }

    @Test
    @DisplayName("Deve retornar BadRequest (400) quando serviço falha")
    void publishCourse_ShouldReturnBadRequest_WhenServiceFails() throws Exception {
        Long courseId = 1L;
        doThrow(new IllegalArgumentException("Curso incompleto")).when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Curso incompleto"));
    }

    @Test
    void getInstructorCoursesReport_ShouldReturnOk_WhenUserIsInstructor() throws Exception {
        Long instructorId = 1L;
        User instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);


        InstructorCoursesReportDTO mockReport = new InstructorCoursesReportDTO(
                Collections.emptyList(),
                instructorId
        );

        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(courseService.getInstructorCoursesReport(instructorId)).thenReturn(mockReport);

        mockMvc.perform(get("/instructor/{instructorId}/courses", instructorId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.courses").isArray());
    }

    @Test
    @DisplayName("Deve retornar NotFound (404) quando usuário não existe")
    void getInstructorCoursesReport_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Long instructorId = 99L;
        when(userRepository.findById(instructorId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/instructor/{instructorId}/courses", instructorId))
                .andExpect(status().isNotFound());

        verify(courseService, never()).getInstructorCoursesReport(any());
    }

    @Test
    @DisplayName("Deve retornar BadRequest (400) quando usuário não é instrutor")
    void getInstructorCoursesReport_ShouldReturnBadRequest_WhenUserIsNotInstructor() throws Exception {
        Long studentId = 2L;
        User student = new User("Caio", "caio@alura.com.br", Role.STUDENT);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/instructor/{instructorId}/courses", studentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário informado não é um instrutor"));

        verify(courseService, never()).getInstructorCoursesReport(any());
    }
}