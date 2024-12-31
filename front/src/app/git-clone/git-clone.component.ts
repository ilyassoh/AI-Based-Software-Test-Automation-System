import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-git-clone',
    templateUrl: './git-clone.component.html',
    styleUrls: ['./git-clone.component.css'],
})
export class GitCloneComponent implements OnInit {
    gitUrl = '';
    projects: string[] = [];
    message = '';
    loading = false;

    constructor(private apiService: ApiService, private router: Router, private http: HttpClient) {
        this.loadProjects();
    }

    ngOnInit() {
        this.apiService.listProjects().subscribe({
            next: (projectNames: string[]) => (this.projects = projectNames),
            error: (err: HttpErrorResponse) => {
                console.error('Error loading projects', err);
            }
        });
    }

    private loadProjects() {
        this.apiService.listProjects().subscribe({
            next: (projectNames) => (this.projects = projectNames),
            error: (err) => {
                console.error('Error loading projects', err);
            },
        });
    }

    cloneRepository() {
        if (!this.gitUrl) {
            this.message = 'Please enter a Git URL.';
            return;
        }
        this.loading = true;
        this.message = 'Cloning repository...';
        this.apiService.cloneRepo(this.gitUrl).subscribe({
            next: (response: any) => {
                this.loading = false;
                this.message = 'Repository cloned successfully!';
                setTimeout(() => {
                    this.message = '';
                }, 3000);
                this.loadProjects();
            },
            error: (err: HttpErrorResponse) => {
                this.loading = false;
                this.message = 'Failed to clone repository.';
                console.error('Error cloning repository:', err);
            },
        });
    }

    navigateToProject(projectName: string) {
        this.router.navigate(['/project', projectName]);
    }

    navigateToTestGenerate(projectName: string) {
        this.router.navigate(['/test-generate', projectName]);
    }

    viewProject(project: string) {
        this.router.navigate(['/projects', project]);
    }

    downloadProject(projectName: string) {
        this.apiService.downloadProject(projectName).subscribe(response => {
            const blob = new Blob([response], { type: 'application/zip' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `${projectName}.zip`;
            link.click();
            window.URL.revokeObjectURL(url);
        });
    }
}
