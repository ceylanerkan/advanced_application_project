import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-store-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './store-settings.component.html',
  styleUrls: []
})
export class StoreSettingsComponent implements OnInit {
  store: any = null;
  storeName = '';
  saving = false;
  message = '';
  isError = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getStores().subscribe({
      next: (stores) => {
        if (stores.length > 0) {
          this.store = stores[0];
          this.storeName = this.store.name;
        }
      }
    });
  }

  save() {
    if (!this.store || !this.storeName.trim()) return;
    this.saving = true;
    this.apiService.updateStore(this.store.id, { name: this.storeName.trim(), status: this.store.status }).subscribe({
      next: (updated) => {
        this.store = updated;
        this.storeName = updated.name;
        this.isError = false;
        this.message = 'Store name updated successfully!';
        this.saving = false;
        setTimeout(() => this.message = '', 3000);
      },
      error: () => {
        this.isError = true;
        this.message = 'Failed to update store name.';
        this.saving = false;
        setTimeout(() => this.message = '', 3000);
      }
    });
  }
}