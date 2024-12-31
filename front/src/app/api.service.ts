import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';

export interface FileDetails {
    name: string;
    type: 'file' | 'directory';
    path: string;
    isFile?: boolean;
    isDirectory?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private apiUrl = 'http://localhost:8080/TESTUNITAI/api';  // Changed port to 8080

    constructor(private http: HttpClient) {}

    listFiles(projectName: string, path?: string): Observable<FileDetails[]> {
        let params = new HttpParams();
        if (path) {
            params = params.set('localPath', path);
        }

        return this.http.get<FileDetails[]>(`${this.apiUrl}/projet/${projectName}`, { params })
            .pipe(
                map((response: any[]) => {
                    return response.map(item => ({
                        name: item.name,
                        type: item.isDirectory ? 'directory' : 'file',
                        path: item.path,
                        isFile: item.isFile,
                        isDirectory: item.isDirectory
                    }));
                })
            );
    }

    getFileContent(projectName: string, filePath: string): Observable<string> {
        console.log('Getting file content for:', { projectName, filePath });
        
        const params = new HttpParams()
            .set('localPath', filePath);
            
        return this.http.get(`${this.apiUrl}/projet/${projectName}/file/read`, {
            params: params,
            responseType: 'text',
            headers: {
                'Accept': 'text/plain'
            }
        }).pipe(
            tap({
                next: () => console.log('File content received'),
                error: (error) => console.error('Error fetching file content:', error)
            })
        );
    }

    listProjects() {
        return this.http.get<string[]>(`${this.apiUrl}/projet/names`);
    }

    cloneRepo(gitUrl: string) {
        return this.http.post<any>(`${this.apiUrl}/git`, { url: gitUrl });
    }

    generateTest(projectName: string) {
        return this.http.post<any>(`${this.apiUrl}/test/Generate/${projectName}`,{});
    }

    downloadProject(projectName: string): Observable<Blob> {
        return this.http.post(`${this.apiUrl}/projet/${projectName}/download`, null, {
            responseType: 'blob'
        });
    }

    readFile(projectName: string, filePath: string): Observable<string> {
        const params = new HttpParams().set('localPath', filePath);
        return this.http.get(`${this.apiUrl}/projet/${projectName}/file/read`, { 
            params,
            responseType: 'text'  // Specify that we expect a text response
        }).pipe(
            catchError((error: HttpErrorResponse) => {
                console.error('Error reading file:', error);
                return throwError(() => new Error(error.error || 'Failed to read file contents'));
            })
        );
    }
}
