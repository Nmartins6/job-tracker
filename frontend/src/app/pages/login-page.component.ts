import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toErrorMessage } from '../core/api/error.utils';
import { AuthService } from '../core/auth/auth.service';
import { AuthStore } from '../core/auth/auth.store';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);

  private readonly router = inject(Router);

  private readonly authService = inject(AuthService);

  protected readonly auth = inject(AuthStore);

  protected readonly loginPending = signal(false);

  protected readonly signUpPending = signal(false);

  protected readonly errorMessage = signal<string | null>(null);

  protected readonly successMessage = signal<string | null>(null);

  protected readonly alreadyAuthenticated = computed(() => this.auth.isAuthenticated());

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected readonly signUpForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    headline: [''],
    location: [''],
    bio: [''],
  });

  protected submitLogin(): void {
    if (this.loginPending()) {
      return;
    }

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.loginForm.getRawValue();
    this.loginPending.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.login(email, password).subscribe({
      next: () => {
        this.loginPending.set(false);
        this.successMessage.set('Sessão aberta. Vamos para o workspace.');
        void this.router.navigateByUrl('/workspace');
      },
      error: (error: unknown) => {
        this.loginPending.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'Não foi possível autenticar com a API.'),
        );
      },
    });
  }

  protected submitSignUp(): void {
    if (this.signUpPending()) {
      return;
    }

    if (this.signUpForm.invalid) {
      this.signUpForm.markAllAsTouched();
      return;
    }

    this.signUpPending.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.signUp(this.signUpForm.getRawValue()).subscribe({
      next: () => {
        this.signUpPending.set(false);
        this.successMessage.set(
          'Conta criada e sessão aberta. Agora o frontend já conhece o seu userId.',
        );
        void this.router.navigateByUrl('/workspace');
      },
      error: (error: unknown) => {
        this.signUpPending.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'Não foi possível criar a conta local.'),
        );
      },
    });
  }
}
