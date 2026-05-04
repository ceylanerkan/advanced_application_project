import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NgChartsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: []
})
export class AdminDashboardComponent implements OnInit {
  userEmail = '';

  // Chart configuration for dark theme
  public chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { labels: { color: '#f8fafc' } }
    },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };
  
  public doughnutOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { labels: { color: '#f8fafc' } }
    }
  };

  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Monthly Revenue ($)',
        fill: true,
        tension: 0.4,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.3)'
      }
    ]
  };

  public doughnutChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'],
        borderWidth: 0
      }
    ]
  };

  totalUsers = 0;
  totalRevenue = 0;
  activeStores = 0;
  totalOrders = 0;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.userEmail = user.email;
      this.fetchAdminDashboard();
    }
  }

  fetchAdminDashboard() {
    this.apiService.getAdminDashboard().subscribe({
      next: (data: any) => {
        this.totalUsers = data.totalUsers || 0;
        this.totalRevenue = data.totalRevenue || 0;
        this.activeStores = data.activeStores || 0;
        this.totalOrders = data.totalOrders || 0;

        this.lineChartData.labels = data.monthLabels;
        this.lineChartData.datasets[0].data = data.monthValues;

        this.doughnutChartData.labels = data.regionLabels;
        this.doughnutChartData.datasets[0].data = data.regionValues;

        this.lineChartData = { ...this.lineChartData };
        this.doughnutChartData = { ...this.doughnutChartData };
      },
      error: (err: any) => console.error('Failed to load admin dashboard data', err)
    });
  }

  isRecalculating = false;
  recalculateMsg = '';

  recalculateOrders() {
    this.isRecalculating = true;
    this.recalculateMsg = '';
    this.apiService.recalculateAllOrders().subscribe({
      next: (msg: string) => {
        this.isRecalculating = false;
        this.recalculateMsg = '✅ All order totals updated! Refreshing dashboard...';
        setTimeout(() => {
          this.recalculateMsg = '';
          this.fetchAdminDashboard();
        }, 2000);
      },
      error: (err: any) => {
        this.isRecalculating = false;
        this.recalculateMsg = '❌ Recalculation failed. Check console.';
        console.error('Recalculate error', err);
      }
    });
  }

  isPopulating = false;
  populateMsg = '';

  populateImages() {
    this.isPopulating = true;
    this.populateMsg = '';
    this.apiService.populateProductImages().subscribe({
      next: (res: any) => {
        this.isPopulating = false;
        this.populateMsg = `✅ ${res.message}`;
        setTimeout(() => this.populateMsg = '', 4000);
      },
      error: (err: any) => {
        this.isPopulating = false;
        this.populateMsg = '❌ Failed to populate images. Check console.';
        console.error('Populate images error', err);
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
