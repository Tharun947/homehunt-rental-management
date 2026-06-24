import { Component, signal } from '@angular/core';
import { RentalApplication } from '../../core/models/api.models';
import { ApplicationService } from '../../core/services/application.service';

@Component({
  templateUrl: './tenant-applications.html',
  styleUrl: './tenant-applications.scss'
})
export class TenantApplicationsPage {
  applications = signal<RentalApplication[]>([]);
  message = signal('');

  constructor(private service: ApplicationService) {
    this.load();
  }

  load() {
    this.service.mine().subscribe((applications) => this.applications.set(applications));
  }

  withdraw(id: number) {
    this.service.withdraw(id).subscribe(() => {
      this.message.set('Application removed.');
      this.load();
    });
  }
}
