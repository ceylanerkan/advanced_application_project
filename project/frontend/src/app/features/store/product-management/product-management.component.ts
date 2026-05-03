import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-management.component.html',
  styleUrls: []
})
export class ProductManagementComponent implements OnInit {
  products: any[] = [];
  categories: any[] = [];
  storeId: number | null = null;
  showModal = false;
  editMode = false;
  currentProduct: any = { name: '', sku: '', unitPrice: 0, categoryId: null, stock: 0 };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getStores().subscribe({
      next: (stores) => {
        if (stores.length > 0) this.storeId = stores[0].id;
      }
    });
    this.loadProducts();
    this.apiService.getCategories().subscribe(cats => this.categories = cats);
  }

  loadProducts() {
    this.apiService.getMyProducts().subscribe({
      next: (data) => {
        // Assume corporate user only sees their products (backend usually handles this filter via token)
        this.products = data.map(p => ({
          ...p,
          stock: p.stock || 0
        }));
      },
      error: (err) => console.error('Failed to load products', err)
    });
  }

  openCreate() {
    this.editMode = false;
    this.currentProduct = { name: '', sku: '', unitPrice: 0, categoryId: this.categories[0]?.id, stock: 0 };
    this.showModal = true;
  }

  openEdit(product: any) {
    this.editMode = true;
    this.currentProduct = { 
      ...product,
      categoryId: product.category?.id
    };
    this.showModal = true;
  }

  get hasNoStore(): boolean {
    return this.storeId === null;
  }

  save() {
    if (!this.storeId) {
      this.showModal = false;
      return;
    }
    const payload = {
      name: this.currentProduct.name,
      sku: this.currentProduct.sku,
      unitPrice: this.currentProduct.unitPrice,
      baseCurrency: 'USD',
      category: { id: Number(this.currentProduct.categoryId) },
      store: { id: this.storeId },
      stock: this.currentProduct.stock
    };

    if (this.editMode) {
      this.apiService.updateProduct(this.currentProduct.id, payload).subscribe({
        next: () => {
          this.loadProducts();
          this.showModal = false;
        },
        error: () => alert('Failed to update product')
      });
    } else {
      this.apiService.createProduct(payload).subscribe({
        next: () => {
          this.loadProducts();
          this.showModal = false;
        },
        error: () => alert('Failed to create product')
      });
    }
  }

  deleteProduct(id: number) {
    if (confirm('Are you sure you want to delete this product?')) {
      this.apiService.deleteProduct(id).subscribe({
        next: () => this.loadProducts(),
        error: () => alert('Failed to delete product')
      });
    }
  }

  closeModal() { this.showModal = false; }
}
