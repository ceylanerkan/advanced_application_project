import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-history.component.html',
  styleUrls: []
})
export class OrderHistoryComponent implements OnInit {
  orders: any[] = [];
  filteredOrders: any[] = [];
  statusFilter = '';
  searchTerm = '';

  constructor(private router: Router, private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.filteredOrders = data;
      },
      error: (err) => console.error('Failed to load orders', err)
    });
  }

  applyFilters() {
    let result = [...this.orders];
    if (this.statusFilter) result = result.filter(o => o.status === this.statusFilter);
    if (this.searchTerm) {
      const t = this.searchTerm.toLowerCase();
      result = result.filter(o => o.id.toString().includes(t));
    }
    this.filteredOrders = result;
  }

  trackOrder(orderId: number) {
    this.router.navigate(['/tracking', orderId]);
  }

  exportCSV() {
    const header = 'Order ID,Status,Total,Date\n';
    const rows = this.filteredOrders.map(o => `${o.id},${o.status},${o.grandTotal},${o.createdAt}`).join('\n');
    const blob = new Blob([header + rows], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'orders.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
