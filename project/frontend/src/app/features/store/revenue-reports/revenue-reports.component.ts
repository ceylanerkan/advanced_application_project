import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-revenue-reports',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './revenue-reports.component.html',
  styleUrls: []
})
export class RevenueReportsComponent {
  selectedMonth: string | null = null;

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  monthlyData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Monthly Revenue ($)', backgroundColor: '#3b82f6' }]
  };

  dailyData: ChartConfiguration<'line'>['data'] = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [{ data: [], label: 'Daily Revenue ($)', tension: 0.3, borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,0.2)', fill: true }]
  };

  months: any[] = [];
  allOrders: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getStores().subscribe({
      next: (stores: any[]) => {
        if (stores && stores.length > 0) {
          const storeId = stores[0].id;
          this.apiService.getStoreDashboard(storeId).subscribe({
            next: (data: any) => {
              this.monthlyData.labels = data.monthLabels;
              this.monthlyData.datasets[0].data = data.monthValues;
              this.monthlyData = { ...this.monthlyData };

              this.months = [];
              for(let i = 0; i < data.monthLabels.length; i++) {
                 const rev = data.monthValues[i];
                 const ords = data.monthOrdersValues[i];
                 const avg = ords > 0 ? (rev / ords).toFixed(2) : 0;
                 this.months.push({
                   name: data.monthLabels[i],
                   revenue: rev,
                   orders: ords,
                   avgOrder: avg
                 });
              }
            },
            error: (err: any) => console.error('Failed to load dashboard data', err)
          });
        }
      },
      error: (err: any) => console.error('Failed to load stores', err)
    });

    this.apiService.getOrders().subscribe({
      next: (orders: any[]) => this.allOrders = orders,
      error: (err: any) => console.error('Failed to load orders', err)
    });
  }

  drillDown(monthStr: string) {
    this.selectedMonth = monthStr;

    // Filter orders by selected month (e.g., "Jan", "Feb")
    const monthOrders = this.allOrders.filter(o => {
      if (!o.createdAt) return false;
      const d = new Date(o.createdAt);
      const mLabel = d.toLocaleString('default', { month: 'short' });
      return mLabel === monthStr;
    });

    // Determine max days in this month based on the first found order, or default to 31
    let maxDays = 31;
    if (monthOrders.length > 0) {
      const d = new Date(monthOrders[0].createdAt);
      maxDays = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
    }

    const dailyRev = new Array(maxDays).fill(0);
    const dayLabels = Array.from({length: maxDays}, (_, i) => `${i + 1}`);

    monthOrders.forEach(o => {
      const d = new Date(o.createdAt);
      const dayIdx = d.getDate() - 1;
      dailyRev[dayIdx] += (o.grandTotal || 0);
    });

    this.dailyData.labels = dayLabels;
    this.dailyData.datasets[0].data = dailyRev;
    this.dailyData = { ...this.dailyData };
  }

  goBack() {
    this.selectedMonth = null;
  }
}
