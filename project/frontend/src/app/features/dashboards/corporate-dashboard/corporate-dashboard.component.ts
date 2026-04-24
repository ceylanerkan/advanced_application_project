import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-corporate-dashboard',
  standalone: true,
  imports: [RouterModule, NgChartsModule],
  templateUrl: './corporate-dashboard.component.html',
  styleUrls: ['../admin-dashboard/admin-dashboard.component.scss'] // Reusing admin styles
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
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        data: [1200, 1500, 800, 2200, 3000, 4500, 3800],
        label: 'Daily Revenue ($)',
        backgroundColor: '#10b981'
      }
    ]
  };

  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: ['Electronics', 'Clothing', 'Home', 'Books'],
    datasets: [
      {
        data: [45, 25, 20, 10],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'],
        borderWidth: 0
      }
    ]
  };

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.userEmail = user.email;
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
