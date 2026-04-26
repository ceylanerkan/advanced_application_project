import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory.component.html',
  styleUrls: []
})
export class InventoryComponent implements OnInit {
  inventory: any[] = [];

  ngOnInit() {
    this.inventory = Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      name: `Product ${i + 1}`,
      sku: `SKU-${String(i + 1).padStart(4, '0')}`,
      stock: Math.floor(Math.random() * 300),
      reorderLevel: 30,
      lastRestocked: `2026-03-${String(10 + (i % 20)).padStart(2, '0')}`
    }));
  }

  get lowStockCount(): number {
    return this.inventory.filter(i => i.stock < i.reorderLevel).length;
  }
}
