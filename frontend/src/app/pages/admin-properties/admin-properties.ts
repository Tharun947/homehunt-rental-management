import { CurrencyPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { Page, Property } from '../../core/models/api.models';
import { AdminService } from '../../core/services/admin.service';

@Component({
  imports: [CurrencyPipe],
  templateUrl: './admin-properties.html',
  styleUrl: './admin-properties.scss'
})
export class AdminPropertiesPage {
  properties = signal<Page<Property> | null>(null);
  message = signal('');

  constructor(private admin: AdminService) {
    this.load();
  }

  load() {
    this.admin.properties().subscribe((page) => this.properties.set(page));
  }

  deleteProperty(id: number) {
    this.admin.deleteProperty(id).subscribe(() => {
      this.message.set('Property removed.');
      this.load();
    });
  }
}
