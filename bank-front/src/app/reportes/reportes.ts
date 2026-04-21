import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportesService, CuentaReporte } from './reportes.service';

@Component({
  selector: 'app-reportes',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    DecimalPipe,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatExpansionModule,
    MatChipsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class Reportes {
  private readonly reportesService = inject(ReportesService);
  private readonly fb = inject(FormBuilder);

  protected reportes = signal<CuentaReporte[]>([]);
  protected loading = signal(false);
  protected searched = signal(false);

  protected readonly movimientoColumns = ['id', 'fecha', 'tipo', 'valor'];

  protected readonly form = this.fb.nonNullable.group({
    clienteId: [null as unknown as number, [Validators.required, Validators.min(1)]],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
  });

  protected buscar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { clienteId, fechaInicio, fechaFin } = this.form.getRawValue();
    this.loading.set(true);
    this.searched.set(false);
    this.reportesService
      .getReporte(clienteId, this.toIso(fechaInicio), this.toIso(fechaFin))
      .subscribe({
        next: (data) => { this.reportes.set(data); this.searched.set(true); },
        error: () => { this.loading.set(false); this.searched.set(true); },
        complete: () => this.loading.set(false),
      });
  }

  protected exportarPDF(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { clienteId, fechaInicio, fechaFin } = this.form.getRawValue();
    this.loading.set(true);
    this.reportesService
      .getReportePDF(clienteId, this.toIso(fechaInicio), this.toIso(fechaFin))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `reporte_${clienteId}_${fechaInicio}_${fechaFin}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: () => { this.loading.set(false); this.searched.set(true); },
        complete: () => this.loading.set(false),
      });
  }

  private toIso(local: string): string {
    if (local.includes('Z')) return local;
    return local.length === 16 ? `${local}:00Z` : `${local}Z`;
  }
}

