import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction.service';
import { Transaction } from '../../../core/models/transaction.model';
import { DepositWithdrawComponent } from '../deposit-withdraw/deposit-withdraw.component';

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, RouterLink, DepositWithdrawComponent],
  template: `
    <div class="page">
      <a routerLink="/accounts" class="back-link">← Retour aux comptes</a>
      <h1>Transactions</h1>

      <app-deposit-withdraw
        [accountId]="accountId"
        (operationDone)="reload()" />

      @if (transactions().length === 0) {
        <p class="empty">Aucune transaction pour ce compte.</p>
      } @else {
        <table class="tx-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Montant</th>
              <th>Solde après</th>
            </tr>
          </thead>
          <tbody>
            @for (tx of transactions(); track tx.transactionId) {
              <tr [class]="tx.type === 'DEPOSIT' ? 'deposit' : 'withdrawal'">
                <td>{{ tx.occurredAt | date:'short' }}</td>
                <td>{{ tx.type === 'DEPOSIT' ? 'Dépôt' : 'Retrait' }}</td>
                <td>{{ tx.amount | number:'1.2-2' }} {{ tx.currencyCode }}</td>
                <td>{{ tx.balanceAfter | number:'1.2-2' }} {{ tx.currencyCode }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 800px; margin: 2rem auto; padding: 0 1rem; }
    .back-link { color: #2d6a4f; text-decoration: none; font-size: 0.9rem; }
    .empty { color: #718096; margin-top: 1rem; }
    .tx-table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; }
    th { text-align: left; padding: 0.75rem; background: #f7fafc; border-bottom: 2px solid #e2e8f0; }
    td { padding: 0.75rem; border-bottom: 1px solid #e2e8f0; }
    tr.deposit td:nth-child(3) { color: #2d6a4f; }
    tr.withdrawal td:nth-child(3) { color: #c53030; }
  `]
})
export class TransactionHistoryComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private transactionService = inject(TransactionService);

  accountId = '';
  readonly transactions = signal<Transaction[]>([]);

  ngOnInit() {
    this.accountId = this.route.snapshot.paramMap.get('id')!;
    this.reload();
  }

  reload() {
    this.transactionService.getHistory(this.accountId).subscribe(
      history => this.transactions.set(history.transactions)
    );
  }
}
