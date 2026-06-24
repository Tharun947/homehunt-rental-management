import { CurrencyPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApplicationStatus, Property, RentalApplication } from '../../core/models/api.models';
import { ApplicationService } from '../../core/services/application.service';
import { PropertyService } from '../../core/services/property.service';

@Component({
  imports: [CurrencyPipe, FormsModule],
  templateUrl: './landlord-applications.html',
  styleUrl: './landlord-applications.scss'
})
export class LandlordApplicationsPage {
  properties = signal<Property[]>([]);
  allApplications = signal<RentalApplication[]>([]);
  selectedPropertyId: number | 'all' = 'all';

  constructor(private applicationsService: ApplicationService, private propertyService: PropertyService) {
    this.propertyService.mine().subscribe((page) => {
      this.properties.set(page.content);
    });
    this.loadApplications();
  }

  selectedProperty() {
    return typeof this.selectedPropertyId === 'number'
      ? this.properties().find((property) => property.id === this.selectedPropertyId)
      : null;
  }

  loadApplications() {
    this.applicationsService.forLandlord().subscribe((apps) => this.allApplications.set(apps));
  }

  visibleApplications() {
    return this.selectedPropertyId === 'all'
      ? this.allApplications()
      : this.allApplications().filter((application) => application.propertyId === this.selectedPropertyId);
  }

  onPropertyChange() {
    this.visibleApplications();
  }

  setStatus(id: number, status: ApplicationStatus) {
    this.applicationsService.updateStatus(id, status).subscribe(() => this.loadApplications());
  }
}
