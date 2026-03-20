package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.NoteJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.NoteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NoteRepositoryImplement implements NoteRepository {

    private final NoteJpaRepository noteJpaRepository;
    private final NoteMapper mapper;

    public NoteRepositoryImplement(NoteJpaRepository noteJpaRepository, NoteMapper mapper) {
        this.noteJpaRepository = noteJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Note save(Note note) {
        NoteJpaEntity entity = mapper.toJpaEntity(note);
        NoteJpaEntity saved = noteJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Note> findById(UUID id) {
        return noteJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Note> findByApplicationId(UUID applicationId) {
        return noteJpaRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
