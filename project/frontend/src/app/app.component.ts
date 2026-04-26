import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, SidebarComponent],
  templateUrl: './app.component.html',
  styles: [`
    .app-layout {
      display: flex;
      min-height: 100vh;
    }
    .main-content {
      flex: 1;
      overflow-y: auto;
      transition: margin-left 0.3s ease;
    }
    .main-content.with-sidebar {
      margin-left: 260px;
    }
  `]
})
export class AppComponent {
  isLoggedIn = false;

  constructor(private auth: AuthService) {
    this.auth.currentUser$.subscribe(u => this.isLoggedIn = !!u);
  }
}
