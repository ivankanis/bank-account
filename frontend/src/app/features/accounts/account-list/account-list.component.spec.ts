import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountListComponent } from './account-list.component';
import { AccountService } from '../../../core/services/account.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Account } from '../../../core/models/account.model';
import { signal } from '@angular/core';

describe('AccountListComponent', () => {
  let fixture: ComponentFixture<AccountListComponent>;
  let mockAccountService: jasmine.SpyObj<AccountService>;

  const mockAccounts: Account[] = [
    { accountId: 'id-1', ownerName: 'Alice', balance: 100, currencyCode: 'EUR', createdAt: '' },
    { accountId: 'id-2', ownerName: 'Bob', balance: 200, currencyCode: 'EUR', createdAt: '' }
  ];

  beforeEach(async () => {
    const accountsSignal = signal<Account[]>([]);
    mockAccountService = jasmine.createSpyObj('AccountService', ['loadAll'], {
      accounts: accountsSignal
    });
    mockAccountService.loadAll.and.callFake(() => {
      accountsSignal.set(mockAccounts);
      return of(mockAccounts);
    });

    await TestBed.configureTestingModule({
      imports: [AccountListComponent],
      providers: [
        provideRouter([]),
        { provide: AccountService, useValue: mockAccountService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountListComponent);
    fixture.detectChanges();
  });

  it('displays all accounts', () => {
    const items = fixture.nativeElement.querySelectorAll('.account-card');
    expect(items.length).toBe(2);
  });

  it('shows owner names', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Alice');
    expect(text).toContain('Bob');
  });

  it('shows empty state when no accounts', () => {
    mockAccountService.loadAll.and.callFake(() => {
      (mockAccountService.accounts as any).set([]);
      return of([]);
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('account-card');
  });
});
