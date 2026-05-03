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
  isLoading = false;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers(page = this.currentPage) {
    this.isLoading = true;
    this.apiService.getUsersPaged(page).subscribe({
      next: (data) => {
        this.users = data.content ?? [];
        this.currentPage = data.number ?? 0;
        this.totalPages = data.totalPages ?? 1;
        this.totalElements = data.totalElements ?? this.users.length;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => { console.error('Failed to load users', err); this.isLoading = false; }
    });
  }

  applyFilter() {
    const t = this.searchTerm.toLowerCase();
    this.filteredUsers = this.users.filter(u => u.email.includes(t) || u.roleType.toLowerCase().includes(t));
  }

  prevPage() { if (this.currentPage > 0) this.loadUsers(this.currentPage - 1); }
  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadUsers(this.currentPage + 1); }

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
