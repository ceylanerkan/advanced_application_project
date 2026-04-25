import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrls: []
})
export class ProductDetailComponent implements OnInit {
  product: any = null;
  reviews: any[] = [];
  newRating = 5;
  newComment = '';
  quantity = 1;

  constructor(private route: ActivatedRoute, private router: Router, private apiService: ApiService) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    
    this.apiService.getProduct(id).subscribe({
      next: (data) => {
        this.product = data;
        // Mock rating until reviews API is fully fleshed out on frontend side
        if (!this.product.rating) {
          this.product.rating = (Math.random() * 2 + 3).toFixed(1);
        }
      },
      error: (err) => {
        console.error('Error fetching product', err);
        alert('Failed to load product details.');
      }
    });

    // We can fetch actual reviews here if the endpoint gets added, for now mock:
    this.reviews = [
      { user: 'John D.', rating: 5, comment: 'Excellent quality!', date: '2026-03-15' },
      { user: 'Sarah M.', rating: 4, comment: 'Good value for money.', date: '2026-03-10' },
      { user: 'Alex K.', rating: 4, comment: 'Very satisfied with purchase.', date: '2026-02-28' }
    ];
  }

  getStars(rating: number | string): string {
    const num = Number(rating);
    return '★'.repeat(Math.round(num || 0)) + '☆'.repeat(5 - Math.round(num || 0));
  }

  addToCart() {
    if (!this.product) return;
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const existing = cart.find((i: any) => i.id === this.product.id);
    if (existing) {
      existing.quantity += this.quantity;
    } else {
      cart.push({ ...this.product, quantity: this.quantity });
    }
    localStorage.setItem('cart', JSON.stringify(cart));
    alert('Added to cart!');
  }

  submitReview() {
    if (!this.newComment.trim() || !this.product) return;
    // Real implementation would post to ApiService.createReview()
    this.apiService.createReview({
      product: { id: this.product.id },
      starRating: this.newRating,
      sentiment: 'NEUTRAL',
      // The user details will be handled by backend token
    }).subscribe({
      next: () => {
        this.reviews.unshift({
          user: 'You',
          rating: this.newRating,
          comment: this.newComment,
          date: new Date().toISOString().split('T')[0]
        });
        this.newComment = '';
        this.newRating = 5;
        alert('Review submitted!');
      },
      error: (err) => {
        console.error('Failed to submit review', err);
        alert('Failed to submit review.');
      }
    });
  }

  goBack() {
    this.router.navigate(['/shop']);
  }
}
