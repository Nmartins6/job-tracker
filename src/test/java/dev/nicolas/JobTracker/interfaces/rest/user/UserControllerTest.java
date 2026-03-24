package dev.nicolas.JobTracker.interfaces.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nicolas.JobTracker.application.dto.user.UserResponse;
import dev.nicolas.JobTracker.application.usecases.user.create.CreateUserUseCase;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @Test
    void shouldCreateUser() throws Exception {
        UUID id = UUID.randomUUID();

        when(createUserUseCase.execute(any())).thenReturn(new UserResponse(
                id,
                "Nicolas",
                "nicolas@example.com",
                "Backend Developer",
                "Brasil",
                "Bio"
        ));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new dev.nicolas.JobTracker.application.dto.user.CreateUserRequest(
                                "Nicolas",
                                "nicolas@example.com",
                                "123456",
                                "Backend Developer",
                                "Brasil",
                                "Bio"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("nicolas@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldNotExposeListUsersEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldNotExposeGetUserByIdEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
