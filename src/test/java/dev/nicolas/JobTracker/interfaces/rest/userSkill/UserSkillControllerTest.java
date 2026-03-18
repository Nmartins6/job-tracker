package dev.nicolas.JobTracker.interfaces.rest.userSkill;

import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.application.usecases.userSkill.create.CreateUserSkillUseCase;
import dev.nicolas.JobTracker.application.usecases.userSkill.get.GetUserSkillUseCase;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSkillController.class)
@Import(GlobalExceptionHandler.class)
class UserSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserSkillUseCase createUserSkillUseCase;

    @MockitoBean
    private GetUserSkillUseCase getUserSkillUseCase;

    @Test
    void shouldCreateUserSkill() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(createUserSkillUseCase.execute(any())).thenReturn(
                new UserSkillResponse(id, userId, skillId, 3, 4)
        );

        mockMvc.perform(post("/api/v1/user-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "skillId": "%s",
                                  "yearsExperience": 3,
                                  "level": 4
                                }
                                """.formatted(userId, skillId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.level").value(4));
    }

    @Test
    void shouldReturnUserSkillById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(getUserSkillUseCase.findById(id)).thenReturn(
                new UserSkillResponse(id, userId, skillId, 5, 5)
        );

        mockMvc.perform(get("/api/v1/user-skills/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.skillId").value(skillId.toString()));
    }

    @Test
    void shouldListUserSkillsByUserId() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getUserSkillUseCase.findByUserId(userId)).thenReturn(List.of(
                new UserSkillResponse(UUID.randomUUID(), userId, UUID.randomUUID(), 3, 4),
                new UserSkillResponse(UUID.randomUUID(), userId, UUID.randomUUID(), 1, 2)
        ));

        mockMvc.perform(get("/api/v1/users/{userId}/skills", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].level").value(4))
                .andExpect(jsonPath("$[1].level").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserSkillWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/user-skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenUserSkillDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getUserSkillUseCase.findById(id))
                .thenThrow(new DomainException("Habilidade do usuário não encontrada pelo id " + id));

        mockMvc.perform(get("/api/v1/user-skills/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
