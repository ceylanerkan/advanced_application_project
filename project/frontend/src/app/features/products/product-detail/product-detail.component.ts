import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../../core/services/api.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrls: []
})
export class ProductDetailComponent implements OnInit {
  product: any = null;
  productImageUrl: string | null = null;
  reviews: any[] = [];
  newRating = 5;
  newComment = '';
  quantity = 1;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.apiService.getProduct(id).subscribe({
      next: (data) => {
        this.product = data;
        if (!this.product.rating) {
          this.product.rating = this.product.averageRating || 0;
        }
        this.fetchProductImage();
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
        // Reload product so the updated averageRating is reflected immediately
        this.apiService.getProduct(this.product.id).subscribe(p => {
          this.product.rating = p.averageRating || 0;
        });
        alert('Review submitted!');
      },
      error: (err) => {
        console.error('Failed to submit review', err);
        alert('Failed to submit review.');
      }
    });
  }

  fetchProductImage() {
    const p = this.product;
    // Use existing DB URL if it's not a broken source.unsplash.com link
    if (p.imageUrl && !p.imageUrl.includes('source.unsplash.com')) {
      this.productImageUrl = p.imageUrl;
      return;
    }

    const key = environment.unsplashKey;
    if (!key || key === 'YOUR_UNSPLASH_ACCESS_KEY') return;

    const query = p.name
      .split(/\s+/)
      .filter((w: string) => w.length > 1 && !/^[A-Z0-9]*\d[A-Z0-9-]*$/.test(w))
      .slice(0, 4)
      .join(' ') || p.category?.name || p.name;

    this.http.get<any>(
      `https://api.unsplash.com/search/photos?query=${encodeURIComponent(query)}&per_page=1&client_id=${key}`
    ).subscribe({
      next: res => {
        const url = res.results?.[0]?.urls?.regular;
        if (url) this.productImageUrl = url;
      },
      error: () => {}
    });
  }

  goBack() {
    this.router.navigate(['/shop']);
  }
}
