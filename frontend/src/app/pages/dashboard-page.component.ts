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
  CreateSkillRequest,
  CreateUserSkillRequest,
  JobResponse,
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

interface StageAttention {
  stageId: UUID | null;
  stageName: string | null;
  orderIndex: number | null;
  deadlineAt: string | null;
  urgency: StageAttentionUrgency;
  urgencyLabel: string;
  urgencyWeight: number;
  summary: string;
}

interface ApplicationCard extends ApplicationResponse {
  company: string;
  title: string;
  location: string | null;
  seniority: string | null;
  stageAttention: StageAttention;
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

  protected readonly errorMessage = signal<string | null>(null);

  protected readonly successMessage = signal<string | null>(null);

  protected readonly companyFilter = signal('');

  protected readonly statusFilter = signal<ApplicationStatus | ''>('');

  protected readonly jobs = signal<JobResponse[]>([]);

  protected readonly skills = signal<SkillResponse[]>([]);

  protected readonly userSkills = signal<UserSkillResponse[]>([]);

  protected readonly applications = signal<ApplicationResponse[]>([]);

  protected readonly stagesByApplicationId = signal<Record<UUID, StageResponse[]>>({});

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

    return this.applications()
      .map((application) => {
        const job = jobsById.get(application.jobId);
        const stageAttention = this.buildStageAttention(
          stagesByApplicationId[application.id] ?? [],
        );

        return {
          ...application,
          company: job?.company ?? 'Empresa nao encontrada',
          title: job?.title ?? 'Vaga sem titulo',
          location: job?.location ?? null,
          seniority: job?.seniority ?? null,
          stageAttention,
          ...this.buildNextAction(application.status, stageAttention),
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

    return this.applicationCards().filter((application) => {
      const companyMatches = !companyFilter
        ? true
        : application.company.trim().toLowerCase() === companyFilter;

      const statusMatches = !statusFilter ? true : application.status === statusFilter;

      return companyMatches && statusMatches;
    });
  });

  protected readonly hasActiveFilters = computed(
    () => this.companyFilter().length > 0 || this.statusFilter().length > 0,
  );

  protected readonly priorityApplicationCards = computed(() =>
    this.applicationCards()
      .filter((application) => application.status === 'ACTIVE')
      .filter((application) => application.stageAttention.urgencyWeight <= 3)
      .slice(0, 4),
  );

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
  });

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
            });
          }

          return forkJoin(
            applications.map((application) => this.api.getStagesByApplicationId(application.id)),
          ).pipe(
            map((stagesCollection) => ({
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
            })),
          );
        }),
      )
      .subscribe({
        next: ({ jobs, skills, applications, userSkills, stagesByApplicationId }) => {
          this.jobs.set(jobs);
          this.skills.set(skills);
          this.applications.set(applications);
          this.userSkills.set(userSkills);
          this.stagesByApplicationId.set(stagesByApplicationId);
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
    const editingApplicationId = this.editingApplicationId();
    const operation$ = editingApplicationId
      ? this.api.updateApplication(editingApplicationId, value as UpdateApplicationRequest)
      : this.api.createApplication(value as CreateApplicationRequest);

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
    });
  }

  protected cancelApplicationEditing(): void {
    this.clearApplicationEditing();
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

  protected clearFilters(): void {
    this.companyFilter.set('');
    this.statusFilter.set('');
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
      urgency: 'upcoming',
      urgencyLabel: 'No radar',
      urgencyWeight: 4,
      summary: `${nextStage.name} ${deadlineLabel} (${formattedDeadline}).`,
    };
  }

  private buildNextAction(
    status: ApplicationStatus,
    stageAttention: StageAttention,
  ): Pick<ApplicationCard, 'nextActionTitle' | 'nextActionSummary'> {
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
      left.stageAttention.urgencyWeight - right.stageAttention.urgencyWeight;

    if (urgencyDifference !== 0) {
      return urgencyDifference;
    }

    const deadlineDifference = this.compareOptionalDates(
      left.stageAttention.deadlineAt,
      right.stageAttention.deadlineAt,
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
}
