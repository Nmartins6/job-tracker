export type UUID = string;

export type ApplicationStatus = 'ACTIVE' | 'HIRED' | 'REJECTED' | 'WITHDRAWN';

export type ApplicationHistoryEventType =
  | 'NOTE'
  | 'STAGE_STARTED'
  | 'STAGE_COMPLETED'
  | 'STAGE_DEADLINE';

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string | string[];
}

export interface AuthenticatedUserResponse {
  id: UUID;
  name: string;
  email: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  headline?: string | null;
  location?: string | null;
  bio?: string | null;
}

export interface UserResponse {
  id: UUID;
  name: string;
  email: string;
  headline: string | null;
  location: string | null;
  bio: string | null;
}

export interface CreateSkillRequest {
  name: string;
  category?: string | null;
}

export interface SkillResponse {
  id: UUID;
  name: string;
  category: string | null;
}

export interface CreateJobRequest {
  company: string;
  title: string;
  sourceUrl?: string | null;
  seniority?: string | null;
  location?: string | null;
  description?: string | null;
}

export interface UpdateJobRequest {
  company: string;
  title: string;
  sourceUrl?: string | null;
  seniority?: string | null;
  location?: string | null;
  description?: string | null;
}

export interface JobResponse {
  id: UUID;
  company: string;
  title: string;
  sourceUrl: string | null;
  seniority: string | null;
  location: string | null;
  description: string | null;
}

export interface CreateUserSkillRequest {
  userId: UUID;
  skillId: UUID;
  yearsExperience: number;
  level: number;
}

export interface UpdateUserSkillRequest {
  skillId: UUID;
  yearsExperience: number;
  level: number;
}

export interface UserSkillResponse {
  id: UUID;
  userId: UUID;
  skillId: UUID;
  yearsExperience: number;
  level: number;
}

export interface CreateJobRequirementRequest {
  jobId: UUID;
  skillId: UUID;
  mustHave: boolean;
  desiredLevel: number;
  weight: number;
}

export interface UpdateJobRequirementRequest {
  skillId: UUID;
  mustHave: boolean;
  desiredLevel: number;
  weight: number;
}

export interface JobRequirementResponse {
  id: UUID;
  jobId: UUID;
  skillId: UUID;
  mustHave: boolean;
  desiredLevel: number;
  weight: number;
}

export interface CreateApplicationRequest {
  userId: UUID;
  jobId: UUID;
}

export interface UpdateApplicationRequest {
  userId: UUID;
  jobId: UUID;
  status: ApplicationStatus;
}

export interface ApplicationResponse {
  id: UUID;
  userId: UUID;
  jobId: UUID;
  status: ApplicationStatus;
}

export interface UpdateApplicationStatusRequest {
  status: ApplicationStatus;
}

export interface CreateStageRequest {
  applicationId: UUID;
  name: string;
  orderIndex: number;
  deadlineAt?: string | null;
}

export interface UpdateStageRequest {
  name: string;
  orderIndex: number;
  deadlineAt?: string | null;
}

export interface StageResponse {
  id: UUID;
  applicationId: UUID;
  name: string;
  orderIndex: number;
  startedAt: string | null;
  completedAt: string | null;
  deadlineAt: string | null;
}

export interface StartStageRequest {
  startedAt: string;
}

export interface CompleteStageRequest {
  completedAt: string;
}

export interface CreateNoteRequest {
  applicationId: UUID;
  stageId?: UUID | null;
  content: string;
}

export interface UpdateNoteRequest {
  stageId?: UUID | null;
  content: string;
}

export interface NoteResponse {
  id: UUID;
  applicationId: UUID;
  stageId: UUID | null;
  content: string;
  createdAt: string;
}

export interface ApplicationHistoryEventResponse {
  type: ApplicationHistoryEventType;
  referenceId: UUID;
  stageId: UUID | null;
  title: string;
  description: string;
  occurredAt: string;
}

export interface ApplicationHistoryResponse {
  applicationId: UUID;
  events: ApplicationHistoryEventResponse[];
}

export interface JobRequirementMatchResponse {
  skillId: UUID;
  mustHave: boolean;
  desiredLevel: number;
  candidateLevel: number | null;
  weight: number;
  met: boolean;
  gapLevel: number;
  matchPercentage: number;
}

export interface JobMatchingResponse {
  score: number;
  totalRequirements: number;
  metRequirements: number;
  unmetRequirements: number;
  mustHaveUnmetRequirements: number;
  requirements: JobRequirementMatchResponse[];
}
