import { Component, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  protected items = signal<MenuItem[]>([
    { label: 'Clientes', icon: 'people', route: '/clientes' },
    { label: 'Cuentas', icon: 'account_balance', route: '/cuentas' },
    { label: 'Movimientos', icon: 'swap_horiz', route: '/movimientos' },
    { label: 'Reportes', icon: 'bar_chart', route: '/reportes' },
  ]);
}
