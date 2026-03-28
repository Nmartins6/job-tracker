package dev.nicolas.JobTracker.interfaces.rest.note;

import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.application.usecases.note.create.CreateNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.delete.DeleteNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.get.GetNoteUseCase;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateNoteUseCase createNoteUseCase;

    @MockitoBean
    private GetNoteUseCase getNoteUseCase;

    @MockitoBean
    private DeleteNoteUseCase deleteNoteUseCase;

    @Test
    void shouldCreateNote() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        when(createNoteUseCase.execute(any())).thenReturn(
                new NoteResponse(id, applicationId, null, "Observação geral", createdAt)
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "%s",
                                  "content": "Observação geral"
                                }
                                """.formatted(applicationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.content").value("Observação geral"));
    }

    @Test
    void shouldReturnNoteById() throws Exception {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        when(getNoteUseCase.findById(id)).thenReturn(
                new NoteResponse(id, applicationId, stageId, "Feedback da etapa", createdAt)
        );

        mockMvc.perform(get("/api/v1/notes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
                .andExpect(jsonPath("$.stageId").value(stageId.toString()));
    }

    @Test
    void shouldListNotesByApplicationId() throws Exception {
        UUID applicationId = UUID.randomUUID();

        when(getNoteUseCase.findByApplicationId(applicationId)).thenReturn(List.of(
                new NoteResponse(UUID.randomUUID(), applicationId, null, "Primeira", LocalDateTime.now()),
                new NoteResponse(UUID.randomUUID(), applicationId, UUID.randomUUID(), "Segunda", LocalDateTime.now().plusMinutes(5))
        ));

        mockMvc.perform(get("/api/v1/applications/{applicationId}/notes", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Primeira"))
                .andExpect(jsonPath("$[1].content").value("Segunda"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingNoteWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnNotFoundWhenNoteDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(getNoteUseCase.findById(id))
                .thenThrow(new DomainException("Nota não encontrada pelo id " + id));

        mockMvc.perform(get("/api/v1/notes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }

    @Test
    void shouldDeleteNote() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteNoteUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/notes/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownNote() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new DomainException("Nota não encontrada pelo id " + id))
                .when(deleteNoteUseCase).execute(id);

        mockMvc.perform(delete("/api/v1/notes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }
}
