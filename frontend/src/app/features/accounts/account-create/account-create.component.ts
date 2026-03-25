import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../../core/services/account.service';

@Component({
  selector: 'app-account-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <h1>Créer un compte</h1>

      <form [formGroup]="form" (ngSubmit)="submit()" class="form">
        <div class="field">
          <label for="ownerName">Titulaire</label>
          <input id="ownerName" formControlName="ownerName" type="text" placeholder="Nom complet" />
          @if (form.get('ownerName')?.invalid && form.get('ownerName')?.touched) {
            <span class="error">Le nom est obligatoire.</span>
          }
        </div>

        <div class="field">
          <label for="initialAmount">Solde initial</label>
          <input id="initialAmount" formControlName="initialAmount" type="number" min="0" step="0.01" />
          @if (form.get('initialAmount')?.invalid && form.get('initialAmount')?.touched) {
            <span class="error">Le montant doit être positif ou nul.</span>
          }
        </div>

        <div class="field">
          <label for="currencyCode">Devise</label>
          <select id="currencyCode" formControlName="currencyCode">
            <option value="EUR">EUR</option>
            <option value="USD">USD</option>
            <option value="GBP">GBP</option>
          </select>
        </div>

        @if (errorMessage) {
          <p class="error-banner">{{ errorMessage }}</p>
        }

        <div class="actions">
          <a routerLink="/accounts" class="btn-secondary">Annuler</a>
          <button type="submit" [disabled]="form.invalid" class="btn-primary">Créer</button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .page { max-width: 480px; margin: 2rem auto; padding: 0 1rem; }
    .form { display: flex; flex-direction: column; gap: 1.25rem; margin-top: 1.5rem; }
    .field { display: flex; flex-direction: column; gap: 0.4rem; }
    label { font-weight: 500; }
    input, select { padding: 0.5rem 0.75rem; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 1rem; }
    .error { color: #e53e3e; font-size: 0.875rem; }
    .error-banner { background: #fff5f5; color: #c53030; padding: 0.75rem; border-radius: 6px; border: 1px solid #fed7d7; }
    .actions { display: flex; gap: 1rem; justify-content: flex-end; }
    .btn-primary { background: #2d6a4f; color: white; padding: 0.5rem 1.25rem; border: none; border-radius: 6px; cursor: pointer; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-secondary { color: #2d6a4f; border: 1px solid #2d6a4f; padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; }
  `]
})
export class AccountCreateComponent {
  private fb = inject(FormBuilder);
  private accountService = inject(AccountService);
  private router = inject(Router);

  errorMessage = '';

  form = this.fb.group({
    ownerName: ['', [Validators.required, Validators.minLength(1)]],
    initialAmount: [0, [Validators.required, Validators.min(0)]],
    currencyCode: ['EUR', Validators.required]
  });

  submit() {
    if (this.form.invalid) return;
    this.errorMessage = '';
    const { ownerName, initialAmount, currencyCode } = this.form.value;
    this.accountService.create({
      ownerName: ownerName!,
      initialAmount: initialAmount!,
      currencyCode: currencyCode!
    }).subscribe({
      next: () => this.router.navigate(['/accounts']),
      error: err => this.errorMessage = err.error?.message ?? 'Erreur lors de la création.'
    });
  }
}
