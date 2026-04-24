import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, User } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  role: 'ADMIN' | 'CORPORATE' | 'INDIVIDUAL' = 'ADMIN';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(event: Event) {
    event.preventDefault();
    
    // Demo login logic simulating backend response
    const mockUser: User = {
      id: 1,
      email: this.email,
      role: this.role,
      token: 'mock-jwt-token-for-demo'
    };

    this.authService.login(mockUser);
    
    // Redirect based on role
    switch (this.role) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'CORPORATE':
        this.router.navigate(['/corporate']);
        break;
      case 'INDIVIDUAL':
        this.router.navigate(['/individual']);
        break;
    }
  }
}
