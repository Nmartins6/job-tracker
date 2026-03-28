import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, switchMap, tap } from 'rxjs';
import {
  AuthenticatedUserResponse,
  CreateUserRequest,
  UserResponse,
} from '../api/models';
import { AuthStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly authStore = inject(AuthStore);

  signUp(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>('/api/v1/users', request).pipe(
      switchMap((user) =>
        this.login(request.email, request.password, user).pipe(map(() => user)),
      ),
    );
  }

  login(
    email: string,
    password: string,
    user?: Pick<UserResponse, 'id' | 'name'>,
  ): Observable<AuthenticatedUserResponse> {
    const authorizationHeader = this.buildAuthorizationHeader(email, password);

    return this.http
      .get<AuthenticatedUserResponse>('/api/v1/auth/me', {
        headers: new HttpHeaders({
          Authorization: authorizationHeader,
        }),
      })
      .pipe(
        tap((currentUser) =>
          this.authStore.setSession({
            email: currentUser.email,
            authorizationHeader,
            userId: user?.id ?? currentUser.id,
            name: user?.name ?? currentUser.name,
          }),
        ),
      );
  }

  logout(): void {
    this.authStore.clearSession();
  }

  private buildAuthorizationHeader(email: string, password: string): string {
    return `Basic ${btoa(`${email.trim()}:${password}`)}`;
  }
}
