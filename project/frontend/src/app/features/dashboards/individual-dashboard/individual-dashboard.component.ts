import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service'; // EKLENDİ: ApiService importu
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
  totalOrders: number = 0;
  totalSpent: number = 0;
  totalReviews: number = 0;

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

  // SAHTE VERİLER SİLİNDİ - Başlangıçta boş, backend'den dolacak
  public spendData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Monthly Spend ($)',
        fill: true,
        tension: 0.4,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.3)'
      }
    ]
  };

  // SAHTE VERİLER SİLİNDİ - Başlangıçta boş, backend'den dolacak
  public categoryData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: ['#3b82f6', '#f59e0b', '#10b981'],
        borderWidth: 0
      }
    ]
  };

  // EKLENDİ: ApiService constructor'a dahil edildi
  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.userEmail = user.email;
      this.fetchDashboardData(user.id);
    }
  }

  // EKSİK OLAN METOD EKLENDİ: Backend'den veriyi çeken asıl fonksiyon
  fetchDashboardData(userId: number) {
    this.apiService.getIndividualDashboard(userId).subscribe({
      next: (response: any) => {
        // Gelen verileri grafiğe aktarıyoruz
        this.spendData.labels = response.spendLabels;
        this.spendData.datasets[0].data = response.spendValues;

        this.categoryData.labels = response.categoryLabels;
        this.categoryData.datasets[0].data = response.categoryValues;

        // Angular'ın grafiği yenilemesi (re-render) için objeleri güncelliyoruz
        this.spendData = { ...this.spendData };
        this.categoryData = { ...this.categoryData };

        // YENİ EKLENEN KPI VERİLERİ (Backend'den geliyorsa al, gelmiyorsa 0 yap)
        this.totalOrders = response.totalOrders || 0;
        this.totalSpent = response.totalSpent || 0;
        this.totalReviews = response.totalReviews || 0;

        // Eski grafik verileri (aynen kalacak)
        this.spendData.labels = response.spendLabels;
        this.spendData.datasets[0].data = response.spendValues;
        this.categoryData.labels = response.categoryLabels;
        this.categoryData.datasets[0].data = response.categoryValues;

        this.spendData = { ...this.spendData };
        this.categoryData = { ...this.categoryData };
      },
      error: (err) => {
        console.error('Dashboard verisi çekilirken hata oluştu:', err);
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}