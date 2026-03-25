import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TransactionService } from '../../../core/services/transaction.service';

@Component({
  selector: 'app-deposit-withdraw',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="operations">
      <form [formGroup]="form" class="op-form">
        <input formControlName="amount" type="number" min="0.01" step="0.01" placeholder="Montant" />
        <select formControlName="currencyCode">
          <option value="EUR">EUR</option>
          <option value="USD">USD</option>
          <option value="GBP">GBP</option>
        </select>
        <button type="button" (click)="deposit()" [disabled]="form.invalid" class="btn-deposit">Déposer</button>
        <button type="button" (click)="withdraw()" [disabled]="form.invalid" class="btn-withdraw">Retirer</button>
      </form>
      @if (errorMessage) {
        <p class="error-banner">{{ errorMessage }}</p>
      }
    </div>
  `,
  styles: [`
    .operations { margin: 1.5rem 0; padding: 1rem; background: #f7fafc; border-radius: 8px; }
    .op-form { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
    input, select { padding: 0.5rem 0.75rem; border: 1px solid #cbd5e0; border-radius: 6px; }
    .btn-deposit { background: #2d6a4f; color: white; padding: 0.5rem 1rem; border: none; border-radius: 6px; cursor: pointer; }
    .btn-withdraw { background: #c53030; color: white; padding: 0.5rem 1rem; border: none; border-radius: 6px; cursor: pointer; }
    button:disabled { opacity: 0.5; cursor: not-allowed; }
    .error-banner { color: #c53030; margin-top: 0.5rem; }
  `]
})
export class DepositWithdrawComponent {
  @Input() accountId = '';
  @Output() operationDone = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private transactionService = inject(TransactionService);

  errorMessage = '';

  form = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    currencyCode: ['EUR', Validators.required]
  });

  deposit() {
    if (this.form.invalid) return;
    this.errorMessage = '';
    const { amount, currencyCode } = this.form.value;
    this.transactionService.deposit(this.accountId, { amount: amount!, currencyCode: currencyCode! })
      .subscribe({
        next: () => { this.form.reset({ currencyCode: 'EUR' }); this.operationDone.emit(); },
        error: err => this.errorMessage = err.error?.message ?? 'Erreur lors du dépôt.'
      });
  }

  withdraw() {
    if (this.form.invalid) return;
    this.errorMessage = '';
    const { amount, currencyCode } = this.form.value;
    this.transactionService.withdraw(this.accountId, { amount: amount!, currencyCode: currencyCode! })
      .subscribe({
        next: () => { this.form.reset({ currencyCode: 'EUR' }); this.operationDone.emit(); },
        error: err => this.errorMessage = err.error?.message ?? 'Erreur lors du retrait.'
      });
  }
}
