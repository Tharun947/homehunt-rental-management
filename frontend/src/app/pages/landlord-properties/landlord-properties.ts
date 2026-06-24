import { CurrencyPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Page, Property } from '../../core/models/api.models';
import { PropertyService } from '../../core/services/property.service';

@Component({
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './landlord-properties.html',
  styleUrl: './landlord-properties.scss'
})
export class LandlordPropertiesPage {
  page = signal<Page<Property> | null>(null);
  message = signal('');

  constructor(private properties: PropertyService, route: ActivatedRoute) {
    if (route.snapshot.queryParamMap.get('saved')) {
      this.message.set('Property submitted for admin review.');
    }
    this.load();
  }

  load() {
    this.properties.mine().subscribe((page) => this.page.set(page));
  }

  remove(id: number) {
    this.properties.delete(id).subscribe(() => this.load());
  }
}
