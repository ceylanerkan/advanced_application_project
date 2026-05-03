import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

interface Product {
  id: number;
  name: string;
  sku: string;
  unitPrice: number;
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

  constructor(private router: Router, private apiService: ApiService) {}

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
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => { console.error('Failed to load products', err); this.isLoading = false; }
    });
  }

  prevPage() { if (this.currentPage > 0) this.loadPage(this.currentPage - 1); }
  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadPage(this.currentPage + 1); }

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
}
