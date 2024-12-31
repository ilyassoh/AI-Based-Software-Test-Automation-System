export interface FileNode {
    name: string;
    type: 'file' | 'folder';
    children?: FileNode[];
    isExpanded?: boolean;
} 