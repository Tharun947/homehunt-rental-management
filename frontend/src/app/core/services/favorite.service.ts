import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { API_URL } from './api-url';
import { Property } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  constructor(private http: HttpClient) {}

  add(propertyId: number) {
    return this.http.post<void>(`${API_URL}/favorites/${propertyId}`, {});
  }

  remove(propertyId: number) {
    return this.http.delete<void>(`${API_URL}/favorites/${propertyId}`);
  }

  list() {
    return this.http.get<Property[]>(`${API_URL}/favorites`);
  }
}
