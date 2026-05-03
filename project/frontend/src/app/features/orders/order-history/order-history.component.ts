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
    this.apiService.getOrdersPaged(page).subscribe({
      next: (data) => {
        this.orders = data.content ?? [];
        this.currentPage = data.number ?? 0;
        this.totalPages = data.totalPages ?? 1;
        this.totalElements = data.totalElements ?? this.orders.length;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => { console.error('Failed to load orders', err); this.isLoading = false; }
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
