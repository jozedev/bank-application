import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { Cliente } from '../clientes.service';

export interface ClienteDialogData {
  cliente?: Cliente;
}

@Component({
  selector: 'app-cliente-form-modal',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
  ],
  templateUrl: './cliente-form-modal.html',
  styleUrl: './cliente-form-modal.scss',
})
export class ClienteFormModal {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ClienteFormModal>);
  protected readonly data = inject<ClienteDialogData>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.cliente;

  protected readonly form = this.fb.nonNullable.group({
    nombre: [this.data?.cliente?.nombre ?? '', [Validators.required, Validators.maxLength(100)]],
    genero: [this.data?.cliente?.genero ?? '', Validators.required],
    edad: [this.data?.cliente?.edad ?? null as unknown as number, [Validators.required, Validators.min(0), Validators.max(150)]],
    identificacion: [this.data?.cliente?.identificacion ?? '', [Validators.required, Validators.maxLength(20)]],
    direccion: [this.data?.cliente?.direccion ?? '', [Validators.required, Validators.maxLength(200)]],
    telefono: [this.data?.cliente?.telefono ?? '', [Validators.required, Validators.maxLength(20)]],
    contrasena: [this.data?.cliente?.contrasena ?? '', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
    estado: [this.data?.cliente?.estado ?? true],
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
