package dev.nicolas.JobTracker.interfaces.rest.jobRequirement;

import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.application.dto.jobRequirement.UpdateJobRequirementRequest;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.create.CreateJobRequirementUseCase;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.delete.DeleteJobRequirementUseCase;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.get.GetJobRequirementUseCase;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.update.UpdateJobRequirementUseCase;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobRequirementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class JobRequirementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateJobRequirementUseCase createJobRequirementUseCase;

    @MockitoBean
    private GetJobRequirementUseCase getJobRequirementUseCase;

    @MockitoBean
    private DeleteJobRequirementUseCase deleteJobRequirementUseCase;

    @MockitoBean
    private UpdateJobRequirementUseCase updateJobRequirementUseCase;

    @Test
    void shouldCreateJobRequirement() throws Exception {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(createJobRequirementUseCase.execute(any())).thenReturn(
                new JobRequirementResponse(id, jobId, skillId, true, 4, 3)
        );

        mockMvc.perform(post("/api/v1/job-requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobId": "%s",
                                  "skillId": "%s",
                                  "mustHave": true,
                                  "desiredLevel": 4,
                                  "weight": 3
                                }
                                """.formatted(jobId, skillId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.mustHave").value(true))
                .andExpect(jsonPath("$.weight").value(3));
    }

    @Test
    void shouldReturnJobRequirementById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(getJobRequirementUseCase.findById(id)).thenReturn(
                new JobRequirementResponse(id, jobId, skillId, true, 4, 3)
        );

        mockMvc.perform(get("/api/v1/job-requirements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.skillId").value(skillId.toString()));
    }

    @Test
    void shouldListJobRequirementsByJobId() throws Exception {
        UUID jobId = UUID.randomUUID();

        when(getJobRequirementUseCase.findByJobId(jobId)).thenReturn(List.of(
                new JobRequirementResponse(UUID.randomUUID(), jobId, UUID.randomUUID(), true, 4, 5),
                new JobRequirementResponse(UUID.randomUUID(), jobId, UUID.randomUUID(), false, 3, 2)
        ));

        mockMvc.perform(get("/api/v1/jobs/{jobId}/requirements", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].weight").value(5))
                .andExpect(jsonPath("$[1].weight").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingJobRequirementWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/job-requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenJobRequirementDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getJobRequirementUseCase.findById(id))
                .thenThrow(new DomainException("Requisito da vaga não encontrado pelo id " + id));

        mockMvc.perform(get("/api/v1/job-requirements/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }

    @Test
    void shouldDeleteJobRequirement() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteJobRequirementUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/job-requirements/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateJobRequirement() throws Exception {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(updateJobRequirementUseCase.execute(eq(id), any(UpdateJobRequirementRequest.class)))
                .thenReturn(new JobRequirementResponse(id, jobId, skillId, false, 5, 4));

        mockMvc.perform(patch("/api/v1/job-requirements/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skillId": "%s",
                                  "mustHave": false,
                                  "desiredLevel": 5,
                                  "weight": 4
                                }
                                """.formatted(skillId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillId").value(skillId.toString()))
                .andExpect(jsonPath("$.mustHave").value(false))
                .andExpect(jsonPath("$.desiredLevel").value(5))
                .andExpect(jsonPath("$.weight").value(4));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownJobRequirement() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new DomainException("Requisito da vaga não encontrado pelo id " + id))
                .when(deleteJobRequirementUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/job-requirements/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
