import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'accounts', pathMatch: 'full' },
  {
    path: 'accounts',
    loadComponent: () => import('./features/accounts/account-list/account-list.component')
      .then(m => m.AccountListComponent)
  },
  {
    path: 'accounts/new',
    loadComponent: () => import('./features/accounts/account-create/account-create.component')
      .then(m => m.AccountCreateComponent)
  },
  {
    path: 'accounts/:id/history',
    loadComponent: () => import('./features/transactions/transaction-history/transaction-history.component')
      .then(m => m.TransactionHistoryComponent)
  }
];
