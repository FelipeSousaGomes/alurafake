package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.user.*;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseRepository courseRepository,
                            UserRepository userRepository,
                            CourseService courseService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
    }

    @Transactional
    @PostMapping("/course/new")
    public ResponseEntity createCourse(@Valid @RequestBody NewCourseDTO newCourse, Principal principal) {
        String emailLogado = principal.getName();

        User instructor = userRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found in the database"));

        if (!instructor.isInstructor()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorItemDTO("user", "Only instructors can create courses."));
        }

        Course course = new Course(newCourse.getTitle(), newCourse.getDescription(), instructor);

        courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseListItemDTO>> listAllCourses() {
        List<CourseListItemDTO> courses = courseRepository.findAll().stream()
                .map(CourseListItemDTO::new)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity publishCourse(@PathVariable("id") Long id) {
        try {
            courseService.publishCourse(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("course", e.getMessage()));
        }
    }

    @GetMapping("/instructor/{instructorId}/courses")
    public ResponseEntity getInstructorCoursesReport(@PathVariable("instructorId") Long instructorId) {
        Optional<User> possibleUser = userRepository.findById(instructorId);

        if (possibleUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!possibleUser.get().isInstructor()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("instructorId", "An informed user is not an instructor."));
        }

        InstructorCoursesReportDTO report = courseService.getInstructorCoursesReport(instructorId);
        return ResponseEntity.ok(report);
    }
}