import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-sales-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, NgChartsModule],
  templateUrl: './sales-analytics.component.html',
  styleUrls: []
})
export class SalesAnalyticsComponent {
  dateFrom = new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0];
  dateTo = new Date().toISOString().split('T')[0];

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { min: 0, ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  salesData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { data: [], label: 'Revenue ($)', backgroundColor: '#3b82f6' },
      { data: [], label: 'Orders', backgroundColor: '#10b981' }
    ]
  };

  orderTrendData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Orders', tension: 0.4, borderColor: '#3b82f6', backgroundColor: 'rgba(59,130,246,0.2)', fill: true }]
  };

  allOrders: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getOrders().subscribe({
      next: (orders: any[]) => {
        this.allOrders = orders;
        this.applyFilters();
      },
      error: (err: any) => console.error('Failed to load orders', err)
    });
  }

  applyFilters() {
    const from = new Date(this.dateFrom);
    const to = new Date(this.dateTo);
    // Include end of day
    to.setHours(23, 59, 59, 999);

    const filtered = this.allOrders.filter(o => {
      const d = o.createdAt ? new Date(o.createdAt) : new Date();
      return d >= from && d <= to;
    });

    this.calculateCharts(filtered);
  }

  calculateCharts(orders: any[]) {
    // 1. Calculate Monthly Revenue & Orders (salesData)
    const monthMap = new Map<string, { rev: number, ord: number }>();
    orders.forEach(o => {
      const d = o.createdAt ? new Date(o.createdAt) : new Date();
      const label = d.toLocaleString('default', { month: 'short', year: 'numeric' });
      if (!monthMap.has(label)) monthMap.set(label, { rev: 0, ord: 0 });
      const stats = monthMap.get(label)!;
      stats.rev += (o.grandTotal || 0);
      stats.ord += 1;
    });

    const monthLabels = Array.from(monthMap.keys()).reverse(); // Older to newer assuming we sort or just use natural extraction
    const revData = monthLabels.map(l => monthMap.get(l)!.rev);
    const ordData = monthLabels.map(l => monthMap.get(l)!.ord);

    this.salesData.labels = monthLabels;
    this.salesData.datasets[0].data = revData;
    this.salesData.datasets[1].data = ordData;

    // 2. Calculate Weekly Order Trend (orderTrendData)
    const weekMap = new Map<string, number>();
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    days.forEach(d => weekMap.set(d, 0));
    
    orders.forEach(o => {
      const d = o.createdAt ? new Date(o.createdAt) : new Date();
      const dayLabel = days[d.getDay()];
      weekMap.set(dayLabel, weekMap.get(dayLabel)! + 1);
    });

    // To make it start from Monday, reorder
    const orderedDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    this.orderTrendData.labels = orderedDays;
    this.orderTrendData.datasets[0].data = orderedDays.map(d => weekMap.get(d)!);

    // Trigger Angular update
    this.salesData = { ...this.salesData };
    this.orderTrendData = { ...this.orderTrendData };
  }
}
