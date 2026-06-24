import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_URL } from './api-url';
import { AuthResponse, Role } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'homehunt.auth';
  private readonly authState = signal<AuthResponse | null>(this.readAuth());
  readonly user = computed(() => this.authState());
  readonly isAuthenticated = computed(() => !!this.authState()?.token);
  readonly role = computed<Role | null>(() => this.authState()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email: email.trim(), password }).pipe(tap((auth) => this.store(auth)));
  }

  register(name: string, email: string, password: string, role: Role) {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, { name: name.trim(), email: email.trim(), password, role }).pipe(tap((auth) => this.store(auth)));
  }

  updateProfile(name: string, email: string) {
    return this.http.put<AuthResponse>(`${API_URL}/account/profile`, { name: name.trim(), email: email.trim() }).pipe(tap((auth) => this.store(auth)));
  }

  updatePassword(currentPassword: string, newPassword: string) {
    return this.http.put<void>(`${API_URL}/account/password`, { currentPassword, newPassword });
  }

  token() {
    return this.authState()?.token ?? null;
  }

  logout() {
    localStorage.removeItem(this.storageKey);
    this.authState.set(null);
    this.router.navigateByUrl('/login');
  }

  private store(auth: AuthResponse) {
    localStorage.setItem(this.storageKey, JSON.stringify(auth));
    this.authState.set(auth);
  }

  private readAuth(): AuthResponse | null {
    const raw = localStorage.getItem(this.storageKey);
    return raw ? JSON.parse(raw) as AuthResponse : null;
  }
}
