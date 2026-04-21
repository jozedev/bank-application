import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { Cuenta } from '../cuentas.service';

export interface CuentaDialogData {
  cuenta?: Cuenta;
}

@Component({
  selector: 'app-cuenta-form-modal',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
  ],
  templateUrl: './cuenta-form-modal.html',
  styleUrl: './cuenta-form-modal.scss',
})
export class CuentaFormModal {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CuentaFormModal>);
  protected readonly data = inject<CuentaDialogData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.cuenta;

  protected readonly form = this.fb.nonNullable.group({
    tipo: [this.data?.cuenta?.tipo ?? '', Validators.required],
    saldoInicial: [this.data?.cuenta?.saldoInicial ?? null as unknown as number, [Validators.required, Validators.min(0)]],
    estado: [this.data?.cuenta?.estado ?? true],
    clienteId: [this.data?.cuenta?.cliente ?? '', Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue());
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
