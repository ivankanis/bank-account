import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AccountService } from '../../../core/services/account.service';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <header class="page-header">
        <h1>Comptes bancaires</h1>
        <a routerLink="/accounts/new" class="btn-primary">+ Nouveau compte</a>
      </header>

      @if (accounts().length === 0) {
        <p class="empty">Aucun compte. <a routerLink="/accounts/new">Créez le premier.</a></p>
      } @else {
        <ul class="account-list">
          @for (account of accounts(); track account.accountId) {
            <li class="account-card">
              <div class="account-info">
                <span class="owner">{{ account.ownerName }}</span>
                <span class="balance">{{ account.balance | number:'1.2-2' }} {{ account.currencyCode }}</span>
              </div>
              <a [routerLink]="['/accounts', account.accountId, 'history']" class="btn-secondary">
                Voir transactions
              </a>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 720px; margin: 2rem auto; padding: 0 1rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .account-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.75rem; }
    .account-card { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.25rem; border: 1px solid #e2e8f0; border-radius: 8px; }
    .owner { font-weight: 600; }
    .balance { color: #2d6a4f; font-size: 1.1rem; margin-left: 1rem; }
    .btn-primary { background: #2d6a4f; color: white; padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; }
    .btn-secondary { color: #2d6a4f; border: 1px solid #2d6a4f; padding: 0.4rem 0.9rem; border-radius: 6px; text-decoration: none; }
    .empty { color: #718096; }
  `]
})
export class AccountListComponent implements OnInit {
  private accountService = inject(AccountService);
  readonly accounts = this.accountService.accounts;

  ngOnInit() {
    this.accountService.loadAll().subscribe();
  }
}
