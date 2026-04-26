import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

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
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [{ data: [42000, 38000, 51000, 47000, 53000, 61000], label: 'Monthly Revenue ($)', backgroundColor: '#3b82f6' }]
  };

  dailyData: ChartConfiguration<'line'>['data'] = {
    labels: Array.from({ length: 30 }, (_, i) => `Day ${i + 1}`),
    datasets: [{ data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 3000 + 500)), label: 'Daily Revenue ($)', tension: 0.3, borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,0.2)', fill: true }]
  };

  months = [
    { name: 'January', revenue: 42000, orders: 320, avgOrder: 131 },
    { name: 'February', revenue: 38000, orders: 280, avgOrder: 136 },
    { name: 'March', revenue: 51000, orders: 410, avgOrder: 124 },
    { name: 'April', revenue: 47000, orders: 370, avgOrder: 127 },
    { name: 'May', revenue: 53000, orders: 420, avgOrder: 126 },
    { name: 'June', revenue: 61000, orders: 490, avgOrder: 124 }
  ];

  drillDown(month: string) {
    this.selectedMonth = month;
  }

  goBack() {
    this.selectedMonth = null;
  }
}
