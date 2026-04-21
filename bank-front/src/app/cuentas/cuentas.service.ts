import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Cuenta {
  numeroCuenta: number;
  tipo: string;
  saldoInicial: number;
  estado: boolean;
  cliente: number;
}

@Injectable({ providedIn: 'root' })
export class CuentasService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8081/cuentas';

  getAll(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.baseUrl);
  }

  create(cuenta: Omit<Cuenta, 'numeroCuenta'>): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.baseUrl, cuenta);
  }

  update(numeroCuenta: number, cuenta: Omit<Cuenta, 'numeroCuenta'>): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.baseUrl}/${numeroCuenta}`, cuenta);
  }

  updateState(numeroCuenta: number, estado: boolean): Observable<Cuenta> {
    return this.http.patch<Cuenta>(`${this.baseUrl}/${numeroCuenta}`, { estado });
  }

  delete(numeroCuenta: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${numeroCuenta}`);
  }
}
