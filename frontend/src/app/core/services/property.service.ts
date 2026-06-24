import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from './api-url';
import { Page, Property, PropertyType } from '../models/api.models';

export interface PropertyFilters {
  q?: string;
  location?: string;
  minPrice?: string | number;
  maxPrice?: string | number;
  type?: PropertyType | '';
  sort?: 'newest' | 'price' | '-price';
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class PropertyService {
  constructor(private http: HttpClient) {}

  list(filters: PropertyFilters = {}) {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<Page<Property>>(`${API_URL}/properties`, { params });
  }

  get(id: number) {
    return this.http.get<Property>(`${API_URL}/properties/${id}`);
  }

  mine() {
    return this.http.get<Page<Property>>(`${API_URL}/properties/my`);
  }

  pending() {
    return this.http.get<Page<Property>>(`${API_URL}/properties/pending`);
  }

  create(payload: Partial<Property>) {
    return this.http.post<Property>(`${API_URL}/properties`, payload);
  }

  update(id: number, payload: Partial<Property>) {
    return this.http.put<Property>(`${API_URL}/properties/${id}`, payload);
  }

  delete(id: number) {
    return this.http.delete<void>(`${API_URL}/properties/${id}`);
  }

  approve(id: number) {
    return this.http.put<Property>(`${API_URL}/properties/${id}/approve`, {});
  }

  reject(id: number) {
    return this.http.put<Property>(`${API_URL}/properties/${id}/reject`, {});
  }

  uploadImage(file: File) {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imageUrl: string }>(`${API_URL}/uploads/images`, form).pipe(
      map((response) => ({ imageUrl: this.toAbsoluteImageUrl(response.imageUrl) }))
    );
  }

  toAbsoluteImageUrl(imageUrl?: string) {
    if (!imageUrl || imageUrl.startsWith('http') || imageUrl.startsWith('data:')) {
      return imageUrl;
    }
    return `${API_URL.endsWith('/api') ? API_URL.slice(0, -4) : ''}${imageUrl}`;
  }
}
