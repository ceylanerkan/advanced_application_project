import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-store-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './store-management.component.html',
  styleUrls: []
})
export class StoreManagementComponent implements OnInit {
  stores: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadStores();
  }

  loadStores() {
    this.apiService.getStores().subscribe({
      next: (data) => {
        this.stores = data.map(s => ({
          ...s,
          products: s.products || 0,
          revenue: s.revenue || 0
        }));
      },
      error: (err) => console.error('Failed to load stores', err)
    });
  }

  toggleStatus(store: any) {
    const newStatus = store.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    this.apiService.updateStore(store.id, { ...store, status: newStatus }).subscribe({
      next: () => store.status = newStatus,
      error: (err) => alert('Failed to update store status')
    });
  }
}
