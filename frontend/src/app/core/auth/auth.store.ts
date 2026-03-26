import { Injectable, computed, signal } from '@angular/core';

export interface AuthSession {
  email: string;
  authorizationHeader: string;
  userId: string | null;
  name: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly storageKey = 'jobtracker.frontend.session';

  private readonly state = signal<AuthSession | null>(this.restore());

  readonly session = computed(() => this.state());

  readonly isAuthenticated = computed(() => this.state() !== null);

  readonly email = computed(() => this.state()?.email ?? null);

  readonly knownUserId = computed(() => this.state()?.userId ?? null);

  readonly displayName = computed(
    () => this.state()?.name ?? this.state()?.email ?? 'Sessão local',
  );

  readonly authorizationHeader = computed(
    () => this.state()?.authorizationHeader ?? null,
  );

  setSession(session: AuthSession): void {
    const current = this.state();
    const next: AuthSession = {
      email: session.email,
      authorizationHeader: session.authorizationHeader,
      userId:
        session.userId ??
        (current?.email === session.email ? current.userId : null),
      name:
        session.name ?? (current?.email === session.email ? current.name : null),
    };

    this.state.set(next);
    localStorage.setItem(this.storageKey, JSON.stringify(next));
  }

  clearSession(): void {
    this.state.set(null);
    localStorage.removeItem(this.storageKey);
  }

  private restore(): AuthSession | null {
    try {
      const stored = localStorage.getItem(this.storageKey);

      if (!stored) {
        return null;
      }

      const parsed = JSON.parse(stored) as Partial<AuthSession>;

      if (
        typeof parsed.email !== 'string' ||
        typeof parsed.authorizationHeader !== 'string'
      ) {
        return null;
      }

      return {
        email: parsed.email,
        authorizationHeader: parsed.authorizationHeader,
        userId: typeof parsed.userId === 'string' ? parsed.userId : null,
        name: typeof parsed.name === 'string' ? parsed.name : null,
      };
    } catch {
      return null;
    }
  }
}
