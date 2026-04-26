import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-sales-analytics',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './sales-analytics.component.html',
  styleUrls: []
})
export class SalesAnalyticsComponent {
  dateFrom = '2026-01-01';
  dateTo = '2026-04-30';

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  salesData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr'],
    datasets: [
      { data: [42000, 38000, 51000, 47000], label: 'Revenue ($)', backgroundColor: '#3b82f6' },
      { data: [320, 280, 410, 370], label: 'Orders', backgroundColor: '#10b981' }
    ]
  };

  orderTrendData: ChartConfiguration<'line'>['data'] = {
    labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6', 'Week 7', 'Week 8'],
    datasets: [{ data: [45, 52, 38, 65, 72, 48, 80, 91], label: 'Orders', tension: 0.4, borderColor: '#3b82f6', backgroundColor: 'rgba(59,130,246,0.2)', fill: true }]
  };
}
