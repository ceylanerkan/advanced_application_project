import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

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

  ratingChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { min: 0, max: 5, ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  revenueCompare: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { data: [], label: 'Revenue ($)', backgroundColor: '#3b82f6' },
      { data: [], label: 'Products', backgroundColor: '#10b981' }
    ]
  };

  ratingCompare: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Avg Rating', backgroundColor: '#f59e0b' }]
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getStores().subscribe({
      next: (data: any[]) => {
        // We will sort stores by revenue as an example to show top stores
        const sortedStores = data.sort((a, b) => (b.revenue || 0) - (a.revenue || 0)).slice(0, 5);

        this.revenueCompare.labels = sortedStores.map(s => s.name);
        this.revenueCompare.datasets[0].data = sortedStores.map(s => s.revenue || 0);
        this.revenueCompare.datasets[1].data = sortedStores.map(s => s.products || 0);

        this.ratingCompare.labels = sortedStores.map(s => s.name);
        // Assuming avgRating is added, or default to 0 if not
        this.ratingCompare.datasets[0].data = sortedStores.map(s => s.avgRating || 0);

        this.revenueCompare = { ...this.revenueCompare };
        this.ratingCompare = { ...this.ratingCompare };
      },
      error: (err) => console.error('Failed to load stores for comparison', err)
    });
  }
}
