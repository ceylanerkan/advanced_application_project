import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { AdminDashboardComponent } from './features/dashboards/admin-dashboard/admin-dashboard.component';
import { CorporateDashboardComponent } from './features/dashboards/corporate-dashboard/corporate-dashboard.component';
import { IndividualDashboardComponent } from './features/dashboards/individual-dashboard/individual-dashboard.component';
import { ChatbotUiComponent } from './features/chat/chatbot-ui/chatbot-ui.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'admin', 
    component: AdminDashboardComponent, 
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] } 
  },
  { 
    path: 'corporate', 
    component: CorporateDashboardComponent, 
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] } 
  },
  { 
    path: 'individual', 
    component: IndividualDashboardComponent, 
    canActivate: [authGuard, roleGuard],
    data: { roles: ['INDIVIDUAL'] } 
  },
  { 
    path: 'chat', 
    component: ChatbotUiComponent,
    canActivate: [authGuard]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
