import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-corporate-dashboard',
  standalone: true,
  imports: [RouterModule, NgChartsModule],
  templateUrl: './corporate-dashboard.component.html',
  styleUrls: [] // Reusing admin styles
})
export class CorporateDashboardComponent implements OnInit {
  userEmail = '';

  public chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };
  
  public pieOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } }
  };

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Daily Revenue ($)',
        backgroundColor: '#10b981'
      }
    ]
  };

  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'],
        borderWidth: 0
      }
    ]
  };

  totalRevenue = 0;
  totalOrders = 0;
  activeProducts = 0;
  avgRating = 0;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.userEmail = user.email;
      this.fetchDashboardData();
    }
  }

  fetchDashboardData() {
    // Get the store for the corporate user first
    this.apiService.getStores().subscribe({
      next: (stores: any[]) => {
        if (stores && stores.length > 0) {
          const storeId = stores[0].id;
          this.apiService.getStoreDashboard(storeId).subscribe({
            next: (data: any) => {
              this.totalRevenue = data.totalRevenue || 0;
              this.totalOrders = data.totalOrders || 0;
              this.activeProducts = data.activeProducts || 0;
              this.avgRating = data.avgRating || 0;

              this.barChartData.labels = data.weekLabels;
              this.barChartData.datasets[0].data = data.weekValues;

              this.pieChartData.labels = data.categoryLabels;
              this.pieChartData.datasets[0].data = data.categoryValues;

              this.barChartData = { ...this.barChartData };
              this.pieChartData = { ...this.pieChartData };
            },
            error: (err: any) => console.error('Failed to load store dashboard data', err)
          });
        }
      },
      error: (err: any) => console.error('Failed to load stores', err)
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
