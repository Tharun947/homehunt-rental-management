import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  imports: [ReactiveFormsModule],
  templateUrl: './account.html',
  styleUrl: './account.scss'
})
export class AccountPage {
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  profileMessage = signal('');
  passwordMessage = signal('');
  error = signal('');

  profileForm = this.fb.group({
    name: [this.auth.user()?.name ?? '', Validators.required],
    email: [this.auth.user()?.email ?? '', [Validators.required, Validators.email]]
  });

  passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
  });

  saveProfile() {
    const value = this.profileForm.getRawValue();
    this.error.set('');
    this.auth.updateProfile(value.name ?? '', value.email ?? '').subscribe({
      next: () => this.profileMessage.set('Profile updated.'),
      error: (err) => this.error.set(err.error?.message ?? 'Profile update failed.')
    });
  }

  savePassword() {
    const value = this.passwordForm.getRawValue();
    this.error.set('');
    this.passwordMessage.set('');
    if (value.newPassword !== value.confirmPassword) {
      this.error.set('New passwords do not match.');
      return;
    }
    this.auth.updatePassword(value.currentPassword ?? '', value.newPassword ?? '').subscribe({
      next: () => {
        this.passwordMessage.set('Password updated.');
        this.passwordForm.reset();
      },
      error: (err) => this.error.set(err.error?.message ?? 'Password update failed.')
    });
  }
}
