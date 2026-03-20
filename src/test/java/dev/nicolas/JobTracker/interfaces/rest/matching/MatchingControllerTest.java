package dev.nicolas.JobTracker.interfaces.rest.matching;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.dto.matching.JobRequirementMatchResponse;
import dev.nicolas.JobTracker.application.usecases.matching.get.GetJobMatchingUseCase;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchingController.class)
@Import(GlobalExceptionHandler.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetJobMatchingUseCase getJobMatchingUseCase;

    @Test
    void shouldReturnJobMatching() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(getJobMatchingUseCase.execute(userId, jobId)).thenReturn(
                new JobMatchingResponse(
                        85,
                        2,
                        1,
                        1,
                        0,
                        List.of(
                                new JobRequirementMatchResponse(skillId, true, 4, 5, 3, true, 0, 100),
                                new JobRequirementMatchResponse(UUID.randomUUID(), false, 3, 2, 2, false, 1, 67)
                        )
                )
        );

        mockMvc.perform(get("/api/v1/jobs/{jobId}/matching", jobId)
                        .queryParam("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.totalRequirements").value(2))
                .andExpect(jsonPath("$.requirements.length()").value(2))
                .andExpect(jsonPath("$.requirements[0].skillId").value(skillId.toString()))
                .andExpect(jsonPath("$.requirements[1].gapLevel").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenMatchingCannotBeGenerated() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(getJobMatchingUseCase.execute(userId, jobId))
                .thenThrow(new DomainException("Usuário não encontrado pelo id " + userId));

        mockMvc.perform(get("/api/v1/jobs/{jobId}/matching", jobId)
                        .queryParam("userId", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsMissing() throws Exception {
        UUID jobId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/jobs/{jobId}/matching", jobId))
                .andExpect(status().isBadRequest());
    }
}
