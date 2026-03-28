import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApplicationHistoryResponse,
  ApplicationResponse,
  CompleteStageRequest,
  CreateApplicationRequest,
  CreateJobRequest,
  CreateJobRequirementRequest,
  CreateNoteRequest,
  CreateSkillRequest,
  CreateStageRequest,
  CreateUserSkillRequest,
  JobMatchingResponse,
  JobRequirementResponse,
  JobResponse,
  NoteResponse,
  SkillResponse,
  StageResponse,
  StartStageRequest,
  UpdateApplicationStatusRequest,
  UserSkillResponse,
  UUID,
} from './models';

@Injectable({ providedIn: 'root' })
export class JobTrackerApiService {
  private readonly http = inject(HttpClient);

  private readonly baseUrl = '/api/v1';

  getJobs(): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(`${this.baseUrl}/jobs`);
  }

  createJob(request: CreateJobRequest): Observable<JobResponse> {
    return this.http.post<JobResponse>(`${this.baseUrl}/jobs`, request);
  }

  getJobRequirementsByJobId(jobId: UUID): Observable<JobRequirementResponse[]> {
    return this.http.get<JobRequirementResponse[]>(
      `${this.baseUrl}/jobs/${jobId}/requirements`,
    );
  }

  createJobRequirement(
    request: CreateJobRequirementRequest,
  ): Observable<JobRequirementResponse> {
    return this.http.post<JobRequirementResponse>(
      `${this.baseUrl}/job-requirements`,
      request,
    );
  }

  deleteJobRequirement(id: UUID): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/job-requirements/${id}`);
  }

  getSkills(): Observable<SkillResponse[]> {
    return this.http.get<SkillResponse[]>(`${this.baseUrl}/skills`);
  }

  createSkill(request: CreateSkillRequest): Observable<SkillResponse> {
    return this.http.post<SkillResponse>(`${this.baseUrl}/skills`, request);
  }

  getUserSkillsByUserId(userId: UUID): Observable<UserSkillResponse[]> {
    return this.http.get<UserSkillResponse[]>(`${this.baseUrl}/users/${userId}/skills`);
  }

  createUserSkill(request: CreateUserSkillRequest): Observable<UserSkillResponse> {
    return this.http.post<UserSkillResponse>(`${this.baseUrl}/user-skills`, request);
  }

  getApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`${this.baseUrl}/applications`);
  }

  getApplicationById(id: UUID): Observable<ApplicationResponse> {
    return this.http.get<ApplicationResponse>(`${this.baseUrl}/applications/${id}`);
  }

  createApplication(
    request: CreateApplicationRequest,
  ): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(
      `${this.baseUrl}/applications`,
      request,
    );
  }

  updateApplicationStatus(
    id: UUID,
    request: UpdateApplicationStatusRequest,
  ): Observable<ApplicationResponse> {
    return this.http.patch<ApplicationResponse>(
      `${this.baseUrl}/applications/${id}/status`,
      request,
    );
  }

  getApplicationHistory(id: UUID): Observable<ApplicationHistoryResponse> {
    return this.http.get<ApplicationHistoryResponse>(
      `${this.baseUrl}/applications/${id}/history`,
    );
  }

  getStagesByApplicationId(applicationId: UUID): Observable<StageResponse[]> {
    return this.http.get<StageResponse[]>(
      `${this.baseUrl}/applications/${applicationId}/stages`,
    );
  }

  createStage(request: CreateStageRequest): Observable<StageResponse> {
    return this.http.post<StageResponse>(`${this.baseUrl}/stages`, request);
  }

  deleteStage(id: UUID): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/stages/${id}`);
  }

  startStage(id: UUID, request: StartStageRequest): Observable<StageResponse> {
    return this.http.patch<StageResponse>(
      `${this.baseUrl}/stages/${id}/start`,
      request,
    );
  }

  completeStage(
    id: UUID,
    request: CompleteStageRequest,
  ): Observable<StageResponse> {
    return this.http.patch<StageResponse>(
      `${this.baseUrl}/stages/${id}/complete`,
      request,
    );
  }

  getNotesByApplicationId(applicationId: UUID): Observable<NoteResponse[]> {
    return this.http.get<NoteResponse[]>(
      `${this.baseUrl}/applications/${applicationId}/notes`,
    );
  }

  createNote(request: CreateNoteRequest): Observable<NoteResponse> {
    return this.http.post<NoteResponse>(`${this.baseUrl}/notes`, request);
  }

  deleteNote(id: UUID): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/notes/${id}`);
  }

  getMatching(jobId: UUID, userId: UUID): Observable<JobMatchingResponse> {
    return this.http.get<JobMatchingResponse>(
      `${this.baseUrl}/jobs/${jobId}/matching`,
      {
        params: {
          userId,
        },
      },
    );
  }
}
