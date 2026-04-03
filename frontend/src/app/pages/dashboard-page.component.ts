import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { toErrorMessage } from '../core/api/error.utils';
import { JobTrackerApiService } from '../core/api/job-tracker-api.service';
import {
  ApplicationStatus,
  ApplicationResponse,
  CreateApplicationRequest,
  CreateJobRequest,
  CreateNoteRequest,
  CreateSkillRequest,
  CreateUserSkillRequest,
  JobResponse,
  NoteResponse,
  SkillResponse,
  StageResponse,
  UpdateApplicationRequest,
  UpdateJobRequest,
  UpdateUserSkillRequest,
  UUID,
  UserSkillResponse,
} from '../core/api/models';
import { AuthStore } from '../core/auth/auth.store';

type StageAttentionUrgency =
  | 'overdue'
  | 'soon'
  | 'unscheduled'
  | 'started'
  | 'upcoming'
  | 'idle';

type DashboardTriageView =
  | 'all'
  | 'attention'
  | 'missing-next-action'
  | 'missing-stages'
  | 'stalled';

interface StageAttention {
  stageId: UUID | null;
  stageName: string | null;
  orderIndex: number | null;
  deadlineAt: string | null;
  quickAction: 'start' | 'complete' | null;
  urgency: StageAttentionUrgency;
  urgencyLabel: string;
  urgencyWeight: number;
  summary: string;
}

interface LatestActivity {
  happenedAt: string | null;
  title: string;
  summary: string;
}

interface ApplicationCard extends ApplicationResponse {
  company: string;
  title: string;
  location: string | null;
  seniority: string | null;
  stageAttention: StageAttention;
  stagesCount: number;
  latestActivity: LatestActivity;
  latestNote: NoteResponse | null;
  latestNotePreview: string | null;
  notesCount: number;
  nextActionTitle: string;
  nextActionSummary: string;
}

interface UserSkillCard extends UserSkillResponse {
  skillName: string;
  skillCategory: string | null;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './dashboard-page.component.html',
})
export class DashboardPageComponent {
  private readonly fb = inject(FormBuilder);

  private readonly api = inject(JobTrackerApiService);

  protected readonly auth = inject(AuthStore);

  protected readonly isLoading = signal(true);

  protected readonly isCreatingJob = signal(false);

  protected readonly isCreatingApplication = signal(false);

  protected readonly editingJobId = signal<UUID | null>(null);

  protected readonly editingApplicationId = signal<UUID | null>(null);

  protected readonly editingUserSkillId = signal<UUID | null>(null);

  protected readonly isSavingUserSkill = signal(false);

  protected readonly operatingApplicationId = signal<UUID | null>(null);

  protected readonly errorMessage = signal<string | null>(null);

  protected readonly successMessage = signal<string | null>(null);

  protected readonly companyFilter = signal('');

  protected readonly statusFilter = signal<ApplicationStatus | ''>('');

  protected readonly triageView = signal<DashboardTriageView>('all');

  protected readonly jobs = signal<JobResponse[]>([]);

  protected readonly skills = signal<SkillResponse[]>([]);

  protected readonly userSkills = signal<UserSkillResponse[]>([]);

  protected readonly applications = signal<ApplicationResponse[]>([]);

  protected readonly stagesByApplicationId = signal<Record<UUID, StageResponse[]>>({});

  protected readonly notesByApplicationId = signal<Record<UUID, NoteResponse[]>>({});

  protected readonly knownUserId = computed(() => this.auth.knownUserId());

  protected readonly sortedJobs = computed(() =>
    [...this.jobs()].sort(
      (left, right) =>
        left.company.localeCompare(right.company) || left.title.localeCompare(right.title),
    ),
  );

  protected readonly levelOptions = [1, 2, 3, 4, 5];

  protected readonly statusOptions: ApplicationStatus[] = [
    'ACTIVE',
    'HIRED',
    'REJECTED',
    'WITHDRAWN',
  ];

  protected readonly applicationCards = computed<ApplicationCard[]>(() => {
    const jobsById = new Map(this.jobs().map((job) => [job.id, job]));
    const stagesByApplicationId = this.stagesByApplicationId();
    const notesByApplicationId = this.notesByApplicationId();

    return this.applications()
      .map((application) => {
        const job = jobsById.get(application.jobId);
        const stageAttention = this.buildStageAttention(
          stagesByApplicationId[application.id] ?? [],
        );
        const applicationStages = stagesByApplicationId[application.id] ?? [];
        const applicationNotes = notesByApplicationId[application.id] ?? [];

        return {
          ...application,
          company: job?.company ?? 'Empresa nao encontrada',
          title: job?.title ?? 'Vaga sem titulo',
          location: job?.location ?? null,
          seniority: job?.seniority ?? null,
          stageAttention,
          stagesCount: applicationStages.length,
          latestActivity: this.buildLatestActivity(
            applicationStages,
            applicationNotes,
          ),
          latestNote: this.getLatestNote(applicationNotes),
          latestNotePreview: this.buildLatestNotePreview(applicationNotes),
          notesCount: applicationNotes.length,
          ...this.buildNextAction(application, stageAttention),
        };
      })
      .sort((left, right) => this.compareApplications(left, right));
  });

  protected readonly userSkillCards = computed<UserSkillCard[]>(() => {
    const skillsById = new Map(this.skills().map((skill) => [skill.id, skill]));

    return this.userSkills()
      .map((userSkill) => {
        const skill = skillsById.get(userSkill.skillId);

        return {
          ...userSkill,
          skillName: skill?.name ?? 'Skill sem nome',
          skillCategory: skill?.category ?? null,
        };
      })
      .sort((left, right) => right.level - left.level || left.skillName.localeCompare(right.skillName));
  });

  protected readonly companyOptions = computed(() =>
    [...new Set(this.jobs().map((job) => job.company).filter((company) => company.trim().length > 0))].sort(
      (left, right) => left.localeCompare(right),
    ),
  );

  protected readonly filteredApplicationCards = computed(() => {
    const companyFilter = this.companyFilter().trim().toLowerCase();
    const statusFilter = this.statusFilter();
    const triageView = this.triageView();

    return this.applicationCards().filter((application) => {
      const companyMatches = !companyFilter
        ? true
        : application.company.trim().toLowerCase() === companyFilter;

      const statusMatches = !statusFilter ? true : application.status === statusFilter;
      const triageMatches = this.matchesTriageView(application, triageView);

      return companyMatches && statusMatches && triageMatches;
    });
  });

  protected readonly hasActiveFilters = computed(
    () =>
      this.companyFilter().length > 0 ||
      this.statusFilter().length > 0 ||
      this.triageView() !== 'all',
  );

  protected readonly priorityApplicationCards = computed(() =>
    this.applicationCards()
      .filter((application) => application.status === 'ACTIVE')
      .filter(
        (application) =>
          application.stageAttention.urgencyWeight <= 3 ||
          this.manualNextActionUrgencyWeight(application) !== null,
      )
      .slice(0, 4),
  );

  protected readonly recentActivityCards = computed(() =>
    this.applicationCards()
      .filter((application) => application.latestActivity.happenedAt !== null)
      .sort((left, right) =>
        new Date(right.latestActivity.happenedAt!).getTime() -
        new Date(left.latestActivity.happenedAt!).getTime(),
      )
      .slice(0, 5),
  );

  protected readonly triageCounts = computed(() => {
    const activeApplications = this.applicationCards().filter(
      (application) => application.status === 'ACTIVE',
    );

    return {
      attention: activeApplications.filter((application) =>
        this.matchesTriageView(application, 'attention'),
      ).length,
      missingNextAction: activeApplications.filter((application) =>
        this.matchesTriageView(application, 'missing-next-action'),
      ).length,
      missingStages: activeApplications.filter((application) =>
        this.matchesTriageView(application, 'missing-stages'),
      ).length,
      stalled: activeApplications.filter((application) =>
        this.matchesTriageView(application, 'stalled'),
      ).length,
    };
  });

  protected readonly metrics = computed(() => {
    const applications = this.applications();
    const activeApplications = this.applicationCards().filter((item) => item.status === 'ACTIVE');

    return {
      totalApplications: applications.length,
      activeApplications: applications.filter((item) => item.status === 'ACTIVE').length,
      totalJobs: this.jobs().length,
      totalUserSkills: this.userSkills().length,
      trackedUserId: this.knownUserId(),
      overdueDeadlines: activeApplications.filter(
        (item) => item.stageAttention.urgency === 'overdue',
      ).length,
      dueSoonDeadlines: activeApplications.filter(
        (item) => item.stageAttention.urgency === 'soon',
      ).length,
      pendingPlanning: activeApplications.filter(
        (item) => item.stageAttention.urgency === 'unscheduled',
      ).length,
    };
  });

  protected readonly createJobForm = this.fb.nonNullable.group({
    company: ['', [Validators.required]],
    title: ['', [Validators.required]],
    sourceUrl: [''],
    seniority: [''],
    location: [''],
    description: [''],
  });

  protected readonly createSkillForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    category: [''],
  });

  protected readonly createUserSkillForm = this.fb.nonNullable.group({
    skillId: ['', [Validators.required]],
    yearsExperience: [0, [Validators.required, Validators.min(0)]],
    level: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
  });

  protected readonly createApplicationForm = this.fb.nonNullable.group({
    userId: [this.knownUserId() ?? '', [Validators.required]],
    jobId: ['', [Validators.required]],
    status: ['ACTIVE' as ApplicationStatus, [Validators.required]],
    nextAction: ['', [Validators.maxLength(255)]],
    nextActionDueAt: [''],
  });

  protected readonly quickNoteForm = this.fb.nonNullable.group({
    content: ['', [Validators.required]],
  });

  protected readonly quickNoteApplicationId = signal<UUID | null>(null);

  protected readonly isSavingQuickNote = signal(false);

  constructor() {
    this.loadWorkspace();
  }

  protected loadWorkspace(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    if (this.knownUserId()) {
      this.createApplicationForm.patchValue({ userId: this.knownUserId()! });
    }

    forkJoin({
      jobs: this.api.getJobs(),
      skills: this.api.getSkills(),
      applications: this.api.getApplications(),
      userSkills: this.knownUserId()
        ? this.api.getUserSkillsByUserId(this.knownUserId()!)
        : of([] as UserSkillResponse[]),
    })
      .pipe(
        switchMap(({ jobs, skills, applications, userSkills }) => {
          if (applications.length === 0) {
            return of({
              jobs,
              skills,
              applications,
              userSkills,
              stagesByApplicationId: {} as Record<UUID, StageResponse[]>,
              notesByApplicationId: {} as Record<UUID, NoteResponse[]>,
            });
          }

          return forkJoin({
            stagesCollection: forkJoin(
              applications.map((application) => this.api.getStagesByApplicationId(application.id)),
            ),
            notesCollection: forkJoin(
              applications.map((application) => this.api.getNotesByApplicationId(application.id)),
            ),
          }).pipe(
            map(({ stagesCollection, notesCollection }) => ({
              jobs,
              skills,
              applications,
              userSkills,
              stagesByApplicationId: Object.fromEntries(
                applications.map((application, index) => [
                  application.id,
                  stagesCollection[index],
                ]),
              ) as Record<UUID, StageResponse[]>,
              notesByApplicationId: Object.fromEntries(
                applications.map((application, index) => [
                  application.id,
                  notesCollection[index],
                ]),
              ) as Record<UUID, NoteResponse[]>,
            })),
          );
        }),
      )
      .subscribe({
        next: ({
          jobs,
          skills,
          applications,
          userSkills,
          stagesByApplicationId,
          notesByApplicationId,
        }) => {
          this.jobs.set(jobs);
          this.skills.set(skills);
          this.applications.set(applications);
          this.userSkills.set(userSkills);
          this.stagesByApplicationId.set(stagesByApplicationId);
          this.notesByApplicationId.set(notesByApplicationId);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(
            toErrorMessage(error, 'Nao foi possivel carregar o workspace.'),
          );
          this.isLoading.set(false);
        },
      });
  }

  protected submitSkill(): void {
    if (this.createSkillForm.invalid) {
      this.createSkillForm.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.api.createSkill(this.createSkillForm.getRawValue() as CreateSkillRequest).subscribe({
      next: (skill) => {
        this.createSkillForm.reset({
          name: '',
          category: '',
        });
        this.successMessage.set(`Skill "${skill.name}" adicionada ao catálogo.`);
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel cadastrar a skill.'),
        );
      },
    });
  }

  protected submitJob(): void {
    if (this.createJobForm.invalid) {
      this.createJobForm.markAllAsTouched();
      return;
    }

    this.isCreatingJob.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const request = this.createJobForm.getRawValue() as CreateJobRequest;
    const editingJobId = this.editingJobId();
    const operation$ = editingJobId
      ? this.api.updateJob(editingJobId, request as UpdateJobRequest)
      : this.api.createJob(request);

    operation$.subscribe({
      next: (job) => {
        this.isCreatingJob.set(false);
        this.clearJobEditing();
        this.successMessage.set(
          editingJobId
            ? `Vaga "${job.title}" atualizada com sucesso.`
            : `Vaga "${job.title}" registrada com sucesso.`,
        );
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.isCreatingJob.set(false);
        this.errorMessage.set(
          toErrorMessage(
            error,
            editingJobId
              ? 'Nao foi possivel atualizar a vaga.'
              : 'Nao foi possivel registrar a vaga.',
          ),
        );
      },
    });
  }

  protected editJob(job: JobResponse): void {
    this.editingJobId.set(job.id);
    this.createJobForm.setValue({
      company: job.company,
      title: job.title,
      sourceUrl: job.sourceUrl ?? '',
      seniority: job.seniority ?? '',
      location: job.location ?? '',
      description: job.description ?? '',
    });
  }

  protected cancelJobEditing(): void {
    this.clearJobEditing();
  }

  protected deleteJob(job: JobResponse): void {
    const confirmed = globalThis.confirm(
      `Remover a vaga "${job.title}" em ${job.company}? Essa ação só funciona se não houver candidatura vinculada.`,
    );

    if (!confirmed) {
      return;
    }

    this.isCreatingJob.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.api.deleteJob(job.id).subscribe({
      next: () => {
        this.isCreatingJob.set(false);
        if (this.editingJobId() === job.id) {
          this.clearJobEditing();
        }
        if (this.createApplicationForm.getRawValue().jobId === job.id) {
          this.createApplicationForm.patchValue({ jobId: '' });
        }
        this.successMessage.set(`Vaga "${job.title}" removida com sucesso.`);
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.isCreatingJob.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel remover a vaga.'),
        );
      },
    });
  }

  protected submitApplication(): void {
    if (this.knownUserId()) {
      this.createApplicationForm.patchValue({ userId: this.knownUserId()! });
    }

    if (this.createApplicationForm.invalid) {
      this.createApplicationForm.markAllAsTouched();
      return;
    }

    this.isCreatingApplication.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const value = this.createApplicationForm.getRawValue();
    const request = {
      userId: value.userId,
      jobId: value.jobId,
      status: value.status,
      nextAction: this.normalizeOptionalText(value.nextAction),
      nextActionDueAt: this.toOptionalDateTime(value.nextActionDueAt),
    };
    const editingApplicationId = this.editingApplicationId();
    const operation$ = editingApplicationId
      ? this.api.updateApplication(editingApplicationId, request as UpdateApplicationRequest)
      : this.api.createApplication(request as CreateApplicationRequest);

    operation$.subscribe({
        next: () => {
          this.isCreatingApplication.set(false);
          this.clearApplicationEditing();
          this.successMessage.set(
            editingApplicationId
              ? 'Candidatura atualizada com sucesso.'
              : 'Candidatura registrada. Abra o detalhe para acompanhar o andamento.',
          );
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.isCreatingApplication.set(false);
          this.errorMessage.set(
            toErrorMessage(
              error,
              editingApplicationId
                ? 'Nao foi possivel atualizar a candidatura.'
                : 'Nao foi possivel registrar a candidatura.',
            ),
          );
        },
      });
  }

  protected editApplication(application: ApplicationResponse): void {
    this.editingApplicationId.set(application.id);
    this.createApplicationForm.setValue({
      userId: application.userId,
      jobId: application.jobId,
      status: application.status,
      nextAction: application.nextAction ?? '',
      nextActionDueAt: this.toDateTimeLocalValue(application.nextActionDueAt),
    });
  }

  protected cancelApplicationEditing(): void {
    this.clearApplicationEditing();
  }

  protected startQuickNote(applicationId: UUID): void {
    this.quickNoteApplicationId.set(applicationId);
    this.quickNoteForm.reset({
      content: '',
    });
  }

  protected cancelQuickNote(): void {
    this.quickNoteApplicationId.set(null);
    this.quickNoteForm.reset({
      content: '',
    });
  }

  protected hasQuickStageAction(application: ApplicationCard): boolean {
    return application.status === 'ACTIVE' && application.stageAttention.quickAction !== null;
  }

  protected quickStageActionLabel(application: ApplicationCard): string {
    return application.stageAttention.quickAction === 'complete'
      ? 'Concluir etapa'
      : 'Iniciar etapa';
  }

  protected runQuickStageAction(application: ApplicationCard): void {
    const { quickAction, stageId, stageName } = application.stageAttention;

    if (application.status !== 'ACTIVE' || !stageId || !quickAction) {
      return;
    }

    this.operatingApplicationId.set(application.id);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const operation$ =
      quickAction === 'complete'
        ? this.api.completeStage(stageId, {
            completedAt: this.currentLocalDateTime(),
          })
        : this.api.startStage(stageId, {
            startedAt: this.currentLocalDateTime(),
          });

    operation$.subscribe({
      next: () => {
        this.operatingApplicationId.set(null);
        this.successMessage.set(
          quickAction === 'complete'
            ? `Etapa "${stageName}" concluida em ${application.company} · ${application.title}.`
            : `Etapa "${stageName}" iniciada em ${application.company} · ${application.title}.`,
        );
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.operatingApplicationId.set(null);
        this.errorMessage.set(
          toErrorMessage(
            error,
            quickAction === 'complete'
              ? 'Nao foi possivel concluir a etapa agora.'
              : 'Nao foi possivel iniciar a etapa agora.',
          ),
        );
      },
    });
  }

  protected canClearManualNextAction(application: ApplicationCard): boolean {
    return application.status === 'ACTIVE' && !!application.nextAction;
  }

  protected canQuickUpdateStatus(application: ApplicationCard): boolean {
    return application.status === 'ACTIVE' || application.status === 'WITHDRAWN';
  }

  protected quickStatusOptions(application: ApplicationCard): ApplicationStatus[] {
    if (application.status === 'ACTIVE') {
      return ['HIRED', 'REJECTED', 'WITHDRAWN'];
    }

    if (application.status === 'WITHDRAWN') {
      return ['ACTIVE'];
    }

    return [];
  }

  protected quickStatusLabel(status: ApplicationStatus): string {
    switch (status) {
      case 'HIRED':
        return 'Marcar contratada';
      case 'REJECTED':
        return 'Marcar rejeitada';
      case 'WITHDRAWN':
        return 'Registrar desistência';
      case 'ACTIVE':
        return 'Reativar';
    }
  }

  protected updateApplicationStatusQuickly(
    application: ApplicationCard,
    status: ApplicationStatus,
  ): void {
    if (application.status === status) {
      return;
    }

    this.operatingApplicationId.set(application.id);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.api
      .updateApplicationStatus(application.id, { status })
      .subscribe({
        next: () => {
          this.operatingApplicationId.set(null);
          this.successMessage.set(
            `Candidatura em ${application.company} · ${application.title} atualizada para ${status}.`,
          );
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.operatingApplicationId.set(null);
          this.errorMessage.set(
            toErrorMessage(error, 'Nao foi possivel atualizar o status da candidatura.'),
          );
        },
      });
  }

  protected clearManualNextAction(application: ApplicationCard): void {
    if (!this.canClearManualNextAction(application)) {
      return;
    }

    this.operatingApplicationId.set(application.id);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.api
      .updateApplication(application.id, {
        userId: application.userId,
        jobId: application.jobId,
        status: application.status,
        nextAction: null,
        nextActionDueAt: null,
      })
      .subscribe({
        next: () => {
          this.operatingApplicationId.set(null);
          this.successMessage.set(
            `Próxima ação removida de ${application.company} · ${application.title}.`,
          );
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.operatingApplicationId.set(null);
          this.errorMessage.set(
            toErrorMessage(error, 'Nao foi possivel limpar a próxima ação agora.'),
          );
        },
      });
  }

  protected submitQuickNote(application: ApplicationCard): void {
    if (this.quickNoteForm.invalid) {
      this.quickNoteForm.markAllAsTouched();
      return;
    }

    this.isSavingQuickNote.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const request = {
      applicationId: application.id,
      content: this.quickNoteForm.getRawValue().content.trim(),
    } as CreateNoteRequest;

    this.api.createNote(request).subscribe({
      next: () => {
        this.isSavingQuickNote.set(false);
        this.cancelQuickNote();
        this.successMessage.set(
          `Nota rápida registrada em ${application.company} · ${application.title}.`,
        );
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.isSavingQuickNote.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel registrar a nota rápida.'),
        );
      },
    });
  }

  protected submitUserSkill(): void {
    const userId = this.knownUserId();

    if (!userId) {
      this.errorMessage.set(
        'Seu perfil ainda nao foi identificado nesta sessao. Saia e entre novamente para continuar.',
      );
      return;
    }

    if (this.createUserSkillForm.invalid) {
      this.createUserSkillForm.markAllAsTouched();
      return;
    }

    this.isSavingUserSkill.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const value = this.createUserSkillForm.getRawValue();
    const editingUserSkillId = this.editingUserSkillId();
    const request = {
      skillId: value.skillId,
      yearsExperience: Number(value.yearsExperience),
      level: Number(value.level),
    };
    const operation$ = editingUserSkillId
      ? this.api.updateUserSkill(editingUserSkillId, request as UpdateUserSkillRequest)
      : this.api.createUserSkill({
          userId,
          ...(request as UpdateUserSkillRequest),
        } as CreateUserSkillRequest);

    operation$.subscribe({
      next: () => {
        this.isSavingUserSkill.set(false);
        this.clearUserSkillEditing();
        this.successMessage.set(
          editingUserSkillId
            ? 'Skill do perfil atualizada com sucesso.'
            : 'Skill vinculada ao seu perfil. O matching agora consegue usar esse dado.',
        );
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.isSavingUserSkill.set(false);
        this.errorMessage.set(
          toErrorMessage(
            error,
            editingUserSkillId
              ? 'Nao foi possivel atualizar a skill do perfil.'
              : 'Nao foi possivel vincular a skill ao seu perfil.',
          ),
        );
      },
    });
  }

  protected editUserSkill(userSkill: UserSkillResponse): void {
    this.editingUserSkillId.set(userSkill.id);
    this.createUserSkillForm.setValue({
      skillId: userSkill.skillId,
      yearsExperience: userSkill.yearsExperience,
      level: userSkill.level,
    });
  }

  protected cancelUserSkillEditing(): void {
    this.clearUserSkillEditing();
  }

  protected deleteUserSkill(userSkill: UserSkillCard): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.api.deleteUserSkill(userSkill.id).subscribe({
      next: () => {
        if (this.editingUserSkillId() === userSkill.id) {
          this.clearUserSkillEditing();
        }
        this.successMessage.set(`Skill "${userSkill.skillName}" removida do seu perfil.`);
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel remover a skill do seu perfil.'),
        );
      },
    });
  }

  protected statusClass(status: string): string {
    return `status-pill status-${status.toLowerCase()}`;
  }

  protected updateCompanyFilter(value: string): void {
    this.companyFilter.set(value);
  }

  protected updateStatusFilter(value: string): void {
    this.statusFilter.set((value as ApplicationStatus | '') ?? '');
  }

  protected updateTriageView(value: DashboardTriageView): void {
    this.triageView.set(value);
  }

  protected clearFilters(): void {
    this.companyFilter.set('');
    this.statusFilter.set('');
    this.triageView.set('all');
  }

  protected stageAttentionClass(attention: StageAttention): string {
    switch (attention.urgency) {
      case 'overdue':
        return 'tag tag-danger';
      case 'soon':
      case 'unscheduled':
        return 'tag tag-warning';
      default:
        return 'tag tag-success';
    }
  }

  private clearJobEditing(): void {
    this.editingJobId.set(null);
    this.createJobForm.reset({
      company: '',
      title: '',
      sourceUrl: '',
      seniority: '',
      location: '',
      description: '',
    });
  }

  private clearApplicationEditing(): void {
    this.editingApplicationId.set(null);
    this.createApplicationForm.reset({
      userId: this.knownUserId() ?? '',
      jobId: '',
      status: 'ACTIVE',
      nextAction: '',
      nextActionDueAt: '',
    });
  }

  private clearUserSkillEditing(): void {
    this.editingUserSkillId.set(null);
    this.createUserSkillForm.reset({
      skillId: '',
      yearsExperience: 0,
      level: 3,
    });
  }

  private buildStageAttention(stages: StageResponse[]): StageAttention {
    const pendingStages = [...stages]
      .filter((stage) => !stage.completedAt)
      .sort((left, right) => left.orderIndex - right.orderIndex);

    if (pendingStages.length === 0) {
      return {
        stageId: null,
        stageName: null,
        orderIndex: null,
        deadlineAt: null,
        quickAction: null,
        urgency: stages.length === 0 ? 'unscheduled' : 'idle',
        urgencyLabel: stages.length === 0 ? 'Planejar etapas' : 'Fluxo concluido',
        urgencyWeight: stages.length === 0 ? 2 : 5,
        summary:
          stages.length === 0
            ? 'Nenhuma etapa foi cadastrada ainda. Vale definir o proximo passo.'
            : 'Todas as etapas cadastradas foram concluidas.',
      };
    }

    const nextStage = pendingStages[0];

    if (!nextStage.deadlineAt) {
      return {
        stageId: nextStage.id,
        stageName: nextStage.name,
        orderIndex: nextStage.orderIndex,
        deadlineAt: null,
        quickAction: nextStage.startedAt ? 'complete' : 'start',
        urgency: nextStage.startedAt ? 'started' : 'unscheduled',
        urgencyLabel: nextStage.startedAt ? 'Em andamento' : 'Sem prazo',
        urgencyWeight: nextStage.startedAt ? 3 : 2,
        summary: nextStage.startedAt
          ? `${nextStage.name} esta em andamento, mas ainda sem prazo definido.`
          : `${nextStage.name} e a proxima etapa, mas ainda sem prazo definido.`,
      };
    }

    const deadlineAt = nextStage.deadlineAt;
    const deadlineTime = new Date(deadlineAt).getTime();
    const diffHours = (deadlineTime - Date.now()) / (1000 * 60 * 60);
    const deadlineLabel = this.deadlineDistanceLabel(deadlineAt);
    const formattedDeadline = this.formatDateTime(deadlineAt);

    if (diffHours < 0) {
      return {
        stageId: nextStage.id,
        stageName: nextStage.name,
        orderIndex: nextStage.orderIndex,
        deadlineAt,
        quickAction: nextStage.startedAt ? 'complete' : 'start',
        urgency: 'overdue',
        urgencyLabel: 'Prazo vencido',
        urgencyWeight: 0,
        summary: `${nextStage.name} ${deadlineLabel} (${formattedDeadline}).`,
      };
    }

    if (diffHours <= 72) {
      return {
        stageId: nextStage.id,
        stageName: nextStage.name,
        orderIndex: nextStage.orderIndex,
        deadlineAt,
        quickAction: nextStage.startedAt ? 'complete' : 'start',
        urgency: 'soon',
        urgencyLabel: 'Ate 3 dias',
        urgencyWeight: 1,
        summary: `${nextStage.name} ${deadlineLabel} (${formattedDeadline}).`,
      };
    }

    return {
      stageId: nextStage.id,
      stageName: nextStage.name,
      orderIndex: nextStage.orderIndex,
      deadlineAt,
      quickAction: nextStage.startedAt ? 'complete' : 'start',
      urgency: 'upcoming',
      urgencyLabel: 'No radar',
      urgencyWeight: 4,
      summary: `${nextStage.name} ${deadlineLabel} (${formattedDeadline}).`,
    };
  }

  private buildNextAction(
    application: ApplicationResponse,
    stageAttention: StageAttention,
  ): Pick<ApplicationCard, 'nextActionTitle' | 'nextActionSummary'> {
    const status = application.status;

    if (status === 'HIRED') {
      return {
        nextActionTitle: 'Fechar ciclo',
        nextActionSummary:
          'Registrar os aprendizados finais, consolidar a historia e arquivar a candidatura como conquista.',
      };
    }

    if (status === 'REJECTED') {
      return {
        nextActionTitle: 'Registrar feedback',
        nextActionSummary:
          'Anote o que aprendeu com esta rejeicao e mantenha o historico para ajustar as proximas candidaturas.',
      };
    }

    if (status === 'WITHDRAWN') {
      return {
        nextActionTitle: 'Documentar desistência',
        nextActionSummary:
          'Vale registrar por que voce saiu do processo para nao perder contexto depois.',
        };
    }

    if (status === 'ACTIVE' && application.nextAction) {
      const dueAt = application.nextActionDueAt;
      const dueSummary = dueAt
        ? `${this.deadlineDistanceLabel(dueAt)} (${this.formatDateTime(dueAt)}).`
        : 'Sem data definida por enquanto.';
      const urgencyWeight = this.manualNextActionUrgencyWeight(application);

      if (urgencyWeight === 0) {
        return {
          nextActionTitle: 'Próxima ação atrasada',
          nextActionSummary: `${application.nextAction}. ${dueSummary}`,
        };
      }

      if (urgencyWeight === 1) {
        return {
          nextActionTitle: 'Próxima ação em foco',
          nextActionSummary: `${application.nextAction}. ${dueSummary}`,
        };
      }

      return {
        nextActionTitle: 'Próxima ação registrada',
        nextActionSummary: `${application.nextAction}. ${dueSummary}`,
      };
    }

    switch (stageAttention.urgency) {
      case 'overdue':
        return {
          nextActionTitle: 'Retomar agora',
          nextActionSummary:
            'O prazo da proxima etapa ja venceu. Atualize a etapa, cobre retorno ou redefina o plano desta candidatura.',
        };
      case 'soon':
        return {
          nextActionTitle: 'Preparar entrega',
          nextActionSummary:
            'A proxima etapa vence nos proximos dias. Priorize preparacao, revisao de materiais e confirmacao do horario.',
        };
      case 'unscheduled':
        return {
          nextActionTitle: 'Planejar proximo passo',
          nextActionSummary:
            'Defina a proxima etapa ou atribua um prazo para evitar que a candidatura fique sem direcao.',
        };
      case 'started':
        return {
          nextActionTitle: 'Atualizar andamento',
          nextActionSummary:
            'Existe uma etapa em andamento sem prazo claro. Vale registrar prazo, nota ou novo status para manter o fluxo confiavel.',
        };
      case 'upcoming':
        return {
          nextActionTitle: 'Manter no radar',
          nextActionSummary:
            'A proxima etapa ja esta planejada. Use este espaco para acompanhar preparo, materiais e proximas confirmacoes.',
        };
      default:
        return {
          nextActionTitle: 'Revisar historico',
          nextActionSummary:
            'Confira se as notas e etapas representam bem o momento atual antes de seguir para a proxima movimentacao.',
        };
    }
  }

  private buildLatestActivity(
    stages: StageResponse[],
    notes: NoteResponse[],
  ): LatestActivity {
    const events: LatestActivity[] = [];

    notes.forEach((note) => {
      events.push({
        happenedAt: note.createdAt,
        title: 'Última nota registrada',
        summary: this.truncateText(note.content, 120),
      });
    });

    stages.forEach((stage) => {
      if (stage.completedAt) {
        events.push({
          happenedAt: stage.completedAt,
          title: 'Etapa concluída',
          summary: `${stage.name} foi concluida e ja entrou no historico da candidatura.`,
        });
      }

      if (stage.startedAt) {
        events.push({
          happenedAt: stage.startedAt,
          title: 'Etapa em andamento',
          summary: `${stage.name} ja foi iniciada e esta no fluxo ativo da candidatura.`,
        });
      }
    });

    if (events.length === 0) {
      return {
        happenedAt: null,
        title: 'Sem movimentacao recente',
        summary:
          'Registre uma nota rápida ou avance uma etapa para manter o contexto desta candidatura.',
      };
    }

    return events.sort(
      (left, right) =>
        new Date(right.happenedAt!).getTime() - new Date(left.happenedAt!).getTime(),
    )[0];
  }

  private getLatestNote(notes: NoteResponse[]): NoteResponse | null {
    if (notes.length === 0) {
      return null;
    }

    return [...notes].sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime(),
    )[0];
  }

  private buildLatestNotePreview(notes: NoteResponse[]): string | null {
    const latestNote = this.getLatestNote(notes);

    if (!latestNote) {
      return null;
    }

    return this.truncateText(latestNote.content, 140);
  }

  private compareApplications(left: ApplicationCard, right: ApplicationCard): number {
    const statusPriority: Record<ApplicationStatus, number> = {
      ACTIVE: 0,
      HIRED: 1,
      WITHDRAWN: 2,
      REJECTED: 3,
    };

    const statusDifference = statusPriority[left.status] - statusPriority[right.status];

    if (statusDifference !== 0) {
      return statusDifference;
    }

    const urgencyDifference =
      this.effectiveUrgencyWeight(left) - this.effectiveUrgencyWeight(right);

    if (urgencyDifference !== 0) {
      return urgencyDifference;
    }

    const deadlineDifference = this.compareOptionalDates(
      this.nextRelevantDate(left),
      this.nextRelevantDate(right),
    );

    if (deadlineDifference !== 0) {
      return deadlineDifference;
    }

    return left.company.localeCompare(right.company) || left.title.localeCompare(right.title);
  }

  private compareOptionalDates(left: string | null, right: string | null): number {
    if (left && right) {
      return new Date(left).getTime() - new Date(right).getTime();
    }

    if (left) {
      return -1;
    }

    if (right) {
      return 1;
    }

    return 0;
  }

  private effectiveUrgencyWeight(application: ApplicationCard): number {
    const manualUrgencyWeight = this.manualNextActionUrgencyWeight(application);

    if (manualUrgencyWeight === null) {
      return application.stageAttention.urgencyWeight;
    }

    return Math.min(application.stageAttention.urgencyWeight, manualUrgencyWeight);
  }

  private manualNextActionUrgencyWeight(application: ApplicationResponse): number | null {
    if (!application.nextAction || !application.nextActionDueAt || application.status !== 'ACTIVE') {
      return null;
    }

    const diffHours =
      (new Date(application.nextActionDueAt).getTime() - Date.now()) / (1000 * 60 * 60);

    if (diffHours < 0) {
      return 0;
    }

    if (diffHours <= 72) {
      return 1;
    }

    return 4;
  }

  private matchesTriageView(
    application: ApplicationCard,
    triageView: DashboardTriageView,
  ): boolean {
    if (triageView === 'all') {
      return true;
    }

    if (application.status !== 'ACTIVE') {
      return false;
    }

    switch (triageView) {
      case 'attention':
        return (
          this.effectiveUrgencyWeight(application) <= 2 ||
          this.manualNextActionUrgencyWeight(application) === 0
        );
      case 'missing-next-action':
        return !application.nextAction;
      case 'missing-stages':
        return application.stagesCount === 0;
      case 'stalled':
        return this.isStalled(application);
      default:
        return true;
    }
  }

  private isStalled(application: ApplicationCard): boolean {
    if (application.latestActivity.happenedAt === null) {
      return true;
    }

    const diffDays =
      (Date.now() - new Date(application.latestActivity.happenedAt).getTime()) /
      (1000 * 60 * 60 * 24);

    return diffDays >= 7;
  }

  private nextRelevantDate(application: ApplicationCard): string | null {
    if (application.nextActionDueAt && application.stageAttention.deadlineAt) {
      return new Date(application.nextActionDueAt).getTime() <=
        new Date(application.stageAttention.deadlineAt).getTime()
        ? application.nextActionDueAt
        : application.stageAttention.deadlineAt;
    }

    return application.nextActionDueAt ?? application.stageAttention.deadlineAt;
  }

  private deadlineDistanceLabel(deadlineAt: string): string {
    const diffHours = (new Date(deadlineAt).getTime() - Date.now()) / (1000 * 60 * 60);

    if (diffHours < 0) {
      const overdueDays = Math.ceil(Math.abs(diffHours) / 24);
      return overdueDays <= 1 ? 'venceu hoje' : `venceu ha ${overdueDays} dias`;
    }

    if (diffHours < 24) {
      return 'vence hoje';
    }

    const dueDays = Math.ceil(diffHours / 24);

    return `vence em ${dueDays} dia(s)`;
  }

  private formatDateTime(dateTime: string): string {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(dateTime));
  }

  private truncateText(value: string, maxLength: number): string {
    if (value.length <= maxLength) {
      return value;
    }

    return `${value.slice(0, maxLength).trimEnd()}...`;
  }

  private normalizeOptionalText(value: string): string | null {
    const normalizedValue = value.trim();
    return normalizedValue.length > 0 ? normalizedValue : null;
  }

  private toOptionalDateTime(value: string): string | null {
    if (!value) {
      return null;
    }

    return value.length === 16 ? `${value}:00` : value;
  }

  private toDateTimeLocalValue(value: string | null): string {
    if (!value) {
      return '';
    }

    return value.slice(0, 16);
  }

  private currentLocalDateTime(): string {
    const now = new Date();
    const parts = [
      now.getFullYear(),
      String(now.getMonth() + 1).padStart(2, '0'),
      String(now.getDate()).padStart(2, '0'),
    ];
    const time = [
      String(now.getHours()).padStart(2, '0'),
      String(now.getMinutes()).padStart(2, '0'),
      String(now.getSeconds()).padStart(2, '0'),
    ];

    return `${parts[0]}-${parts[1]}-${parts[2]}T${time[0]}:${time[1]}:${time[2]}`;
  }
}
