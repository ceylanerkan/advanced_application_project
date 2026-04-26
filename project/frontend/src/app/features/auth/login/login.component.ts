import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, User } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  isRegisterMode = false;
  email = '';
  password = '';
  role: 'CORPORATE' | 'INDIVIDUAL' = 'INDIVIDUAL';
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) { }

  toggleMode() {
    this.isRegisterMode = !this.isRegisterMode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.errorMessage = '';
    this.successMessage = '';

    if (this.isRegisterMode) {
      this.apiService.register(this.email, this.password, this.role).subscribe({
        next: () => {
          this.successMessage = 'Registration successful! You can now log in.';
          this.isRegisterMode = false; // Switch back to login
        },
        error: (err) => {
          console.error('Registration failed', err);
          if (err.status === 0) {
            this.errorMessage = 'Network Error (CORS). Make sure backend is running and CORS is configured in SecurityConfig.';
          } else if (err.status === 400) {
            this.errorMessage = 'Validation Failed: Password must be at least 6 characters.';
          } else if (err.status === 409) {
            this.errorMessage = 'This email is already registered.';
          } else {
            this.errorMessage = err.error?.message || 'Failed to register. Please try again later.';
          }
        }
      });
    } else {
      this.apiService.login(this.email, this.password).subscribe({
        next: (res: any) => {
          const user: User = {
            id: 0,
            email: res.email || this.email,
            role: res.role,
            token: res.access_token
          };

          this.authService.login(user);

          switch (user.role) {
            case 'ADMIN':
              this.router.navigate(['/admin']);
              break;
            case 'CORPORATE':
              this.router.navigate(['/corporate']);
              break;
            default:
              this.router.navigate(['/shop']);
              break;
          }
        },
        error: (err) => {
          console.error('Login failed', err);
          this.errorMessage = 'Invalid email or password.';
        }
      });
    }
  }
}
