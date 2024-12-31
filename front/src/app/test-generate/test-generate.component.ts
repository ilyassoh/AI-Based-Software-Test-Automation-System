import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Location } from '@angular/common';

@Component({
    selector: 'app-test-generate',
    templateUrl: './test-generate.component.html',
    styleUrls: ['./test-generate.component.css']
})
export class TestGenerateComponent implements OnInit {
    projectName: string | null = null;
    message: string = '';
    loading: boolean = false;

    constructor(
        private route: ActivatedRoute,
        private apiService: ApiService,
        private location: Location
    ) { }

    ngOnInit() {
        this.route.paramMap.subscribe(params => {
            this.projectName = params.get('projectName');
        });
    }

    generateTest() {
        if (!this.projectName) return;
        
        this.loading = true;
        this.message = '';
        
        this.apiService.generateTest(this.projectName).subscribe({
            next: () => {
                this.loading = false;
                this.message = 'Tests generated successfully!';
            },
            error: (error: HttpErrorResponse) => {
                this.loading = false;
                this.message = 'Error generating tests: ' + error.message;
            }
        });
    }

    goBack(): void {
        this.location.back();
    }
}
