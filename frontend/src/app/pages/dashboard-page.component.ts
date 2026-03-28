import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { toErrorMessage } from '../core/api/error.utils';
import { JobTrackerApiService } from '../core/api/job-tracker-api.service';
import {
  ApplicationResponse,
  CreateApplicationRequest,
  CreateJobRequest,
  JobResponse,
} from '../core/api/models';
import { AuthStore } from '../core/auth/auth.store';

interface ApplicationCard extends ApplicationResponse {
  company: string;
  title: string;
  location: string | null;
  seniority: string | null;
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

  protected readonly jobs = signal<JobResponse[]>([]);

  protected readonly applications = signal<ApplicationResponse[]>([]);

  protected readonly knownUserId = computed(() => this.auth.knownUserId());

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

  protected readonly metrics = computed(() => {
    const applications = this.applications();

    return {
      totalApplications: applications.length,
      activeApplications: applications.filter((item) => item.status === 'ACTIVE').length,
      totalJobs: this.jobs().length,
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
      applications: this.api.getApplications(),
    }).subscribe({
      next: ({ jobs, applications }) => {
        this.jobs.set(jobs);
        this.applications.set(applications);
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

  protected statusClass(status: string): string {
    return `status-pill status-${status.toLowerCase()}`;
  }
}
