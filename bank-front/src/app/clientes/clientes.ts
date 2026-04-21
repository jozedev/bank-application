import { Component, inject, signal, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Table, TableColumn, ToggleChangeEvent } from '../shared/table/table';
import { ClientesService, Cliente } from './clientes.service';
import { ClienteFormModal } from './cliente-form-modal/cliente-form-modal';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-clientes',
  imports: [Table, MatButtonModule, MatIconModule],
  templateUrl: './clientes.html',
  styleUrl: './clientes.scss',
})
export class Clientes implements OnInit {
  private readonly clientesService = inject(ClientesService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected clientes = signal<Cliente[]>([]);
  protected loading = signal(false);

  protected columns: TableColumn[] = [
    { key: 'clienteId', label: 'ID', sortable: true },
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'genero', label: 'Género' },
    { key: 'edad', label: 'Edad', sortable: true },
    { key: 'identificacion', label: 'Identificación' },
    { key: 'direccion', label: 'Dirección' },
    { key: 'telefono', label: 'Teléfono' },
    { key: 'estado', label: 'Estado', type: 'toggle' },
  ];

  ngOnInit(): void {
    this.loadClientes();
  }

  protected openCreate(): void {
    this.dialog.open(ClienteFormModal, { data: {} }).afterClosed().subscribe(result => {
      if (!result) return;
      this.clientesService.create(result).subscribe({
        next: () => this.loadClientes(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al crear el cliente'),
      });
    });
  }

  protected openEdit(cliente: Cliente): void {
    this.dialog.open(ClienteFormModal, { data: { cliente } }).afterClosed().subscribe(result => {
      if (!result) return;
      this.clientesService.update(cliente.clienteId, result).subscribe({
        next: () => this.loadClientes(),
        error: (err: HttpErrorResponse) => this.showError(err, 'Error al actualizar el cliente'),
      });
    });
  }

  protected onToggleEstado(event: ToggleChangeEvent<Cliente>): void {
    this.clientesService.updateState(event.row.clienteId, event.value).subscribe();
  }

  protected delete(cliente: Cliente): void {
    if (!confirm(`¿Eliminar a ${cliente.nombre}?`)) return;
    this.clientesService.delete(cliente.clienteId).subscribe({
      next: () => this.loadClientes(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al eliminar el cliente'),
    });
  }

  private loadClientes(): void {
    this.loading.set(true);
    this.clientesService.getAll().subscribe({
      next: (data) => this.clientes.set(data),
      error: (err: HttpErrorResponse) => this.showError(err, 'Error al cargar los clientes'),
      complete: () => this.loading.set(false),
    });
  }

  private showError(err: HttpErrorResponse, fallback: string): void {
    const message = err.error?.error ?? err.message ?? fallback;
    this.snackBar.open(message, 'Cerrar', { duration: 5000 });
  }
}
