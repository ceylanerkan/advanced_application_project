import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-order-fulfillment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-fulfillment.component.html',
  styleUrls: []
})
export class OrderFulfillmentComponent implements OnInit {
  orders: any[] = [];
  statuses = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.apiService.getOrders().subscribe({
      next: (data) => {
        // Assume corporate user only sees orders for their store
        this.orders = data.map(o => ({
          ...o,
          status: o.status?.toUpperCase() || 'PENDING'
        }));
      },
      error: (err) => console.error('Failed to load orders', err)
    });
  }

  advanceStatus(order: any) {
    const idx = this.statuses.indexOf(order.status);
    if (idx < this.statuses.length - 1) {
      const nextStatus = this.statuses[idx + 1];
      this.apiService.updateOrderStatus(order.id, nextStatus).subscribe({
        next: () => {
          order.status = nextStatus;
        },
        error: (err) => alert('Failed to update order status')
      });
    }
  }

  getNextStatus(status: string): string {
    const idx = this.statuses.indexOf(status?.toUpperCase());
    return idx < this.statuses.length - 1 ? this.statuses[idx + 1] : '';
  }

  getCountByStatus(status: string): number {
    return this.orders.filter(o => o.status === status.toUpperCase()).length;
  }
}
