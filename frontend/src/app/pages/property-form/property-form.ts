import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, of, switchMap } from 'rxjs';
import { PropertyService } from '../../core/services/property.service';

@Component({
  imports: [ReactiveFormsModule],
  templateUrl: './property-form.html',
  styleUrl: './property-form.scss'
})
export class PropertyFormPage {
  private fb = inject(FormBuilder);
  propertyId?: number;
  error = signal('');
  uploading = signal(false);
  previewUrl = signal('');
  selectedFile?: File;
  form = this.fb.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    location: ['', Validators.required],
    price: [null, [Validators.required, Validators.min(1)]],
    type: ['APARTMENT', Validators.required],
    imageUrl: ['']
  });

  constructor(route: ActivatedRoute, private properties: PropertyService, private router: Router) {
    const id = route.snapshot.paramMap.get('id');
    if (id) {
      this.propertyId = Number(id);
      this.properties.get(this.propertyId).subscribe((property) => {
        this.form.patchValue(property as any);
        this.previewUrl.set(this.properties.toAbsoluteImageUrl(property.imageUrl) ?? '');
      });
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.selectedFile = file;
    this.previewUrl.set(URL.createObjectURL(file));
  }

  submit() {
    this.error.set('');
    this.uploading.set(true);
    const payload = this.form.getRawValue() as any;
    const upload$ = this.selectedFile ? this.properties.uploadImage(this.selectedFile) : of({ imageUrl: payload.imageUrl });
    upload$.pipe(
      switchMap(({ imageUrl }) => {
        const propertyPayload = { ...payload, imageUrl };
        return this.propertyId
          ? this.properties.update(this.propertyId, propertyPayload)
          : this.properties.create(propertyPayload);
      }),
      finalize(() => this.uploading.set(false))
    ).subscribe({
      next: () => this.router.navigate(['/landlord/properties'], { queryParams: { saved: '1' } }),
      error: (err) => this.error.set(err.error?.message ?? 'Save failed')
    });
  }
}
