import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (request.headers.has('Authorization')) {
    return next(request);
  }

  const authStore = inject(AuthStore);
  const authorizationHeader = authStore.authorizationHeader();

  if (!authorizationHeader) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: authorizationHeader,
      },
    }),
  );
};
