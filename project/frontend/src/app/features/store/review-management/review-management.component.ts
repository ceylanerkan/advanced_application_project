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

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getReviews().subscribe({
      next: (data) => this.reviews = data,
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
