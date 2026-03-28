import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
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
  UserSkillResponse,
} from '../core/api/models';
import { AuthStore } from '../core/auth/auth.store';

interface ApplicationCard extends ApplicationResponse {
  company: string;
  title: string;
  location: string | null;
  seniority: string | null;
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

  protected readonly errorMessage = signal<string | null>(null);

  protected readonly successMessage = signal<string | null>(null);

  protected readonly companyFilter = signal('');

  protected readonly statusFilter = signal<ApplicationStatus | ''>('');

  protected readonly jobs = signal<JobResponse[]>([]);

  protected readonly skills = signal<SkillResponse[]>([]);

  protected readonly userSkills = signal<UserSkillResponse[]>([]);

  protected readonly applications = signal<ApplicationResponse[]>([]);

  protected readonly knownUserId = computed(() => this.auth.knownUserId());

  protected readonly levelOptions = [1, 2, 3, 4, 5];

  protected readonly statusOptions: ApplicationStatus[] = [
    'ACTIVE',
    'HIRED',
    'REJECTED',
    'WITHDRAWN',
  ];

  protected readonly applicationCards = computed<ApplicationCard[]>(() => {
    const jobsById = new Map(this.jobs().map((job) => [job.id, job]));

    return this.applications().map((application) => {
      const job = jobsById.get(application.jobId);

      return {
        ...application,
        company: job?.company ?? 'Empresa não encontrada',
        title: job?.title ?? 'Vaga sem título',
        location: job?.location ?? null,
        seniority: job?.seniority ?? null,
      };
    });
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

  protected readonly metrics = computed(() => {
    const applications = this.applications();

    return {
      totalApplications: applications.length,
      activeApplications: applications.filter((item) => item.status === 'ACTIVE').length,
      totalJobs: this.jobs().length,
      totalUserSkills: this.userSkills().length,
      trackedUserId: this.knownUserId(),
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
    }).subscribe({
      next: ({ jobs, skills, applications, userSkills }) => {
        this.jobs.set(jobs);
        this.skills.set(skills);
        this.applications.set(applications);
        this.userSkills.set(userSkills);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(
          toErrorMessage(error, 'Não foi possível carregar o workspace.'),
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

    this.api.createJob(this.createJobForm.getRawValue() as CreateJobRequest).subscribe({
      next: (job) => {
        this.isCreatingJob.set(false);
        this.createJobForm.reset({
          company: '',
          title: '',
          sourceUrl: '',
          seniority: '',
          location: '',
          description: '',
        });
        this.successMessage.set(`Vaga "${job.title}" registrada com sucesso.`);
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.isCreatingJob.set(false);
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel registrar a vaga.'),
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

    this.api
      .createApplication(
        this.createApplicationForm.getRawValue() as CreateApplicationRequest,
      )
      .subscribe({
        next: () => {
          this.isCreatingApplication.set(false);
          this.createApplicationForm.patchValue({
            jobId: '',
            userId: this.knownUserId() ?? this.createApplicationForm.getRawValue().userId,
          });
          this.successMessage.set(
            'Candidatura registrada. Abra o detalhe para acompanhar o andamento.',
          );
          this.loadWorkspace();
        },
        error: (error: unknown) => {
          this.isCreatingApplication.set(false);
          this.errorMessage.set(
            toErrorMessage(error, 'Nao foi possivel registrar a candidatura.'),
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

    this.errorMessage.set(null);
    this.successMessage.set(null);

    const value = this.createUserSkillForm.getRawValue();
    const request: CreateUserSkillRequest = {
      userId,
      skillId: value.skillId,
      yearsExperience: Number(value.yearsExperience),
      level: Number(value.level),
    };

    this.api.createUserSkill(request).subscribe({
      next: () => {
        this.createUserSkillForm.patchValue({
          skillId: '',
          yearsExperience: 0,
          level: 3,
        });
        this.successMessage.set(
          'Skill vinculada ao seu perfil. O matching agora consegue usar esse dado.',
        );
        this.loadWorkspace();
      },
      error: (error: unknown) => {
        this.errorMessage.set(
          toErrorMessage(error, 'Nao foi possivel vincular a skill ao seu perfil.'),
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
}
