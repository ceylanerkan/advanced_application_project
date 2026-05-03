import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-tracking.component.html',
  styleUrls: []
})
export class OrderTrackingComponent implements OnInit {
  orderId = 0;
  order: any = null;
  shipment: any = null;
  items: any[] = [];
  trackingSteps = [
    { label: 'Order Placed', status: '', date: '', desc: 'Your order has been confirmed.' },
    { label: 'Processing', status: '', date: '', desc: 'Order is being processed at the warehouse.' },
    { label: 'Shipped', status: '', date: '', desc: 'Package picked up by carrier.' },
    { label: 'In Transit', status: '', date: '', desc: 'Package is on the way.' },
    { label: 'Delivered', status: '', date: '', desc: 'Package delivered to your address.' }
  ];

  constructor(private route: ActivatedRoute, private router: Router, private apiService: ApiService) {}

  ngOnInit() {
    this.orderId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.apiService.getOrder(this.orderId).subscribe({
      next: (data) => {
        this.order = data;
        this.updateTimeline(this.order.status);
      },
      error: (err) => console.error('Failed to load order', err)
    });

    this.apiService.getOrderItems(this.orderId).subscribe({
      next: (data) => this.items = data,
      error: () => this.items = []
    });

    this.apiService.getShipmentByOrder(this.orderId).subscribe({
      next: (shipmentData) => {
        this.shipment = shipmentData;
      },
      error: (err) => {
        console.error('Failed to load shipment details or no shipment exists yet', err);
        // Fallback for visual testing if no real shipment
        this.shipment = {
          warehouse: 'Processing Facility',
          mode: 'Pending',
          carrier: 'TBD',
          trackingNumber: 'PENDING'
        };
      }
    });
  }

  updateTimeline(status: string) {
    const s = status ? status.toUpperCase() : 'PENDING';
    
    this.trackingSteps[0].status = 'done';
    this.trackingSteps[0].date = this.order.createdAt?.split('T')[0] || '';

    if (s === 'PROCESSING' || s === 'SHIPPED' || s === 'DELIVERED') {
      this.trackingSteps[1].status = 'done';
    } else if (s === 'PENDING') {
      this.trackingSteps[1].status = 'active';
    }

    if (s === 'SHIPPED' || s === 'DELIVERED') {
      this.trackingSteps[2].status = 'done';
      this.trackingSteps[3].status = s === 'DELIVERED' ? 'done' : 'active';
    } else if (s === 'PROCESSING') {
      this.trackingSteps[2].status = 'active';
    }

    if (s === 'DELIVERED') {
      this.trackingSteps[4].status = 'done';
    }
  }

  goBack() {
    this.router.navigate(['/orders']);
  }
}
