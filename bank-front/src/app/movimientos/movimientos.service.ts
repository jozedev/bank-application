import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Movimiento {
  id: number;
  fecha: string;
  tipo: string;
  valor: number;
  balance: number;
  numeroCuenta: number;
}

@Injectable({ providedIn: 'root' })
export class MovimientosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8081/movimientos';

  getAll(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.baseUrl);
  }

  create(movimiento: Omit<Movimiento, 'id' | 'balance'>): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.baseUrl, movimiento);
  }

  update(id: number, movimiento: Omit<Movimiento, 'id' | 'balance'>): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.baseUrl}/${id}`, movimiento);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
