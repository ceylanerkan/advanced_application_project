import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

// Dashboards
import { AdminDashboardComponent } from './features/dashboards/admin-dashboard/admin-dashboard.component';
import { CorporateDashboardComponent } from './features/dashboards/corporate-dashboard/corporate-dashboard.component';
import { IndividualDashboardComponent } from './features/dashboards/individual-dashboard/individual-dashboard.component';

// AI Chat
import { ChatbotUiComponent } from './features/chat/chatbot-ui/chatbot-ui.component';

// Individual User — Shop
import { ProductCatalogComponent } from './features/products/product-catalog/product-catalog.component';
import { ProductDetailComponent } from './features/products/product-detail/product-detail.component';
import { CartComponent } from './features/cart/cart.component';
import { CheckoutComponent } from './features/checkout/checkout.component';
import { OrderHistoryComponent } from './features/orders/order-history/order-history.component';
import { OrderTrackingComponent } from './features/orders/order-tracking/order-tracking.component';

// Corporate User — Store Management
import { ProductManagementComponent } from './features/store/product-management/product-management.component';
import { InventoryComponent } from './features/store/inventory/inventory.component';
import { OrderFulfillmentComponent } from './features/store/order-fulfillment/order-fulfillment.component';
import { SalesAnalyticsComponent } from './features/store/sales-analytics/sales-analytics.component';
import { CustomerInsightsComponent } from './features/store/customer-insights/customer-insights.component';
import { RevenueReportsComponent } from './features/store/revenue-reports/revenue-reports.component';
import { ReviewManagementComponent } from './features/store/review-management/review-management.component';

// Admin
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { StoreManagementComponent } from './features/admin/store-management/store-management.component';
import { CategoryManagementComponent } from './features/admin/category-management/category-management.component';
import { AuditLogsComponent } from './features/admin/audit-logs/audit-logs.component';
import { SystemSettingsComponent } from './features/admin/system-settings/system-settings.component';
import { StoreComparisonComponent } from './features/admin/store-comparison/store-comparison.component';

export const routes: Routes = [
  // === Public ===
  { path: 'login', component: LoginComponent },

  // === Individual User ===
  { path: 'shop', component: ProductCatalogComponent, canActivate: [authGuard] },
  { path: 'shop/:id', component: ProductDetailComponent, canActivate: [authGuard] },
  { path: 'cart', component: CartComponent, canActivate: [authGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'orders', component: OrderHistoryComponent, canActivate: [authGuard] },
  { path: 'tracking/:id', component: OrderTrackingComponent, canActivate: [authGuard] },
  { path: 'individual', component: IndividualDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['INDIVIDUAL'] } },

  // === Corporate User ===
  { path: 'corporate', component: CorporateDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/products', component: ProductManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/inventory', component: InventoryComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/orders', component: OrderFulfillmentComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/sales', component: SalesAnalyticsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/customers', component: CustomerInsightsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/revenue', component: RevenueReportsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },
  { path: 'corporate/reviews', component: ReviewManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CORPORATE'] } },

  // === Admin ===
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/users', component: UserManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/stores', component: StoreManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/categories', component: CategoryManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/audit-logs', component: AuditLogsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/settings', component: SystemSettingsComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/store-comparison', component: StoreComparisonComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },

  // === AI Chat (All roles) ===
  { path: 'chat', component: ChatbotUiComponent, canActivate: [authGuard] },

  // === Defaults ===
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
