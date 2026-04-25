import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  ngOnInit() {
    this.reviews = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      user: `user${i + 1}@example.com`,
      product: `Product ${i + 1}`,
      starRating: Math.floor(Math.random() * 3) + 3,
      sentiment: ['POSITIVE', 'NEUTRAL', 'NEGATIVE'][i % 3],
      createdAt: `2026-04-${String(10 + i).padStart(2, '0')}`,
      reply: i < 3 ? 'Thank you for your feedback!' : null
    }));
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
