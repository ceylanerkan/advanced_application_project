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
  isProcessing = false;

  // Shipping address
  fullName = '';
  address = '';
  city = '';
  postalCode = '';
  phone = '';

  // Card details
  cardNumber = '';
  cardExpiry = '';
  cardCvv = '';

  // Validation
  errors: { [key: string]: string } = {};
  Object = Object;

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

  validate(): boolean {
    this.errors = {};

    if (!this.fullName.trim()) this.errors['fullName'] = 'Full name is required.';
    if (!this.address.trim()) this.errors['address'] = 'Address is required.';
    if (!this.city.trim()) this.errors['city'] = 'City is required.';
    if (!this.postalCode.trim()) this.errors['postalCode'] = 'Postal code is required.';
    if (!this.phone.trim()) this.errors['phone'] = 'Phone number is required.';

    if (this.paymentMethod === 'credit_card') {
      if (!this.cardNumber.trim() || this.cardNumber.replace(/\s/g, '').length < 16) {
        this.errors['cardNumber'] = 'Enter a valid 16-digit card number.';
      }
      if (!this.cardExpiry.trim() || !/^\d{2}\/\d{2}$/.test(this.cardExpiry)) {
        this.errors['cardExpiry'] = 'Enter expiry as MM/YY.';
      }
      if (!this.cardCvv.trim() || this.cardCvv.length < 3) {
        this.errors['cardCvv'] = 'Enter a valid CVV.';
      }
    }

    return Object.keys(this.errors).length === 0;
  }

  placeOrder() {
    if (!this.validate()) return;

    this.isProcessing = true;

    const storeMap = new Map<number, any[]>();
    this.cartItems.forEach(item => {
      const storeId = item.store?.id || 1;
      if (!storeMap.has(storeId)) {
        storeMap.set(storeId, []);
      }
      storeMap.get(storeId)!.push(item);
    });

    const user = this.authService.currentUserValue;
    if (!user) {
      alert('You must be logged in to place an order.');
      this.isProcessing = false;
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
        baseCurrency: 'USD',
        shippingFullName: this.fullName,
        shippingAddress: this.address,
        shippingCity: this.city,
        shippingPostalCode: this.postalCode,
        shippingPhone: this.phone
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

    if (orderObservables.length === 0) {
      this.isProcessing = false;
      return;
    }

    forkJoin(orderObservables).subscribe({
      next: () => {
        this.orderPlaced = true;
        this.isProcessing = false;
        localStorage.removeItem('cart');
      },
      error: (err) => {
        console.error('Order creation failed', err);
        this.isProcessing = false;
        alert('There was an issue processing your order. Please try again.');
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
