import { Component, Input, OnChanges } from '@angular/core';
import { ApiService } from '../api.service';
import * as Prism from 'prismjs';
import 'prismjs/components/prism-java';
import { Location } from '@angular/common';

@Component({
    selector: 'app-file-view',
    template: `
        <div class="file-view">
            <!-- Navigation Header -->
            <div class="file-header mb-3">
                <button class="btn btn-outline-secondary btn-sm" (click)="goBack()">
                    <i class="bi bi-arrow-left"></i> Back
                </button>
                <span class="ms-3 file-path">{{filePath}}</span>
            </div>

            <!-- Content Card -->
            <div class="content-card">
                <div *ngIf="loading" class="text-center p-4">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>

                <div *ngIf="error" class="alert alert-danger">
                    {{ error }}
                </div>

                <div *ngIf="!loading && !error && fileContent" class="code-container">
                    <pre><code [class]="'language-' + (isJavaFile ? 'java' : 'plaintext')" [innerHTML]="highlightedContent"></code></pre>
                </div>

                <!-- Debug info -->
                <div *ngIf="!loading && !fileContent && !error" class="alert alert-info">
                    Waiting for file content...
                    <br>
                    Project Name: {{projectName}}
                    <br>
                    File Path: {{filePath}}
                </div>
            </div>
        </div>
    `,
    styleUrls: ['./file-view.component.css']
})
export class FileViewComponent implements OnChanges {
    @Input() projectName: string | null = null;
    @Input() filePath: string = '';
    
    fileContent: string = '';
    highlightedContent: string = '';
    loading: boolean = false;
    error: string = '';
    isJavaFile: boolean = false;

    constructor(
        private apiService: ApiService,
        private location: Location
    ) {}

    ngOnChanges() {
        console.log('FileView inputs changed:', { projectName: this.projectName, filePath: this.filePath });
        this.loadFileContent();
    }

    loadFileContent() {
        if (!this.projectName || !this.filePath) {
            console.log('Missing required props:', { projectName: this.projectName, filePath: this.filePath });
            return;
        }

        console.log('Loading file content for:', { projectName: this.projectName, filePath: this.filePath });
        this.loading = true;
        this.error = '';
        
        this.apiService.getFileContent(this.projectName, this.filePath).subscribe({
            next: (content) => {
                console.log('Content received, length:', content.length);
                this.fileContent = content;
                this.isJavaFile = this.filePath.endsWith('.java');
                
                try {
                    if (this.isJavaFile) {
                        this.highlightedContent = Prism.highlight(
                            content,
                            Prism.languages['java'],
                            'java'
                        );
                    } else {
                        this.highlightedContent = content;
                    }
                } catch (e) {
                    console.error('Error highlighting code:', e);
                    this.highlightedContent = content; // Fallback to plain text
                }
                
                this.loading = false;
            },
            error: (err) => {
                console.error('Error loading file:', err);
                this.error = `Failed to load file content: ${err.status} ${err.statusText}`;
                if (err.error) {
                    this.error += ` - ${err.error}`;
                }
                this.loading = false;
            }
        });
    }

    goBack(): void {
        this.location.back();
    }
}
