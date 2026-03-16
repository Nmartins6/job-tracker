package dev.nicolas.JobTracker.interfaces.rest.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationStatusRequest;
import dev.nicolas.JobTracker.application.usecases.application.create.CreateApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.get.GetApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.update.UpdateApplicationStatusUseCase;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

@WebMvcTest(ApplicationController.class)
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
    private UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    @Test
    void shouldCreateApplication() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(createApplicationUseCase.execute(any())).thenReturn(
                new ApplicationResponse(id, userId, jobId, ApplicationStatus.ACTIVE)
        );

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "jobId": "%s"
                                }
                                """.formatted(userId, jobId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldListAllApplications() throws Exception {
        when(getApplicationUseCase.findAll()).thenReturn(List.of(
                new ApplicationResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.ACTIVE),
                new ApplicationResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.REJECTED)
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
                new ApplicationResponse(id, userId, jobId, ApplicationStatus.ACTIVE)
        );

        mockMvc.perform(get("/api/v1/applications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()));
    }

    @Test
    void shouldUpdateApplicationStatus() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.HIRED);

        when(updateApplicationStatusUseCase.execute(eq(id), any(UpdateApplicationStatusRequest.class))).thenReturn(
                new ApplicationResponse(id, userId, jobId, ApplicationStatus.HIRED)
        );

        mockMvc.perform(patch("/api/v1/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIRED"));
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
    void shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getApplicationUseCase.findById(id))
                .thenThrow(new DomainException("Candidatura não encontrada pelo id " + id));

        mockMvc.perform(get("/api/v1/applications/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
