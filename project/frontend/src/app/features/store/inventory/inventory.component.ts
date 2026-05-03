import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory.component.html',
  styleUrls: []
})
export class InventoryComponent implements OnInit {
  inventory: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getProducts().subscribe({
      next: (products: any[]) => {
        this.inventory = products.map(p => ({
          id: p.id,
          name: p.name,
          sku: `SKU-${String(p.id).padStart(4, '0')}`,
          stock: p.stock || 0,
          reorderLevel: 30, // threshold
          lastRestocked: 'N/A'
        }));
      },
      error: (err: any) => console.error('Failed to load inventory', err)
    });
  }

  get lowStockCount(): number {
    return this.inventory.filter(i => i.stock < i.reorderLevel).length;
  }
}
