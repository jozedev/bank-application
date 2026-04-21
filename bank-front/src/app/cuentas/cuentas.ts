import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Cuenta, CuentasService } from './cuentas.service';
import { Table, TableColumn, ToggleChangeEvent } from '../shared/table/table';
import { CuentaFormModal } from './cuenta-form-modal/cuenta-form-modal';

@Component({
  selector: 'app-cuentas',
  imports: [Table, MatButtonModule, MatIconModule],
  templateUrl: './cuentas.html',
  styleUrl: './cuentas.scss',
})
export class Cuentas implements OnInit {
  private readonly cuentasService = inject(CuentasService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected cuentas = signal<Cuenta[]>([]);
  protected loading = signal(false);

  protected columns: TableColumn[] = [
    { key: 'numeroCuenta', label: 'Número de Cuenta', sortable: true },
    { key: 'tipo', label: 'Tipo' },
    { key: 'saldoInicial', label: 'Saldo Inicial', sortable: true },
    { key: 'estado', label: 'Estado', type: 'toggle' },
  ];

  ngOnInit(): void {
    this.loadCuentas();
  }

  protected openCreate(): void {
    this.dialog.open(CuentaFormModal, { data: {} }).afterClosed().subscribe(result => {
      if (!result) return;
      this.cuentasService.create(result).subscribe({
        next: () => this.loadCuentas(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al crear la cuenta'),
      });
    });
  }

  protected openEdit(cuenta: Cuenta): void {
    this.dialog.open(CuentaFormModal, { data: { cuenta } }).afterClosed().subscribe(result => {
      if (!result) return;
      this.cuentasService.update(cuenta.numeroCuenta, result).subscribe({
        next: () => this.loadCuentas(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al actualizar la cuenta'),
      });
    });
  }

  protected onToggleEstado(event: ToggleChangeEvent<Cuenta>): void {
    this.cuentasService.updateState(event.row.numeroCuenta, event.value).subscribe({
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al cambiar el estado'),
    });
  }

  protected delete(cuenta: Cuenta): void {
    if (!confirm(`¿Eliminar la cuenta ${cuenta.numeroCuenta}?`)) return;
    this.cuentasService.delete(cuenta.numeroCuenta).subscribe({
      next: () => this.loadCuentas(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al eliminar la cuenta'),
    });
  }

  private loadCuentas(): void {
    this.loading.set(true);
    this.cuentasService.getAll().subscribe({
      next: (data) => this.cuentas.set(data),
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.showError(err, 'Error al cargar las cuentas');
      },
      complete: () => this.loading.set(false),
    });
  }

  private showError(err: HttpErrorResponse, fallback: string): void {
    const message = err.error?.error ?? err.message ?? fallback;
    this.snackBar.open(message, 'Cerrar', { duration: 5000 });
  }
}
