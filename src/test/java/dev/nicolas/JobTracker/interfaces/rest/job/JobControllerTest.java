package dev.nicolas.JobTracker.interfaces.rest.job;

import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.application.dto.job.UpdateJobRequest;
import dev.nicolas.JobTracker.application.usecases.job.create.CreateJobUseCase;
import dev.nicolas.JobTracker.application.usecases.job.delete.DeleteJobUseCase;
import dev.nicolas.JobTracker.application.usecases.job.get.GetJobUseCase;
import dev.nicolas.JobTracker.application.usecases.job.update.UpdateJobUseCase;
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

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateJobUseCase createJobUseCase;

    @MockitoBean
    private GetJobUseCase getJobUseCase;

    @MockitoBean
    private UpdateJobUseCase updateJobUseCase;

    @MockitoBean
    private DeleteJobUseCase deleteJobUseCase;

    @Test
    void shouldCreateJob() throws Exception {
        UUID id = UUID.randomUUID();

        when(createJobUseCase.execute(any())).thenReturn(
                new JobResponse(id, "Acme", "Backend Engineer", "https://example.com/job", "Pleno", "Remoto", "Descricao")
        );

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Acme",
                                  "title": "Backend Engineer",
                                  "sourceUrl": "https://example.com/job",
                                  "seniority": "Pleno",
                                  "location": "Remoto",
                                  "description": "Descricao"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.company").value("Acme"))
                .andExpect(jsonPath("$.title").value("Backend Engineer"));
    }

    @Test
    void shouldListJobs() throws Exception {
        when(getJobUseCase.findAll()).thenReturn(List.of(
                new JobResponse(UUID.randomUUID(), "Acme", "Backend Engineer", null, "Pleno", "Remoto", null),
                new JobResponse(UUID.randomUUID(), "Beta", "Data Engineer", null, "Senior", "Hibrido", null)
        ));

        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].company").value("Acme"))
                .andExpect(jsonPath("$[1].title").value("Data Engineer"));
    }

    @Test
    void shouldUpdateJob() throws Exception {
        UUID id = UUID.randomUUID();

        when(updateJobUseCase.execute(eq(id), any(UpdateJobRequest.class))).thenReturn(
                new JobResponse(id, "Acme", "Senior Backend Engineer", "https://example.com/job", "Senior", "Remoto", "Descricao atualizada")
        );

        mockMvc.perform(patch("/api/v1/jobs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Acme",
                                  "title": "Senior Backend Engineer",
                                  "sourceUrl": "https://example.com/job",
                                  "seniority": "Senior",
                                  "location": "Remoto",
                                  "description": "Descricao atualizada"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Acme"))
                .andExpect(jsonPath("$.title").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.seniority").value("Senior"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingJobWithMissingFields() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/jobs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "",
                                  "title": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownJob() throws Exception {
        UUID id = UUID.randomUUID();

        when(updateJobUseCase.execute(eq(id), any(UpdateJobRequest.class)))
                .thenThrow(new DomainException("Vaga não encontrada pelo id " + id));

        mockMvc.perform(patch("/api/v1/jobs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Acme",
                                  "title": "Senior Backend Engineer"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }

    @Test
    void shouldDeleteJob() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteJobUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/jobs/{id}", id))
                .andExpect(status().isNoContent());
    }
}
