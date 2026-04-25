import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-individual-dashboard',
  standalone: true,
  imports: [RouterModule, NgChartsModule],
  templateUrl: './individual-dashboard.component.html',
  styleUrls: [] // Reusing admin styles
})
export class IndividualDashboardComponent implements OnInit {
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
  
  public doughnutOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#f8fafc' } } }
  };

  public spendData: ChartConfiguration<'line'>['data'] = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      {
        data: [150, 0, 300, 100, 250, 450],
        label: 'Monthly Spend ($)',
        fill: true,
        tension: 0.4,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.3)'
      }
    ]
  };

  public categoryData: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Electronics', 'Books', 'Clothing'],
    datasets: [
      {
        data: [600, 150, 500],
        backgroundColor: ['#3b82f6', '#f59e0b', '#10b981'],
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
