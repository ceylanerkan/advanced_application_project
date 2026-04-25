import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

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
    datasets: [{ data: [120, 340, 540], backgroundColor: ['#f59e0b', '#94a3b8', '#b45309'], borderWidth: 0 }]
  };

  cityData: ChartConfiguration<'bar'>['data'] = {
    labels: ['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix'],
    datasets: [{ data: [180, 150, 120, 90, 75], label: 'Customers', backgroundColor: '#3b82f6' }]
  };

  customers = [
    { email: 'john@example.com', membership: 'Gold', orders: 24, totalSpent: 3450.00, city: 'New York' },
    { email: 'sarah@example.com', membership: 'Silver', orders: 15, totalSpent: 1890.50, city: 'LA' },
    { email: 'mike@example.com', membership: 'Bronze', orders: 8, totalSpent: 720.00, city: 'Chicago' },
    { email: 'emma@example.com', membership: 'Gold', orders: 31, totalSpent: 5120.75, city: 'Houston' },
    { email: 'alex@example.com', membership: 'Silver', orders: 12, totalSpent: 1340.00, city: 'Phoenix' }
  ];
}
