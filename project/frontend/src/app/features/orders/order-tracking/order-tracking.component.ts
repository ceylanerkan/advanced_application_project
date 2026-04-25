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

    // We can fetch actual shipment details if the shipment endpoint links to orderId
    // For now, mock the shipment details specific to this order to prevent errors
    this.shipment = {
      warehouse: 'Main Facility',
      mode: 'Standard Shipping',
      carrier: 'FedEx',
      trackingNumber: 'FDX-' + this.orderId + '-2026'
    };
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
