import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-store-comparison',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './store-comparison.component.html',
  styleUrls: []
})
export class StoreComparisonComponent {
  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  revenueCompare: ChartConfiguration<'bar'>['data'] = {
    labels: ['Tech Haven', 'Fashion Boutique', 'Home Essentials', 'Sports World', 'Book Corner'],
    datasets: [
      { data: [124000, 89000, 56000, 230000, 67000], label: 'Revenue ($)', backgroundColor: '#3b82f6' },
      { data: [890, 650, 420, 1200, 510], label: 'Orders', backgroundColor: '#10b981' }
    ]
  };

  ratingCompare: ChartConfiguration<'bar'>['data'] = {
    labels: ['Tech Haven', 'Fashion Boutique', 'Home Essentials', 'Sports World', 'Book Corner'],
    datasets: [{ data: [4.5, 4.2, 3.9, 4.7, 4.1], label: 'Avg Rating', backgroundColor: '#f59e0b' }]
  };
}
