import { Component, inject, signal, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Movimiento, MovimientosService } from './movimientos.service';
import { Table, TableColumn } from '../shared/table/table';
import { MovimientoFormModal } from './movimiento-form-modal/movimiento-form-modal';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-movimientos',
  imports: [Table, MatButtonModule, MatIconModule],
  templateUrl: './movimientos.html',
  styleUrl: './movimientos.scss',
})
export class Movimientos implements OnInit {
  private readonly movimientosService = inject(MovimientosService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected movimientos = signal<Movimiento[]>([]);
  protected loading = signal(false);

  protected columns: TableColumn[] = [
    { key: 'id', label: 'ID', sortable: true },
    { key: 'fecha', label: 'Fecha' },
    { key: 'tipo', label: 'Tipo' },
    { key: 'valor', label: 'Valor', sortable: true },
    { key: 'balance', label: 'Balance' },
    { key: 'numeroCuenta', label: 'Número de Cuenta' },
  ];

  ngOnInit(): void {
    this.loadMovimientos();
  }

  protected openCreate(): void {
    this.dialog.open(MovimientoFormModal, { data: {} }).afterClosed().subscribe(result => {
      if (!result) return;
      this.movimientosService.create(result).subscribe({
        next: () => this.loadMovimientos(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al crear el movimiento'),
      });
    });
  }

  protected openEdit(movimiento: Movimiento): void {
    this.dialog.open(MovimientoFormModal, { data: { movimiento } }).afterClosed().subscribe(result => {
      if (!result) return;
      this.movimientosService.update(movimiento.id, result).subscribe({
        next: () => this.loadMovimientos(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al actualizar el movimiento'),
      });
    });
  }

  protected delete(movimiento: Movimiento): void {
    if (!confirm(`¿Eliminar el movimiento #${movimiento.id}?`)) return;
    this.movimientosService.delete(movimiento.id).subscribe({
      next: () => this.loadMovimientos(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al eliminar el movimiento'),
    });
  }

  private loadMovimientos(): void {
    this.loading.set(true);
    this.movimientosService.getAll().subscribe({
      next: (data) => this.movimientos.set(data),
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al cargar los movimientos'),
      complete: () => this.loading.set(false),
    });
  }

  private showError(err: HttpErrorResponse, fallback: string): void {
    const message = err.error?.error ?? err.message ?? fallback;
    this.snackBar.open(message, 'Cerrar', { duration: 5000 });
  }
}
