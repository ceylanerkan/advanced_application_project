import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // ── Auth ──
  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.base}/auth/login`, { email, password });
  }

  register(email: string, password: string, roleType: string): Observable<any> {
    return this.http.post(`${this.base}/auth/register`, { email, password, roleType });
  }

  // ── Products ──
  getProducts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/products`);
  }

  getProduct(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/products/${id}`);
  }

  createProduct(product: any): Observable<any> {
    return this.http.post(`${this.base}/products`, product);
  }

  updateProduct(id: number, product: any): Observable<any> {
    return this.http.put(`${this.base}/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<any> {
    return this.http.delete(`${this.base}/products/${id}`);
  }

  // ── Categories ──
  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/categories`);
  }

  createCategory(cat: any): Observable<any> {
    return this.http.post(`${this.base}/categories`, cat);
  }

  updateCategory(id: number, cat: any): Observable<any> {
    return this.http.put(`${this.base}/categories/${id}`, cat);
  }

  deleteCategory(id: number): Observable<any> {
    return this.http.delete(`${this.base}/categories/${id}`);
  }

  // ── Orders ──
  getOrders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/orders`);
  }

  getOrder(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/orders/${id}`);
  }

  createOrder(order: any): Observable<any> {
    return this.http.post(`${this.base}/orders`, order);
  }

  updateOrderStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.base}/orders/${id}`, { status });
  }

  // ── Order Items ──
  getOrderItems(orderId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/order-items/order/${orderId}`);
  }

  createOrderItem(orderItem: any): Observable<any> {
    return this.http.post(`${this.base}/order-items`, orderItem);
  }

  // ── Users ──
  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/users`);
  }

  getUser(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/users/${id}`);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.base}/users/${id}`);
  }

  // ── Stores ──
  getStores(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/stores`);
  }

  updateStore(id: number, store: any): Observable<any> {
    return this.http.put(`${this.base}/stores/${id}`, store);
  }

  // ── Reviews ──
  getReviews(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/reviews`);
  }

  createReview(review: any): Observable<any> {
    return this.http.post(`${this.base}/reviews`, review);
  }

  // ── Shipments ──
  getShipments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/shipments`);
  }

  getShipment(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/shipments/${id}`);
  }

  getShipmentByOrder(orderId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/shipments/order/${orderId}`);
  }

  // ── AI Chat ──
  askAI(question: string, sessionId: string): Observable<any> {
    return this.http.post(`${this.base}/ai-chat/ask`, { question, sessionId });
  }

  // ── Customer Profiles ──
  getProfiles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/customer-profiles`);
  }

  // ── Dashboards ──
  getIndividualDashboard(userId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/users/${userId}/dashboard`);
  }

  getStoreDashboard(storeId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/stores/${storeId}/dashboard`);
  }

  getAdminDashboard(): Observable<any> {
    return this.http.get<any>(`${this.base}/admin/dashboard`);
  }

}
