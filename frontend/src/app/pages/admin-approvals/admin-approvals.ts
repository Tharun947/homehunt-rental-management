import { CurrencyPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { Page, Property } from '../../core/models/api.models';
import { AdminService } from '../../core/services/admin.service';
import { PropertyService } from '../../core/services/property.service';

@Component({
  imports: [CurrencyPipe],
  templateUrl: './admin-approvals.html',
  styleUrl: './admin-approvals.scss'
})
export class AdminApprovalsPage {
  pending = signal<Page<Property> | null>(null);
  message = signal('');
  fallback = 'https://images.unsplash.com/photo-1560518883-ce09059eeffa';

  constructor(private properties: PropertyService, private admin: AdminService) {
    this.load();
  }

  load() {
    this.properties.pending().subscribe((page) => this.pending.set(page));
  }

  imageSrc(imageUrl?: string) {
    return this.properties.toAbsoluteImageUrl(imageUrl) || this.fallback;
  }

  approve(id: number) {
    this.properties.approve(id).subscribe({
      next: () => {
        this.message.set('Property approved.');
        this.load();
      },
      error: (err) => this.message.set(err.error?.message ?? 'Approval failed.')
    });
  }

  reject(id: number) {
    this.properties.reject(id).subscribe({
      next: () => {
        this.message.set('Property rejected.');
        this.load();
      },
      error: (err) => this.message.set(err.error?.message ?? 'Reject failed.')
    });
  }

  deleteProperty(id: number) {
    this.admin.deleteProperty(id).subscribe({
      next: () => {
        this.message.set('Property removed.');
        this.load();
      },
      error: (err) => this.message.set(err.error?.message ?? 'Delete failed.')
    });
  }
}
