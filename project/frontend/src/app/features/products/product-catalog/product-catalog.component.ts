import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { environment } from '../../../../environments/environment';

interface Product {
  id: number;
  name: string;
  sku: string;
  unitPrice: number;
  imageUrl?: string;
  category: { name: string };
  store: { id: number, name: string };
  rating?: number;
  stock?: number;
}

@Component({
  selector: 'app-product-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-catalog.component.html',
  styleUrls: []
})
export class ProductCatalogComponent implements OnInit {
  Math = Math;
  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: string[] = [];
  searchTerm = '';
  selectedCategory = '';
  sortBy = 'name';
  isLoading = false;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  constructor(private router: Router, private apiService: ApiService, private http: HttpClient) {}

  ngOnInit() {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.isLoading = true;
    this.apiService.getProductsPaged(page).subscribe({
      next: (data) => {
        const items = data.content ?? [];
        this.currentPage = data.number ?? 0;
        this.totalPages = data.totalPages ?? 1;
        this.totalElements = data.totalElements ?? items.length;
        this.products = items.map((p: any) => ({
          ...p,
          rating: p.averageRating || 0,
          stock: p.stock || 0,
          category: p.category ?? { name: 'Uncategorized' }
        }));
        this.categories = [...new Set(this.products.map(p => p.category.name))].filter(Boolean);
        this.fetchUnsplashImages(this.products);
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => { console.error('Failed to load products', err); this.isLoading = false; }
    });
  }

  // Fetch a unique, relevant image from Unsplash for each product
  private fetchUnsplashImages(products: Product[]) {
    const key = environment.unsplashKey;
    if (!key || key === 'YOUR_UNSPLASH_ACCESS_KEY') return;

    products.forEach(p => {
      // Skip products that already have a valid custom image URL
      if (p.imageUrl && !p.imageUrl.includes('source.unsplash.com')) return;

      const query = this.buildSearchQuery(p);
      this.http.get<any>(
        `https://api.unsplash.com/search/photos?query=${encodeURIComponent(query)}&per_page=1&client_id=${key}`
      ).subscribe({
        next: res => {
          const url = res.results?.[0]?.urls?.small;
          if (url) {
            p.imageUrl = url;
            // Rebuild filteredProducts to force Angular to pick up the imageUrl change
            this.filteredProducts = [...this.filteredProducts];
          }
        },
        error: () => {}
      });
    });
  }

  // Strip model/SKU tokens (e.g. "AP3132867", "731670-0010") and take first 4 meaningful words
  private buildSearchQuery(p: Product): string {
    const cleaned = p.name
      .split(/\s+/)
      .filter(w => w.length > 1 && !/^[A-Z0-9]*\d[A-Z0-9-]*$/.test(w))
      .slice(0, 4)
      .join(' ');
    return cleaned || p.category?.name || p.name;
  }

  jumpPage = 1;

  prevPage() { if (this.currentPage > 0) this.loadPage(this.currentPage - 1); }
  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadPage(this.currentPage + 1); }

  goToPage(page: number) {
    const p = Math.max(0, Math.min(Math.floor(page), this.totalPages - 1));
    if (p !== this.currentPage) this.loadPage(p);
  }

  getPageRange(): number[] {
    const total = this.totalPages;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const cur = this.currentPage;
    const start = Math.max(0, Math.min(cur - 2, total - 5));
    return Array.from({ length: 5 }, (_, i) => start + i);
  }

  getStars(rating: number | string): string {
    const num = Number(rating);
    return '★'.repeat(Math.round(num || 0)) + '☆'.repeat(5 - Math.round(num || 0));
  }

  applyFilters() {
    let result = [...this.products];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(p => p.name.toLowerCase().includes(term) || p.sku.toLowerCase().includes(term));
    }

    if (this.selectedCategory) {
      result = result.filter(p => p.category?.name === this.selectedCategory);
    }

    if (this.sortBy === 'name') result.sort((a, b) => a.name.localeCompare(b.name));
    else if (this.sortBy === 'price-asc') result.sort((a, b) => a.unitPrice - b.unitPrice);
    else if (this.sortBy === 'price-desc') result.sort((a, b) => b.unitPrice - a.unitPrice);
    else if (this.sortBy === 'rating') result.sort((a, b) => (Number(b.rating) || 0) - (Number(a.rating) || 0));

    this.filteredProducts = result;
  }

  viewProduct(id: number) {
    this.router.navigate(['/shop', id]);
  }

  addToCart(product: Product, event: Event) {
    event.stopPropagation();
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const existing = cart.find((i: any) => i.id === product.id);
    if (existing) {
      existing.quantity++;
    } else {
      cart.push({ ...product, quantity: 1 });
    }
    localStorage.setItem('cart', JSON.stringify(cart));
    alert(`${product.name} added to cart!`);
  }

  // ── Image helpers ─────────────────────────────────────────────────────────

  private readonly categoryEmojis: Record<string, string> = {
    'Electronics':      '💻',
    'Books':            '📚',
    'Clothing':         '👕',
    'Apparel':          '👕',
    'Furniture':        '🛋️',
    'Major Appliances': '🏠',
    'Toys':             '🧸',
    'Sports':           '⚽',
    'Beauty':           '💄',
    'Food':             '🍎',
  };

  // Category fallback images (used while Unsplash loads or if key is not set)
  private readonly categoryImages: Record<string, string> = {
    'Electronics':      'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400&h=300&fit=crop',
    'Major Appliances': 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop',
    'Furniture':        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&h=300&fit=crop',
    'Apparel':          'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&h=300&fit=crop',
    'Clothing':         'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&h=300&fit=crop',
    'Books':            'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=300&fit=crop',
    'Toys':             'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=400&h=300&fit=crop',
    'Sports':           'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=400&h=300&fit=crop',
    'Beauty':           'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400&h=300&fit=crop',
    'Food':             'https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=400&h=300&fit=crop',
  };

  getImageUrl(p: Product): string {
    // After Unsplash responds, p.imageUrl is a fresh images.unsplash.com URL — use it directly
    if (p.imageUrl && !p.imageUrl.includes('source.unsplash.com')) {
      return p.imageUrl;
    }
    // While Unsplash is loading (or key not set), show a category-relevant fallback
    return this.categoryImages[p.category?.name ?? '']
      ?? `https://picsum.photos/seed/product-${p.id}/400/300`;
  }

  onImgError(event: Event, p: Product) {
    const el = event.target as HTMLImageElement;
    const emoji = this.categoryEmojis[p.category?.name ?? ''] ?? '📦';
    el.style.display = 'none';
    const wrapper = el.parentElement;
    if (wrapper && !wrapper.querySelector('.img-fallback')) {
      const fb = document.createElement('div');
      fb.className = 'img-fallback';
      fb.textContent = emoji;
      wrapper.insertBefore(fb, el);
    }
  }
}