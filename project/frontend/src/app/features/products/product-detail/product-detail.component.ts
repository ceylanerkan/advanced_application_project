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

    this.apiService.getReviews().subscribe({
      next: (allReviews) => {
        // Filter reviews for this product
        this.reviews = allReviews.filter((r: any) => r.product?.id === id);
      },
      error: (err) => console.error('Failed to load reviews', err)
    });
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
    
    this.apiService.createReview({
      product: { id: this.product.id },
      starRating: this.newRating,
      comment: this.newComment,
      sentiment: 'NEUTRAL'
    }).subscribe({
      next: (savedReview) => {
        this.reviews.unshift(savedReview);
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
