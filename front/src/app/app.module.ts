import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { GitCloneComponent } from './git-clone/git-clone.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { FileViewComponent } from './file-view/file-view.component';
import { TestGenerateComponent } from './test-generate/test-generate.component';

@NgModule({
  declarations: [
    AppComponent,
    GitCloneComponent,
    ProjectViewComponent,
    FileViewComponent,
    TestGenerateComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
