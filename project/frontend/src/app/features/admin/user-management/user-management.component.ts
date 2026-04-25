import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: []
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  searchTerm = '';
  showModal = false;
  newUser: any = { email: '', roleType: 'INDIVIDUAL', password: '' };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.apiService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = data;
      },
      error: (err) => console.error('Failed to load users', err)
    });
  }

  applyFilter() {
    const t = this.searchTerm.toLowerCase();
    this.filteredUsers = this.users.filter(u => u.email.includes(t) || u.roleType.toLowerCase().includes(t));
  }

  toggleStatus(user: any) {
    // Ideally this would hit a backend endpoint to toggle status.
    user.status = user.status === 'Active' ? 'Suspended' : 'Active';
  }

  deleteUser(id: number) {
    if (confirm('Are you sure you want to delete this user?')) {
      this.apiService.deleteUser(id).subscribe({
        next: () => this.loadUsers(),
        error: (err) => alert('Failed to delete user')
      });
    }
  }

  createUser() {
    this.apiService.register(this.newUser.email, this.newUser.password, this.newUser.roleType).subscribe({
      next: () => {
        this.loadUsers();
        this.newUser = { email: '', roleType: 'INDIVIDUAL', password: '' };
        this.showModal = false;
      },
      error: (err) => alert('Failed to create user')
    });
  }
}
