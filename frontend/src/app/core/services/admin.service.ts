import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API_URL } from './api-url';
import { DashboardStats, Page, Property, Role, UserSummary } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}

  stats() {
    return this.http.get<DashboardStats>(`${API_URL}/admin/stats`);
  }

  users() {
    return this.http.get<UserSummary[]>(`${API_URL}/admin/users`);
  }

  deleteUser(id: number) {
    return this.http.delete<void>(`${API_URL}/admin/users/${id}`);
  }

  updateRole(id: number, role: Role) {
    return this.http.put<UserSummary>(`${API_URL}/admin/users/${id}/role`, { role });
  }

  properties() {
    return this.http.get<Page<Property>>(`${API_URL}/admin/properties`);
  }

  deleteProperty(id: number) {
    return this.http.delete<void>(`${API_URL}/admin/properties/${id}`);
  }
}
