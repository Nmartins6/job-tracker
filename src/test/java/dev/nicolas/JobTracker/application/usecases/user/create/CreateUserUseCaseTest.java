package dev.nicolas.JobTracker.application.usecases.user.create;

import dev.nicolas.JobTracker.application.dto.user.CreateUserRequest;
import dev.nicolas.JobTracker.application.dto.user.UserResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Test
    void shouldNormalizeEmailBeforeCheckingUniquenessAndSaving() {
        CreateUserRequest request = new CreateUserRequest(
                "Nicolas",
                "  NICOLAS@Example.COM  ",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        );

        when(userRepository.existsByEmail("nicolas@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = createUserUseCase.execute(request);

        verify(userRepository).existsByEmail("nicolas@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo("nicolas@example.com");
        assertThat(response.email()).isEqualTo("nicolas@example.com");
    }

    @Test
    void shouldRejectDuplicateEmailAfterNormalization() {
        CreateUserRequest request = new CreateUserRequest(
                "Nicolas",
                " NICOLAS@Example.COM ",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        );

        when(userRepository.existsByEmail("nicolas@example.com")).thenReturn(true);

        assertThatThrownBy(() -> createUserUseCase.execute(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Email já cadastrado: nicolas@example.com");

        verify(userRepository, never()).save(any(User.class));
    }
}
