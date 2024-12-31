import { Component, OnInit } from '@angular/core';
import { ApiService, FileDetails } from '../api.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';

interface TreeNode extends FileDetails {
    children?: TreeNode[];
    isExpanded?: boolean;
    level?: number;
}

@Component({
    selector: 'app-project-view',
    templateUrl: './project-view.component.html',
    styleUrls: ['./project-view.component.css'],
})
export class ProjectViewComponent implements OnInit {
    projectName: string | null = null;
    files: FileDetails[] = [];
    loading = false;
    selectedFilePath: string = '';
    fileContent: string = '';
    error: string = '';
    currentPath: string = '';
    treeNodes: TreeNode[] = [];

    constructor(
        private apiService: ApiService,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location
    ) { }

    ngOnInit(): void {
        this.route.paramMap.subscribe((params) => {
            this.projectName = params.get('projectName');
            if (this.projectName) {
                this.loadFiles();
            }
        });
    }

    private buildFileTree(files: FileDetails[]): TreeNode[] {
        const root: TreeNode[] = [];
        
        files.forEach(file => {
            const parts = file.path.split('/');
            let currentLevel = root;
            
            parts.forEach((part, index) => {
                const existing = currentLevel.find(node => node.name === part);
                if (existing) {
                    currentLevel = existing.children!;
                } else {
                    const isFile = !file.isDirectory && index === parts.length - 1;
                    const newNode: TreeNode = {
                        ...file,
                        name: part,
                        path: parts.slice(0, index + 1).join('/'),
                        isDirectory: !isFile,  // If it's not the last part or it's marked as directory
                        children: isFile ? undefined : [],
                        isExpanded: false,
                        level: index
                    };
                    currentLevel.push(newNode);
                    if (newNode.children) {
                        currentLevel = newNode.children;
                    }
                }
            });
        });
        
        // Sort nodes: folders first, then files, both alphabetically
        const sortNodes = (nodes: TreeNode[]) => {
            nodes.sort((a, b) => {
                if (a.isDirectory === b.isDirectory) {
                    return a.name.localeCompare(b.name);
                }
                return a.isDirectory ? -1 : 1;
            });
            
            nodes.forEach(node => {
                if (node.children) {
                    sortNodes(node.children);
                }
            });
        };
        
        sortNodes(root);
        return root;
    }

    loadFiles() {
        if (this.projectName) {
            this.loading = true;
            this.apiService.listFiles(this.projectName, this.currentPath).subscribe({
                next: (fileDetails) => {
                    // Store all files for the tree structure
                    if (this.currentPath === '') {
                        // If we're at root, rebuild the entire tree
                        this.files = fileDetails;
                        this.treeNodes = this.buildFileTree(fileDetails);
                    } else {
                        // If we're in a subfolder, update the existing tree
                        const parentNode = this.findNodeByPath(this.treeNodes, this.currentPath);
                        if (parentNode && parentNode.children) {
                            parentNode.children = fileDetails.map(file => ({
                                ...file,
                                isDirectory: file.isDirectory,
                                children: file.isDirectory ? [] : undefined,
                                isExpanded: false,
                                level: parentNode.level! + 1
                            }));
                        }
                    }
                    this.loading = false;
                },
                error: (err) => {
                    this.loading = false;
                    console.error('Error loading files', err);
                },
            });
        }
    }

    // Add this helper method to find nodes in the tree
    private findNodeByPath(nodes: TreeNode[], path: string): TreeNode | null {
        for (const node of nodes) {
            if (node.path === path) {
                return node;
            }
            if (node.children) {
                const found = this.findNodeByPath(node.children, path);
                if (found) {
                    return found;
                }
            }
        }
        return null;
    }

    selectFile(path: string) {
        this.selectedFilePath = path;
    }

    navigateToTestGenerate(): void {
        if (this.projectName) {
            this.router.navigate(['/test-generate', this.projectName]);
        }
    }

    goBack(): void {
        this.location.back();
    }

    viewFile(node: TreeNode): void {
        if (!this.projectName) return;
        
        if (node.isDirectory) {
            node.isExpanded = !node.isExpanded;
            if (node.isExpanded && (!node.children || node.children.length === 0)) {
                this.currentPath = node.path;
                this.loadFiles();
            }
            return;
        }

        this.selectedFilePath = node.path;
        this.loading = true;
        this.error = '';

        this.apiService.readFile(this.projectName, node.path).subscribe({
            next: (content) => {
                this.fileContent = content;
                this.loading = false;
                this.error = '';
            },
            error: (err) => {
                console.error('Error reading file:', err);
                this.error = err.message || 'Failed to read file contents';
                this.loading = false;
                this.fileContent = '';  // Clear any previous content
            }
        });
    }

    handlePathChange(newPath: string) {
        this.currentPath = newPath;
        this.loadFiles();
    }

    refresh(): void {
        if (!this.projectName) return;
        
        this.loading = true;
        this.error = '';

        this.apiService.listFiles(this.projectName, this.currentPath).subscribe({
            next: (files: FileDetails[]) => {
                this.files = files;
                this.loading = false;
            },
            error: (err: Error) => {
                this.error = 'Failed to load project files';
                this.loading = false;
            }
        });
    }

    toggleNode(node: TreeNode) {
        node.isExpanded = !node.isExpanded;
    }
}
