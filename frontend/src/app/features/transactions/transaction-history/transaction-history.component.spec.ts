import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionHistoryComponent } from './transaction-history.component';
import { TransactionService } from '../../../core/services/transaction.service';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { Transaction, TransactionHistory } from '../../../core/models/transaction.model';

describe('TransactionHistoryComponent', () => {
  let fixture: ComponentFixture<TransactionHistoryComponent>;
  let mockTransactionService: jasmine.SpyObj<TransactionService>;

  const mockTransactions: Transaction[] = [
    { transactionId: 'tx-1', type: 'DEPOSIT', amount: 100, currencyCode: 'EUR', balanceAfter: 100, occurredAt: '2026-01-01T10:00:00Z' },
    { transactionId: 'tx-2', type: 'WITHDRAWAL', amount: 30, currencyCode: 'EUR', balanceAfter: 70, occurredAt: '2026-01-02T10:00:00Z' }
  ];

  const mockHistory: TransactionHistory = { accountId: 'acc-1', transactions: mockTransactions };

  function createFixture(transactions: Transaction[]) {
    const history: TransactionHistory = { accountId: 'acc-1', transactions };
    mockTransactionService.getHistory.and.returnValue(of(history));
  }

  beforeEach(async () => {
    mockTransactionService = jasmine.createSpyObj('TransactionService', ['getHistory', 'deposit', 'withdraw']);
    mockTransactionService.getHistory.and.returnValue(of(mockHistory));

    await TestBed.configureTestingModule({
      imports: [TransactionHistoryComponent],
      providers: [
        provideRouter([]),
        { provide: TransactionService, useValue: mockTransactionService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'acc-1' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionHistoryComponent);
    fixture.detectChanges();
  });

  it('displays transaction rows', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
  });

  it('shows empty state when no transactions', () => {
    createFixture([]);
    fixture.componentInstance.reload();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Aucune transaction');
    expect(fixture.nativeElement.querySelector('table')).toBeNull();
  });

  it('reloads transactions on operationDone event', () => {
    const newTx: Transaction = { transactionId: 'tx-3', type: 'DEPOSIT', amount: 50, currencyCode: 'EUR', balanceAfter: 120, occurredAt: '2026-01-03T10:00:00Z' };
    createFixture([...mockTransactions, newTx]);

    fixture.componentInstance.reload();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(3);
  });
});
