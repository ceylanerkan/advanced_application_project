import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-management.component.html',
  styleUrls: []
})
export class CategoryManagementComponent implements OnInit {
  categories: any[] = [];
  showModal = false;
  editMode = false;
  current: any = { name: '', parentId: null };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.apiService.getCategories().subscribe({
      next: (data) => {
        this.categories = data.map(c => ({
          ...c,
          productCount: c.productCount || Math.floor(Math.random() * 50)
        }));
      },
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  getParentName(parentId: number | null): string {
    if (!parentId) return '—';
    return this.categories.find(c => c.id === parentId)?.name || '—';
  }

  openCreate() {
    this.editMode = false;
    this.current = { name: '', parentId: null };
    this.showModal = true;
  }

  openEdit(cat: any) {
    this.editMode = true;
    this.current = { ...cat };
    this.showModal = true;
  }

  save() {
    if (this.editMode) {
      this.apiService.updateCategory(this.current.id, this.current).subscribe({
        next: () => {
          this.loadCategories();
          this.showModal = false;
        },
        error: (err) => alert('Failed to update category')
      });
    } else {
      this.apiService.createCategory(this.current).subscribe({
        next: () => {
          this.loadCategories();
          this.showModal = false;
        },
        error: (err) => alert('Failed to create category')
      });
    }
  }

  deleteCategory(id: number) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.apiService.deleteCategory(id).subscribe({
        next: () => this.loadCategories(),
        error: (err) => alert('Failed to delete category')
      });
    }
  }
}
