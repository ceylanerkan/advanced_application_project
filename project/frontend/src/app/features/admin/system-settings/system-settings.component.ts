import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-system-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './system-settings.component.html',
  styleUrls: []
})
export class SystemSettingsComponent {
  settings = {
    platformName: 'E-Commerce Analytics Platform',
    defaultCurrency: 'USD',
    maxProductsPerStore: 500,
    enableRegistration: true,
    maintenanceMode: false,
    sessionTimeout: 30
  };

  save() {
    alert('Settings saved successfully!');
  }
}
