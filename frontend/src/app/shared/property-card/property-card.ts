import { CurrencyPipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Property } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PropertyService } from '../../core/services/property.service';

@Component({
  selector: 'app-property-card',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './property-card.html',
  styleUrl: './property-card.scss'
})
export class PropertyCard {
  @Input({ required: true }) property!: Property;
  @Input() showInterestAction = true;
  fallback = 'https://images.unsplash.com/photo-1560518883-ce09059eeffa';

  constructor(private properties: PropertyService, public auth: AuthService) {}

  imageSrc() {
    return this.properties.toAbsoluteImageUrl(this.property.imageUrl) || this.fallback;
  }
}
