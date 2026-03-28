package dev.nicolas.JobTracker.application.usecases.jobRequirement.delete;

import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteJobRequirementUseCaseTest {

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @InjectMocks
    private DeleteJobRequirementUseCase deleteJobRequirementUseCase;

    @Test
    void shouldDeleteJobRequirementWhenItExists() {
        UUID id = UUID.randomUUID();

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.of(
                JobRequirement.reconstitute(id, UUID.randomUUID(), UUID.randomUUID(), true, 4, 3)
        ));

        deleteJobRequirementUseCase.execute(id);

        verify(jobRequirementRepository).deleteById(id);
    }

    @Test
    void shouldRejectDeleteWhenJobRequirementDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteJobRequirementUseCase.execute(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Requisito da vaga não encontrado pelo id " + id);

        verify(jobRequirementRepository, never()).deleteById(id);
    }
}
