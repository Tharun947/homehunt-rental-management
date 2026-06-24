import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Role } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterPage {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  error = signal('');
  form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['TENANT' as Role, Validators.required]
  });
  submit() {
    const v = this.form.getRawValue();
    this.auth.register(v.name!.trim(), v.email!.trim(), v.password!, v.role!).subscribe({
      next: (user) => this.router.navigateByUrl(user.role === 'TENANT' ? '/tenant' : user.role === 'LANDLORD' ? '/landlord' : '/admin'),
      error: (err) => this.error.set(err.error?.message ?? 'Registration failed')
    });
  }
}
