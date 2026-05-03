import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // === Public ===
  { path: 'login', component: LoginComponent },

  // === Individual User ===
  {
    path: 'shop',
    loadComponent: () => import('./features/products/product-catalog/product-catalog.component').then(m => m.ProductCatalogComponent),
    canActivate: [authGuard]
  },
  {
    path: 'shop/:id',
    loadComponent: () => import('./features/products/product-detail/product-detail.component').then(m => m.ProductDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent),
    canActivate: [authGuard]
  },
  {
    path: 'checkout',
    loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent),
    canActivate: [authGuard]
  },
  {
    path: 'orders',
    loadComponent: () => import('./features/orders/order-history/order-history.component').then(m => m.OrderHistoryComponent),
    canActivate: [authGuard]
  },
  {
    path: 'tracking/:id',
    loadComponent: () => import('./features/orders/order-tracking/order-tracking.component').then(m => m.OrderTrackingComponent),
    canActivate: [authGuard]
  },
  {
    path: 'individual',
    loadComponent: () => import('./features/dashboards/individual-dashboard/individual-dashboard.component').then(m => m.IndividualDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['INDIVIDUAL'] }
  },

  // === Corporate User ===
  {
    path: 'corporate',
    loadComponent: () => import('./features/dashboards/corporate-dashboard/corporate-dashboard.component').then(m => m.CorporateDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/products',
    loadComponent: () => import('./features/store/product-management/product-management.component').then(m => m.ProductManagementComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/inventory',
    loadComponent: () => import('./features/store/inventory/inventory.component').then(m => m.InventoryComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/orders',
    loadComponent: () => import('./features/store/order-fulfillment/order-fulfillment.component').then(m => m.OrderFulfillmentComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/sales',
    loadComponent: () => import('./features/store/sales-analytics/sales-analytics.component').then(m => m.SalesAnalyticsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/customers',
    loadComponent: () => import('./features/store/customer-insights/customer-insights.component').then(m => m.CustomerInsightsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/revenue',
    loadComponent: () => import('./features/store/revenue-reports/revenue-reports.component').then(m => m.RevenueReportsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },
  {
    path: 'corporate/reviews',
    loadComponent: () => import('./features/store/review-management/review-management.component').then(m => m.ReviewManagementComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CORPORATE'] }
  },

  // === Admin ===
  {
    path: 'admin',
    loadComponent: () => import('./features/dashboards/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/users',
    loadComponent: () => import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/stores',
    loadComponent: () => import('./features/admin/store-management/store-management.component').then(m => m.StoreManagementComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/categories',
    loadComponent: () => import('./features/admin/category-management/category-management.component').then(m => m.CategoryManagementComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/audit-logs',
    loadComponent: () => import('./features/admin/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/settings',
    loadComponent: () => import('./features/admin/system-settings/system-settings.component').then(m => m.SystemSettingsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/store-comparison',
    loadComponent: () => import('./features/admin/store-comparison/store-comparison.component').then(m => m.StoreComparisonComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },

  // === AI Chat (All roles) ===
  {
    path: 'chat',
    loadComponent: () => import('./features/chat/chatbot-ui/chatbot-ui.component').then(m => m.ChatbotUiComponent),
    canActivate: [authGuard]
  },

  // === Defaults ===
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
