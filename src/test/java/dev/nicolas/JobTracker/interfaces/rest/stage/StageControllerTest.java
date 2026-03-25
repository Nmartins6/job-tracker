package dev.nicolas.JobTracker.interfaces.rest.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nicolas.JobTracker.application.dto.stage.CompleteStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.StartStageRequest;
import dev.nicolas.JobTracker.application.usecases.stage.complete.CompleteStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.create.CreateStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.get.GetStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.start.StartStageUseCase;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class StageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateStageUseCase createStageUseCase;

    @MockitoBean
    private GetStageUseCase getStageUseCase;

    @MockitoBean
    private StartStageUseCase startStageUseCase;

    @MockitoBean
    private CompleteStageUseCase completeStageUseCase;

    @Test
    void shouldCreateStage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(createStageUseCase.execute(any())).thenReturn(
                new StageResponse(id, applicationId, "Screening", 1, null, null, null)
        );

        mockMvc.perform(post("/api/v1/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "%s",
                                  "name": "Screening",
                                  "orderIndex": 1
                                }
                                """.formatted(applicationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Screening"));
    }

    @Test
    void shouldReturnStageById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(getStageUseCase.findById(id)).thenReturn(
                new StageResponse(id, applicationId, "Challenge", 2, null, null, null)
        );

        mockMvc.perform(get("/api/v1/stages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.name").value("Challenge"));
    }

    @Test
    void shouldListStagesByApplicationId() throws Exception {
        UUID applicationId = UUID.randomUUID();

        when(getStageUseCase.findByApplicationId(applicationId)).thenReturn(List.of(
                new StageResponse(UUID.randomUUID(), applicationId, "Screening", 1, null, null, null),
                new StageResponse(UUID.randomUUID(), applicationId, "Technical Interview", 2, null, null, null)
        ));

        mockMvc.perform(get("/api/v1/applications/{applicationId}/stages", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Screening"))
                .andExpect(jsonPath("$[1].name").value("Technical Interview"));
    }

    @Test
    void shouldStartStage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 16, 21, 0);
        StartStageRequest request = new StartStageRequest(startedAt);

        when(startStageUseCase.execute(eq(id), any(StartStageRequest.class))).thenReturn(
                new StageResponse(id, applicationId, "Screening", 1, startedAt, null, null)
        );

        mockMvc.perform(patch("/api/v1/stages/{id}/start", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startedAt").value("2026-03-16T21:00:00"));
    }

    @Test
    void shouldCompleteStage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 16, 21, 0);
        LocalDateTime completedAt = LocalDateTime.of(2026, 3, 16, 22, 0);
        CompleteStageRequest request = new CompleteStageRequest(completedAt);

        when(completeStageUseCase.execute(eq(id), any(CompleteStageRequest.class))).thenReturn(
                new StageResponse(id, applicationId, "Screening", 1, startedAt, completedAt, null)
        );

        mockMvc.perform(patch("/api/v1/stages/{id}/complete", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").value("2026-03-16T22:00:00"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingStageWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenStageDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getStageUseCase.findById(id))
                .thenThrow(new DomainException("Etapa não encontrada pelo id " + id));

        mockMvc.perform(get("/api/v1/stages/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
