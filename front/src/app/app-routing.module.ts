import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GitCloneComponent } from './git-clone/git-clone.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { TestGenerateComponent } from './test-generate/test-generate.component';

const routes: Routes = [
  { path: 'clone', component: GitCloneComponent },
  { path: 'project/:projectName', component: ProjectViewComponent },
  { path: 'test-generate/:projectName', component: TestGenerateComponent },
  { path: '', redirectTo: '/clone', pathMatch: 'full' },
  { path: '**', redirectTo: '/clone' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
