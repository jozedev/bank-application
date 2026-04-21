import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MovimientoReporte {
  id: number;
  fecha: string;
  tipo: string;
  valor: number;
}

export interface CuentaReporte {
  fecha: string;
  cliente: string;
  numeroCuenta: number;
  tipo: string;
  saldo: number;
  estado: boolean;
  movimiento: MovimientoReporte[];
}

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8081/movimientos/clientes';

  getReporte(clienteId: number, fechaInicio: string, fechaFin: string): Observable<CuentaReporte[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<CuentaReporte[]>(`${this.baseUrl}/${clienteId}/reporte`, { params });
  }

  getReportePDF(clienteId: number, fechaInicio: string, fechaFin: string): Observable<Blob> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get(`${this.baseUrl}/${clienteId}/reporte/pdf`, { params, responseType: 'blob' });
  }
}
