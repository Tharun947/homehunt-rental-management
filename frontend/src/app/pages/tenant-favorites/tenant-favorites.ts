import { Component, signal } from '@angular/core';
import { Property } from '../../core/models/api.models';
import { FavoriteService } from '../../core/services/favorite.service';
import { PropertyCard } from '../../shared/property-card/property-card';

@Component({
  imports: [PropertyCard],
  templateUrl: './tenant-favorites.html',
  styleUrl: './tenant-favorites.scss'
})
export class TenantFavoritesPage {
  favorites = signal<Property[]>([]);
  message = signal('');

  constructor(private service: FavoriteService) {
    this.load();
  }

  load() {
    this.service.list().subscribe((properties) => this.favorites.set(properties));
  }

  remove(propertyId: number) {
    this.service.remove(propertyId).subscribe(() => {
      this.message.set('Property removed from Interested.');
      this.load();
    });
  }
}
