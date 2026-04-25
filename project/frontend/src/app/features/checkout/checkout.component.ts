import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { forkJoin } from 'rxjs';
import { concatMap } from 'rxjs/operators';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout.component.html',
  styleUrls: []
})
export class CheckoutComponent implements OnInit {
  cartItems: any[] = [];
  paymentMethod = 'credit_card';
  orderPlaced = false;

  constructor(private router: Router, private apiService: ApiService, private authService: AuthService) { }

  ngOnInit() {
    this.cartItems = JSON.parse(localStorage.getItem('cart') || '[]');
    if (this.cartItems.length === 0) {
      this.router.navigate(['/cart']);
    }
  }

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  }

  get tax(): number { return this.subtotal * 0.08; }
  get total(): number { return this.subtotal + this.tax; }

  placeOrder() {
    const storeMap = new Map<number, any[]>();
    this.cartItems.forEach(item => {
      const storeId = item.store?.id || 1; // Default to 1 if no store
      if (!storeMap.has(storeId)) {
        storeMap.set(storeId, []);
      }
      storeMap.get(storeId)!.push(item);
    });

    const user = this.authService.currentUserValue;
    if (!user) {
      alert('You must be logged in to place an order.');
      return;
    }

    const orderObservables = Array.from(storeMap.entries()).map(([storeId, items]) => {
      const storeTotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
      const storeTax = storeTotal * 0.08;
      const grandTotal = storeTotal + storeTax;

      const orderPayload = {
        store: { id: storeId },
        user: { id: user.id },
        status: 'PENDING',
        grandTotal: grandTotal,
        baseCurrency: 'USD'
      };

      return this.apiService.createOrder(orderPayload).pipe(
        concatMap((order: any) => {
          const itemObservables = items.map(item => this.apiService.createOrderItem({
            order: { id: order.id },
            product: { id: item.id },
            quantity: item.quantity,
            price: item.unitPrice,
            baseCurrency: 'USD'
          }));
          return forkJoin(itemObservables);
        })
      );
    });

    if (orderObservables.length === 0) return;

    forkJoin(orderObservables).subscribe({
      next: () => {
        this.orderPlaced = true;
        localStorage.removeItem('cart');
      },
      error: (err) => {
        console.error('Order creation failed', err);
        alert('There was an issue processing your order.');
      }
    });
  }

  goToOrders() {
    this.router.navigate(['/orders']);
  }

  goHome() {
    this.router.navigate(['/shop']);
  }
}
