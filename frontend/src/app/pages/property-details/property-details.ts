import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { ApplicationStatus, Property } from '../../core/models/api.models';
import { ApplicationService } from '../../core/services/application.service';
import { AuthService } from '../../core/services/auth.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { PropertyService } from '../../core/services/property.service';

@Component({
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './property-details.html',
  styleUrl: './property-details.scss'
})
export class PropertyDetailsPage {
  private fb = inject(FormBuilder);
  private applications = inject(ApplicationService);
  private favorites = inject(FavoriteService);
  private properties = inject(PropertyService);
  public auth = inject(AuthService);

  property = signal<Property | null>(null);
  message = signal('');
  isFavorite = signal(false);
  hasApplied = signal(false);
  applicationStatus = signal<ApplicationStatus | ''>('');
  savingFavorite = signal(false);
  applying = signal(false);
  fallback = 'https://images.unsplash.com/photo-1560518883-ce09059eeffa';
  form = this.fb.group({ income: [null, [Validators.required, Validators.min(1)]], notes: [''] });

  constructor(route: ActivatedRoute) {
    route.paramMap
      .pipe(switchMap((params) => this.properties.get(Number(params.get('id')))))
      .subscribe((property) => {
        this.property.set(property);
        this.loadTenantState(property.id);
      });
  }

  imageSrc(imageUrl?: string) {
    return this.properties.toAbsoluteImageUrl(imageUrl) || this.fallback;
  }

  apply(propertyId: number) {
    const value = this.form.value;
    this.applying.set(true);
    this.applications.apply(propertyId, Number(value.income), value.notes ?? '').subscribe({
      next: (application) => {
        this.hasApplied.set(true);
        this.applicationStatus.set(application.status);
        this.message.set('Application submitted.');
        this.applying.set(false);
      },
      error: (err) => {
        this.message.set(err.error?.message ?? 'Application failed.');
        this.applying.set(false);
      }
    });
  }

  save(propertyId: number) {
    this.savingFavorite.set(true);
    this.favorites.add(propertyId).subscribe({
      next: () => {
        this.isFavorite.set(true);
        this.message.set('Marked as interested.');
        this.savingFavorite.set(false);
      },
      error: (err) => {
        this.message.set(err.error?.message ?? 'Could not save favorite.');
        this.savingFavorite.set(false);
      }
    });
  }

  removeFavorite(propertyId: number) {
    this.savingFavorite.set(true);
    this.favorites.remove(propertyId).subscribe({
      next: () => {
        this.isFavorite.set(false);
        this.message.set('Removed from Interested.');
        this.savingFavorite.set(false);
      },
      error: (err) => {
        this.message.set(err.error?.message ?? 'Could not remove favorite.');
        this.savingFavorite.set(false);
      }
    });
  }

  private loadTenantState(propertyId: number) {
    this.isFavorite.set(false);
    this.hasApplied.set(false);
    this.applicationStatus.set('');
    if (this.auth.role() !== 'TENANT') {
      return;
    }
    this.favorites.list().subscribe((properties) => {
      this.isFavorite.set(properties.some((property) => property.id === propertyId));
    });
    this.applications.mine().subscribe((applications) => {
      const application = applications.find((item) => item.propertyId === propertyId);
      this.hasApplied.set(!!application);
      this.applicationStatus.set(application?.status ?? '');
    });
  }
}
