import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from './models';

export function toErrorMessage(
  error: unknown,
  fallback = 'Algo saiu do esperado ao conversar com a API.',
): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const payload = error.error as Partial<ApiErrorResponse> | undefined;
  const message = payload?.message;

  if (Array.isArray(message) && message.length > 0) {
    return message.join(' | ');
  }

  if (typeof message === 'string' && message.trim().length > 0) {
    return message;
  }

  if (typeof error.message === 'string' && error.message.trim().length > 0) {
    return error.message;
  }

  return fallback;
}
