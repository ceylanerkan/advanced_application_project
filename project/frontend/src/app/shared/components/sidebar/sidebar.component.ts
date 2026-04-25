import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar glass-panel" *ngIf="isLoggedIn">
      <div class="sidebar-brand">
        <span class="brand-icon">🛒</span>
        <h3>E-Analytics</h3>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section" *ngFor="let section of navSections">
          <span class="nav-section-label">{{ section.label }}</span>
          <ul>
            <li *ngFor="let item of section.items">
              <a [routerLink]="item.route" routerLinkActive="active">
                <span class="nav-icon">{{ item.icon }}</span>
                {{ item.label }}
              </a>
            </li>
          </ul>
        </div>
      </nav>

      <div class="user-info">
        <div class="user-badge">
          <span class="role-badge">{{ userRole }}</span>
          <p>{{ userEmail }}</p>
        </div>
        <button class="btn btn-outline" (click)="logout()">⎋ Logout</button>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 260px;
      min-height: 100vh;
      border-radius: 0;
      border-right: 1px solid var(--glass-border);
      border-top: none; border-bottom: none; border-left: none;
      display: flex;
      flex-direction: column;
      padding: var(--spacing-lg) var(--spacing-md);
      position: fixed;
      left: 0;
      top: 0;
      bottom: 0;
      z-index: 100;
      overflow-y: auto;
    }

    .sidebar-brand {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-xl);
      padding: 0 var(--spacing-sm);
    }

    .brand-icon { font-size: 1.5rem; }

    .sidebar-brand h3 {
      color: var(--accent-primary);
      font-size: 1.25rem;
      margin: 0;
    }

    .nav-section { margin-bottom: var(--spacing-lg); }

    .nav-section-label {
      display: block;
      font-size: 0.7rem;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: var(--text-secondary);
      padding: 0 var(--spacing-sm);
      margin-bottom: var(--spacing-sm);
      font-weight: 600;
    }

    ul { list-style: none; padding: 0; margin: 0; }

    a {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: 0.55rem var(--spacing-sm);
      color: var(--text-secondary);
      text-decoration: none;
      border-radius: var(--radius-sm);
      transition: all 0.2s;
      cursor: pointer;
      font-size: 0.9rem;
    }

    a:hover {
      background: rgba(255, 255, 255, 0.05);
      color: var(--text-primary);
    }

    a.active {
      background: var(--accent-primary);
      color: white;
    }

    .nav-icon { font-size: 1rem; width: 20px; text-align: center; }

    .user-info {
      margin-top: auto;
      border-top: 1px solid var(--glass-border);
      padding-top: var(--spacing-md);
    }

    .user-badge {
      margin-bottom: var(--spacing-sm);
    }

    .role-badge {
      display: inline-block;
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      background: rgba(59, 130, 246, 0.2);
      color: var(--accent-primary);
      padding: 0.15rem 0.5rem;
      border-radius: var(--radius-sm);
      margin-bottom: 0.25rem;
    }

    .user-info p {
      font-size: 0.8rem;
      margin: 0.25rem 0 0 0;
      color: var(--text-secondary);
    }

    .user-info .btn {
      width: 100%;
      font-size: 0.8rem;
      padding: 0.4rem;
      margin-top: var(--spacing-sm);
    }
  `]
})
export class SidebarComponent implements OnInit {
  isLoggedIn = false;
  userEmail = '';
  userRole = '';
  navSections: { label: string; items: NavItem[] }[] = [];

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    this.auth.currentUser$.subscribe(user => {
      this.isLoggedIn = !!user;
      if (user) {
        this.userEmail = user.email;
        this.userRole = user.role;
        this.navSections = this.getNavForRole(user.role);
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  private getNavForRole(role: string): { label: string; items: NavItem[] }[] {
    const common: NavItem[] = [
      { label: 'AI Chatbot', icon: '🤖', route: '/chat' }
    ];

    if (role === 'ADMIN') {
      return [
        { label: 'Dashboard', items: [
          { label: 'Platform Overview', icon: '📊', route: '/admin' },
          { label: 'Store Comparison', icon: '📈', route: '/admin/store-comparison' }
        ]},
        { label: 'Management', items: [
          { label: 'Users', icon: '👤', route: '/admin/users' },
          { label: 'Stores', icon: '🏪', route: '/admin/stores' },
          { label: 'Categories', icon: '📁', route: '/admin/categories' },
          { label: 'Audit Logs', icon: '📋', route: '/admin/audit-logs' },
          { label: 'Settings', icon: '⚙️', route: '/admin/settings' }
        ]},
        { label: 'AI', items: common }
      ];
    }

    if (role === 'CORPORATE') {
      return [
        { label: 'Dashboard', items: [
          { label: 'Store KPIs', icon: '📊', route: '/corporate' }
        ]},
        { label: 'Store', items: [
          { label: 'Products', icon: '📦', route: '/corporate/products' },
          { label: 'Inventory', icon: '📋', route: '/corporate/inventory' },
          { label: 'Orders', icon: '🛒', route: '/corporate/orders' },
          { label: 'Reviews', icon: '⭐', route: '/corporate/reviews' }
        ]},
        { label: 'Analytics', items: [
          { label: 'Sales', icon: '📈', route: '/corporate/sales' },
          { label: 'Customers', icon: '👥', route: '/corporate/customers' },
          { label: 'Revenue', icon: '💰', route: '/corporate/revenue' }
        ]},
        { label: 'AI', items: common }
      ];
    }

    // INDIVIDUAL
    return [
      { label: 'Shop', items: [
        { label: 'Browse Products', icon: '🛍️', route: '/shop' },
        { label: 'My Cart', icon: '🛒', route: '/cart' }
      ]},
      { label: 'My Account', items: [
        { label: 'Dashboard', icon: '📊', route: '/individual' },
        { label: 'Order History', icon: '📋', route: '/orders' },
        { label: 'Track Shipments', icon: '🚚', route: '/tracking' }
      ]},
      { label: 'AI', items: common }
    ];
  }
}
