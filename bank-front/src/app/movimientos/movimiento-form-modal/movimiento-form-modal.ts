import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Movimiento } from '../movimientos.service';

export interface MovimientoDialogData {
  movimiento?: Movimiento;
}

@Component({
  selector: 'app-movimiento-form-modal',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './movimiento-form-modal.html',
  styleUrl: './movimiento-form-modal.scss',
})
export class MovimientoFormModal {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<MovimientoFormModal>);
  protected readonly data = inject<MovimientoDialogData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.movimiento;

  protected readonly form = this.fb.nonNullable.group({
    fecha: [this.toDatetimeLocal(this.data?.movimiento?.fecha), Validators.required],
    tipo: [this.data?.movimiento?.tipo ?? '', Validators.required],
    valor: [this.data?.movimiento?.valor ?? null as unknown as number, [Validators.required]],
    numeroCuenta: [this.data?.movimiento?.numeroCuenta ?? null as unknown as number, [Validators.required, Validators.min(1)]],
  });

  /** Converts ISO string (2024-01-01T11:12:55Z) to datetime-local format (2024-01-01T11:12) */
  private toDatetimeLocal(iso?: string): string {
    if (!iso) return '';
    return iso.replace('Z', '').slice(0, 16);
  }

  /** Converts datetime-local value (2024-01-01T11:12) to ISO UTC string (2024-01-01T11:12:00Z) */
  private toIso(local: string): string {
    return local.length === 16 ? `${local}:00Z` : `${local}Z`;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.dialogRef.close({ ...value, fecha: this.toIso(value.fecha) });
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
