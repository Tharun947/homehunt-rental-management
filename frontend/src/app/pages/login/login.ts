import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { FavoriteService } from '../../core/services/favorite.service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginPage {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private favorites = inject(FavoriteService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  error = signal('');
  form = this.fb.group({ email: ['', [Validators.required, Validators.email]], password: ['', Validators.required] });
  submit() {
    const v = this.form.getRawValue();
    this.auth.login(v.email!.trim(), v.password!).subscribe({
      next: (user) => {
        const interestedPropertyId = Number(this.route.snapshot.queryParamMap.get('interestedPropertyId'));
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        if (user.role === 'TENANT' && interestedPropertyId) {
          this.favorites.add(interestedPropertyId).subscribe({
            next: () => this.router.navigateByUrl('/tenant/interested'),
            error: () => this.router.navigateByUrl(returnUrl || `/properties/${interestedPropertyId}`)
          });
          return;
        }
        this.router.navigateByUrl(returnUrl || (user.role === 'TENANT' ? '/tenant' : user.role === 'LANDLORD' ? '/landlord' : '/admin'));
      },
      error: (err) => this.error.set(err.error?.message ?? 'Login failed')
    });
  }
}
