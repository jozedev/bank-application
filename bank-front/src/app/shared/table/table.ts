import { Component, input, output, effect, viewChild, contentChild, TemplateRef } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

export interface TableColumn {
  /** Property name in the data object */
  key: string;
  /** Displayed header text */
  label: string;
  /** Whether the column is sortable */
  sortable?: boolean;
  /** Render cell as a slide toggle */
  type?: 'text' | 'toggle';
}

export interface ToggleChangeEvent<T> {
  row: T;
  value: boolean;
}

@Component({
  selector: 'app-table',
  imports: [
    NgTemplateOutlet,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
  ],
  templateUrl: './table.html',
  styleUrl: './table.scss',
})
export class Table<T extends object> {
  columns = input.required<TableColumn[]>();
  data = input<T[]>([]);
  pageSize = input<number>(10);
  pageSizeOptions = input<number[]>([5, 10, 25, 50]);
  loading = input<boolean>(false);

  /** Optional template for an actions column. When provided, an 'acciones' column is added. */
  readonly actionsTemplate = contentChild<TemplateRef<{ $implicit: T }>>(TemplateRef);

  /** Emitted when a toggle-type column changes value. */
  readonly toggleChange = output<ToggleChangeEvent<T>>();

  protected readonly paginator = viewChild(MatPaginator);
  protected readonly sort = viewChild(MatSort);

  protected readonly dataSource = new MatTableDataSource<T>([]);

  protected get displayedColumns(): string[] {
    const cols = this.columns().map(c => c.key);
    return this.actionsTemplate() ? [...cols, 'acciones'] : cols;
  }

  constructor() {
    effect(() => {
      this.dataSource.data = this.data();
    });

    effect(() => {
      this.dataSource.paginator = this.paginator() ?? null;
    });

    effect(() => {
      this.dataSource.sort = this.sort() ?? null;
    });
  }
}
