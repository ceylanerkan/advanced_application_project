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

  constructor(private router: Router, private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getProducts().subscribe({
      next: (data) => {
        // Map backend product data
        this.products = data.map(p => ({
          ...p,
          rating: p.rating || (Math.random() * 2 + 3).toFixed(1), // Mock rating since backend doesn't have it
          stock: p.stock || Math.floor(Math.random() * 100) + 10 // Mock stock
        }));
        // Ensure category object exists to prevent errors
        this.products.forEach(p => {
          if (!p.category) {
            p.category = { name: 'Uncategorized' };
          }
        });
        this.categories = [...new Set(this.products.map(p => p.category.name))].filter(Boolean);
        this.applyFilters();
      },
      error: (err) => console.error('Failed to load products', err)
    });
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
}
