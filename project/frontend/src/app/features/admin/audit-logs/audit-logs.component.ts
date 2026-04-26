import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-logs.component.html',
  styleUrls: []
})
export class AuditLogsComponent implements OnInit {
  logs: any[] = [];
  filteredLogs: any[] = [];
  actionFilter = '';

  ngOnInit() {
    const actions = ['LOGIN', 'CREATE_PRODUCT', 'UPDATE_ORDER', 'DELETE_USER', 'APPROVE_STORE', 'CHANGE_ROLE', 'EXPORT_DATA'];
    this.logs = Array.from({ length: 30 }, (_, i) => ({
      id: i + 1,
      user: `user${(i % 10) + 1}@example.com`,
      action: actions[i % actions.length],
      target: `Resource #${100 + i}`,
      timestamp: `2026-04-${String(25 - (i % 25)).padStart(2, '0')}T${String(8 + (i % 12)).padStart(2, '0')}:${String(i * 2 % 60).padStart(2, '0')}:00Z`,
      ip: `192.168.1.${i + 1}`
    }));
    this.filteredLogs = this.logs;
  }

  applyFilter() {
    this.filteredLogs = this.actionFilter
      ? this.logs.filter(l => l.action === this.actionFilter)
      : this.logs;
  }

  get uniqueActions(): string[] {
    return [...new Set(this.logs.map(l => l.action))];
  }
}
