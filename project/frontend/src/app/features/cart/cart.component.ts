import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
  styleUrls: []
})
export class CartComponent implements OnInit {
  cartItems: any[] = [];

  constructor(private router: Router) {}

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.cartItems = JSON.parse(localStorage.getItem('cart') || '[]');
  }

  get total(): number {
    return this.cartItems.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  }

  increaseQty(item: any) {
    item.quantity++;
    this.saveCart();
  }

  decreaseQty(item: any) {
    if (item.quantity > 1) { item.quantity--; }
    else { this.removeItem(item); }
    this.saveCart();
  }

  removeItem(item: any) {
    this.cartItems = this.cartItems.filter(i => i.id !== item.id);
    this.saveCart();
  }

  saveCart() {
    localStorage.setItem('cart', JSON.stringify(this.cartItems));
  }

  checkout() {
    this.router.navigate(['/checkout']);
  }

  continueShopping() {
    this.router.navigate(['/shop']);
  }
}
