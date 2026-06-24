import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Page, Property } from '../../core/models/api.models';
import { PropertyService, PropertyFilters } from '../../core/services/property.service';
import { PropertyCard } from '../../shared/property-card/property-card';

@Component({
  imports: [ReactiveFormsModule, PropertyCard],
  templateUrl: './properties.html',
  styleUrl: './properties.scss'
})
export class PropertiesPage {
  private fb = inject(FormBuilder);
  private properties = inject(PropertyService);
  loading = signal(false);
  page = signal<Page<Property> | null>(null);
  filters = this.fb.group({
    q: [''],
    location: [''],
    minPrice: [''],
    maxPrice: [''],
    type: [''],
    sort: ['newest']
  });

  constructor() {
    this.search();
  }

  search() {
    this.loading.set(true);
    this.properties.list(this.cleanFilters()).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => {
        this.page.set({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 12 });
        this.loading.set(false);
      }
    });
  }

  clear() {
    this.filters.reset({ q: '', location: '', minPrice: '', maxPrice: '', type: '', sort: 'newest' });
    this.search();
  }

  private cleanFilters(): PropertyFilters {
    const raw = this.filters.getRawValue();
    return {
      q: raw.q?.trim(),
      location: raw.location?.trim(),
      minPrice: raw.minPrice ?? '',
      maxPrice: raw.maxPrice ?? '',
      type: raw.type as PropertyFilters['type'],
      sort: raw.sort as PropertyFilters['sort']
    };
  }
}
