package dev.nicolas.JobTracker.interfaces.rest.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationRequest;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationStatusRequest;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventResponse;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventType;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryResponse;
import dev.nicolas.JobTracker.application.usecases.application.create.CreateApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.delete.DeleteApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.get.GetApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.history.GetApplicationHistoryUseCase;
import dev.nicolas.JobTracker.application.usecases.application.update.UpdateApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.update.UpdateApplicationStatusUseCase;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateApplicationUseCase createApplicationUseCase;

    @MockitoBean
    private GetApplicationUseCase getApplicationUseCase;

    @MockitoBean
    private GetApplicationHistoryUseCase getApplicationHistoryUseCase;

    @MockitoBean
    private DeleteApplicationUseCase deleteApplicationUseCase;

    @MockitoBean
    private UpdateApplicationUseCase updateApplicationUseCase;

    @MockitoBean
    private UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    @Test
    void shouldCreateApplication() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(createApplicationUseCase.execute(any())).thenReturn(
                new ApplicationResponse(
                        id,
                        userId,
                        jobId,
                        ApplicationStatus.ACTIVE,
                        "Enviar follow-up",
                        LocalDateTime.of(2026, 4, 2, 10, 0)
                )
        );

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "jobId": "%s",
                                  "nextAction": "Enviar follow-up",
                                  "nextActionDueAt": "2026-04-02T10:00:00"
                                }
                                """.formatted(userId, jobId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.nextAction").value("Enviar follow-up"));
    }

    @Test
    void shouldListAllApplications() throws Exception {
        when(getApplicationUseCase.findAll()).thenReturn(List.of(
                new ApplicationResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.ACTIVE, null, null),
                new ApplicationResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.REJECTED, "Registrar feedback", LocalDateTime.of(2026, 4, 3, 18, 0))
        ));

        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("REJECTED"));
    }

    @Test
    void shouldReturnApplicationById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(getApplicationUseCase.findById(id)).thenReturn(
                new ApplicationResponse(
                        id,
                        userId,
                        jobId,
                        ApplicationStatus.ACTIVE,
                        "Cobrar retorno",
                        LocalDateTime.of(2026, 4, 4, 9, 0)
                )
        );

        mockMvc.perform(get("/api/v1/applications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.nextAction").value("Cobrar retorno"));
    }

    @Test
    void shouldReturnApplicationHistory() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();

        when(getApplicationHistoryUseCase.execute(applicationId)).thenReturn(
                new ApplicationHistoryResponse(
                        applicationId,
                        List.of(
                                new ApplicationHistoryEventResponse(
                                        ApplicationHistoryEventType.NOTE,
                                        noteId,
                                        null,
                                        "Nota da candidatura",
                                        "Observação inicial",
                                        LocalDateTime.of(2026, 3, 21, 10, 0)
                                ),
                                new ApplicationHistoryEventResponse(
                                        ApplicationHistoryEventType.STAGE_STARTED,
                                        stageId,
                                        stageId,
                                        "Screening",
                                        "Etapa iniciada",
                                        LocalDateTime.of(2026, 3, 21, 11, 0)
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/v1/applications/{id}/history", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.events.length()").value(2))
                .andExpect(jsonPath("$.events[0].type").value("NOTE"))
                .andExpect(jsonPath("$.events[1].type").value("STAGE_STARTED"));
    }

    @Test
    void shouldUpdateApplicationStatus() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.HIRED);

        when(updateApplicationStatusUseCase.execute(eq(id), any(UpdateApplicationStatusRequest.class))).thenReturn(
                new ApplicationResponse(id, userId, jobId, ApplicationStatus.HIRED, "Fechar ciclo", null)
        );

        mockMvc.perform(patch("/api/v1/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIRED"));
    }

    @Test
    void shouldUpdateApplicationTracking() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UpdateApplicationRequest request = new UpdateApplicationRequest(
                userId,
                jobId,
                ApplicationStatus.WITHDRAWN,
                "Arquivar candidatura",
                LocalDateTime.of(2026, 4, 5, 12, 0)
        );

        when(updateApplicationUseCase.execute(eq(id), any(UpdateApplicationRequest.class))).thenReturn(
                new ApplicationResponse(
                        id,
                        userId,
                        jobId,
                        ApplicationStatus.WITHDRAWN,
                        "Arquivar candidatura",
                        LocalDateTime.of(2026, 4, 5, 12, 0)
                )
        );

        mockMvc.perform(patch("/api/v1/applications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.nextAction").value("Arquivar candidatura"));
    }

    @Test
    void shouldDeleteApplication() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteApplicationUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/applications/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingApplicationWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingApplicationWithMissingFields() throws Exception {
        mockMvc.perform(patch("/api/v1/applications/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getApplicationUseCase.findById(id))
                .thenThrow(new DomainException("Candidatura não encontrada pelo id " + id));

        mockMvc.perform(get("/api/v1/applications/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
