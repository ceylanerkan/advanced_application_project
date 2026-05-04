import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-review-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-management.component.html',
  styleUrls: []
})
export class ReviewManagementComponent implements OnInit {
  reviews: any[] = [];
  replyText: { [key: number]: string } = {};

  // Pagination
  currentPage = 1;
  readonly pageSize = 5;

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.reviews.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get pagedReviews(): any[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.reviews.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getMyStoreReviews().subscribe({
      next: (data) => { this.reviews = data; this.currentPage = 1; },
      error: (err) => console.error('Failed to load reviews', err)
    });
  }

  getStars(n: number): string {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }

  submitReply(review: any) {
    if (this.replyText[review.id]?.trim()) {
      review.reply = this.replyText[review.id];
      this.replyText[review.id] = '';
    }
  }
}
