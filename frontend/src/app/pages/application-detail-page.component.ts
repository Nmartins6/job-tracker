import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin, map, switchMap } from 'rxjs';
import { toErrorMessage } from '../core/api/error.utils';
import { JobTrackerApiService } from '../core/api/job-tracker-api.service';
import {
  ApplicationHistoryEventResponse,
  ApplicationResponse,
  ApplicationStatus,
  CreateJobRequirementRequest,
  CreateNoteRequest,
  CreateStageRequest,
  JobMatchingResponse,
  JobRequirementResponse,
  JobResponse,
  NoteResponse,
  SkillResponse,
  StageResponse,
  UUID,
} from '../core/api/models';

const APPLICATION_STATUSES: ApplicationStatus[] = [
  'ACTIVE',
  'HIRED',
  'REJECTED',
  'WITHDRAWN',
];

@Component({
  selector: 'app-application-detail-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './application-detail-page.component.html',
})
export class ApplicationDetailPageComponent {
  private readonly fb = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly api = inject(JobTrackerApiService);

  protected readonly isLoading = signal(true);

  protected readonly isSubmitting = signal(false);

  protected readonly errorMessage = signal<string | null>(null);

  protected readonly successMessage = signal<string | null>(null);

  protected readonly application = signal<ApplicationResponse | null>(null);

  protected readonly job = signal<JobResponse | null>(null);

  protected readonly jobRequirements = signal<JobRequirementResponse[]>([]);

  protected readonly stages = signal<StageResponse[]>([]);

  protected readonly notes = signal<NoteResponse[]>([]);

  protected readonly history = signal<ApplicationHistoryEventResponse[]>([]);

  protected readonly matching = signal<JobMatchingResponse | null>(null);

  protected readonly skills = signal<SkillResponse[]>([]);

  protected readonly statusOptions = APPLICATION_STATUSES;

  protected readonly sortedStages = computed(() =>
    [...this.stages()].sort((left, right) => left.orderIndex - right.orderIndex),
  );

  protected readonly scoreStyle = computed(() => {
    const score = this.matching()?.score ?? 0;
    return `${Math.max(score, 0)}%`;
  });

  protected readonly sortedRequirements = computed(() =>
    [...this.jobRequirements()].sort((left, right) => {
      if (left.mustHave !== right.mustHave) {
        return left.mustHave ? -1 : 1;
      }

      return right.weight - left.weight || right.desiredLevel - left.desiredLevel;
    }),
  );

  protected readonly statusForm = this.fb.nonNullable.group({
    status: ['ACTIVE' as ApplicationStatus, [Validators.required]],
  });

  protected readonly requirementForm = this.fb.nonNullable.group({
    skillId: ['', [Validators.required]],
    mustHave: [true],
    desiredLevel: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
    weight: [3, [Validators.required, Validators.min(1)]],
  });

  protected readonly noteForm = this.fb.nonNullable.group({
    content: ['', [Validators.required]],
    stageId: [''],
  });

  protected readonly stageForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    orderIndex: [1, [Validators.required, Validators.min(1)]],
    deadlineAt: [''],
  });

  constructor() {
    this.loadApplication();
  }

  protected loadApplication(): void {
    const applicationId = this.route.snapshot.paramMap.get('id');

    if (!applicationId) {
      this.errorMessage.set('A rota não trouxe o id da candidatura.');
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.api
      .getApplicationById(applicationId)
      .pipe(
        switchMap((application) =>
          forkJoin({
            jobs: this.api.getJobs(),
            skills: this.api.getSkills(),
            requirements: this.api.getJobRequirementsByJobId(application.jobId),
            stages: this.api.getStagesByApplicationId(applicationId),
            notes: this.api.getNotesByApplicationId(applicationId),
            history: this.api.getApplicationHistory(applicationId),
            matching: this.api.getMatching(application.jobId, application.userId),
          }).pipe(map((result) => ({ application, ...result }))),
        ),
      )
      .subscribe({
        next: ({
          application,
          jobs,
          skills,
          requirements,
          stages,
          notes,
          history,
          matching,
        }) => {
          this.application.set(application);
          this.job.set(jobs.find((job) => job.id === application.jobId) ?? null);
          this.skills.set(skills);
          this.jobRequirements.set(requirements);
          this.stages.set(stages);
          this.notes.set(notes);
          this.history.set(history.events);
          this.matching.set(matching);
          this.statusForm.patchValue({ status: application.status });
          this.stageForm.patchValue({
            orderIndex: stages.length + 1,
          });
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            toErrorMessage(error, 'Não foi possível abrir a candidatura.'),
          );
          this.isLoading.set(false);
      },
    });
  }

  protected createRequirement(): void {
    const application = this.application();

    if (!application || this.requirementForm.invalid) {
      this.requirementForm.markAllAsTouched();
      return;
    }

    const value = this.requirementForm.getRawValue();
    const request: CreateJobRequirementRequest = {
      jobId: application.jobId,
      skillId: value.skillId,
      mustHave: value.mustHave,
      desiredLevel: Number(value.desiredLevel),
      weight: Number(value.weight),
    };

    this.runMutation(
      this.api.createJobRequirement(request),
      'Requisito adicionado a esta vaga.',
      () => {
        this.requirementForm.patchValue({
          skillId: '',
          mustHave: true,
          desiredLevel: 3,
          weight: 3,
        });
      },
    );
  }

  protected updateStatus(): void {
    const application = this.application();

    if (!application || this.statusForm.invalid) {
      return;
    }

    this.runMutation(
      this.api.updateApplicationStatus(application.id, this.statusForm.getRawValue()),
      'Status da candidatura atualizado.',
    );
  }

  protected createNote(): void {
    const application = this.application();

    if (!application || this.noteForm.invalid) {
      this.noteForm.markAllAsTouched();
      return;
    }

    const value = this.noteForm.getRawValue();
    const request: CreateNoteRequest = {
      applicationId: application.id,
      stageId: value.stageId ? value.stageId : null,
      content: value.content,
    };

    this.runMutation(this.api.createNote(request), 'Nota registrada no histórico.', () => {
      this.noteForm.reset({ content: '', stageId: '' });
    });
  }

  protected createStage(): void {
    const application = this.application();

    if (!application || this.stageForm.invalid) {
      this.stageForm.markAllAsTouched();
      return;
    }

    const value = this.stageForm.getRawValue();
    const request: CreateStageRequest = {
      applicationId: application.id,
      name: value.name,
      orderIndex: Number(value.orderIndex),
      deadlineAt: value.deadlineAt ? `${value.deadlineAt}:00` : null,
    };

    this.runMutation(this.api.createStage(request), 'Etapa adicionada ao processo.', () => {
      this.stageForm.reset({
        name: '',
        orderIndex: this.sortedStages().length + 2,
        deadlineAt: '',
      });
    });
  }

  protected startStage(stageId: UUID): void {
    this.runMutation(
      this.api.startStage(stageId, {
        startedAt: toApiDateTime(new Date()),
      }),
      'Etapa iniciada.',
    );
  }

  protected completeStage(stageId: UUID): void {
    this.runMutation(
      this.api.completeStage(stageId, {
        completedAt: toApiDateTime(new Date()),
      }),
      'Etapa concluída.',
    );
  }

  protected eventLabel(event: ApplicationHistoryEventResponse): string {
    switch (event.type) {
      case 'NOTE':
        return 'Nota';
      case 'STAGE_STARTED':
        return 'Etapa iniciada';
      case 'STAGE_COMPLETED':
        return 'Etapa concluída';
      case 'STAGE_DEADLINE':
        return 'Prazo de etapa';
    }
  }

  protected statusClass(status: string): string {
    return `status-pill status-${status.toLowerCase()}`;
  }

  protected requirementLabel(skillId: UUID): string {
    const skill = this.skills().find((item) => item.id === skillId);
    return skill ? skill.name : `Skill ${skillId.slice(0, 8)}`;
  }

  private runMutation(
    request$: Observable<unknown>,
    successMessage: string,
    onSuccess?: () => void,
  ): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.successMessage.set(successMessage);
        onSuccess?.();
        this.loadApplication();
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'A operação não pôde ser concluída.'),
        );
      },
    });
  }
}

function toApiDateTime(date: Date): string {
  const pad = (value: number) => `${value}`.padStart(2, '0');

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate(),
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(
    date.getSeconds(),
  )}`;
}
