import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-customer-insights',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './customer-insights.component.html',
  styleUrls: []
})
export class CustomerInsightsComponent {
  doughnutOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } }
  };

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  membershipData: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Gold', 'Silver', 'Bronze'],
    datasets: [{ data: [], backgroundColor: ['#f59e0b', '#94a3b8', '#b45309'], borderWidth: 0 }]
  };

  cityData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Customers', backgroundColor: '#3b82f6' }]
  };

  customers: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getStores().subscribe({
      next: (stores: any[]) => {
        if (stores && stores.length > 0) {
          const storeId = stores[0].id;
          this.apiService.getStoreDashboard(storeId).subscribe({
            next: (data: any) => {
              this.membershipData.labels = data.membershipLabels;
              this.membershipData.datasets[0].data = data.membershipValues;

              this.cityData.labels = data.cityLabels;
              this.cityData.datasets[0].data = data.cityValues;

              this.membershipData = { ...this.membershipData };
              this.cityData = { ...this.cityData };

              this.customers = data.topCustomers || [];
              // Sort by highest spender
              this.customers.sort((a: any, b: any) => b.totalSpent - a.totalSpent);
            },
            error: (err: any) => console.error('Failed to load dashboard data', err)
          });
        }
      },
      error: (err: any) => console.error('Failed to load stores', err)
    });
  }
}
