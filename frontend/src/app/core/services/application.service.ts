import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API_URL } from './api-url';
import { ApplicationStatus, RentalApplication } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  constructor(private http: HttpClient) {}

  apply(propertyId: number, income: number, notes: string) {
    return this.http.post<RentalApplication>(`${API_URL}/applications`, { propertyId, income, notes });
  }

  mine() {
    return this.http.get<RentalApplication[]>(`${API_URL}/applications/my`);
  }

  forProperty(propertyId: number) {
    return this.http.get<RentalApplication[]>(`${API_URL}/applications/property/${propertyId}`);
  }

  forLandlord() {
    return this.http.get<RentalApplication[]>(`${API_URL}/applications/landlord`);
  }

  updateStatus(id: number, status: ApplicationStatus) {
    return this.http.put<RentalApplication>(`${API_URL}/applications/${id}/status`, { status });
  }

  withdraw(id: number) {
    return this.http.delete<void>(`${API_URL}/applications/${id}`);
  }
}
