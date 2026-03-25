import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DepositWithdrawComponent } from './deposit-withdraw.component';
import { TransactionService } from '../../../core/services/transaction.service';
import { of, throwError } from 'rxjs';
import { Transaction } from '../../../core/models/transaction.model';

describe('DepositWithdrawComponent', () => {
  let fixture: ComponentFixture<DepositWithdrawComponent>;
  let mockTransactionService: jasmine.SpyObj<TransactionService>;

  const mockTransaction: Transaction = {
    transactionId: 'tx-1',
    type: 'DEPOSIT',
    amount: 50,
    currencyCode: 'EUR',
    balanceAfter: 150,
    occurredAt: '2026-01-01T10:00:00Z'
  };

  beforeEach(async () => {
    mockTransactionService = jasmine.createSpyObj('TransactionService', ['deposit', 'withdraw']);

    await TestBed.configureTestingModule({
      imports: [DepositWithdrawComponent],
      providers: [{ provide: TransactionService, useValue: mockTransactionService }]
    }).compileComponents();

    fixture = TestBed.createComponent(DepositWithdrawComponent);
    fixture.componentInstance.accountId = 'acc-1';
    fixture.detectChanges();
  });

  it('deposit and withdraw buttons are disabled when form is invalid', () => {
    const buttons = fixture.nativeElement.querySelectorAll('button');
    buttons.forEach((btn: HTMLButtonElement) => expect(btn.disabled).toBeTrue());
  });

  it('calls deposit service and emits operationDone on success', () => {
    mockTransactionService.deposit.and.returnValue(of(mockTransaction));
    const emitSpy = spyOn(fixture.componentInstance.operationDone, 'emit');

    fixture.componentInstance.form.setValue({ amount: 50, currencyCode: 'EUR' });
    fixture.componentInstance.deposit();

    expect(mockTransactionService.deposit).toHaveBeenCalledWith('acc-1', { amount: 50, currencyCode: 'EUR' });
    expect(emitSpy).toHaveBeenCalled();
  });

  it('calls withdraw service and emits operationDone on success', () => {
    const withdrawTx = { ...mockTransaction, type: 'WITHDRAWAL' as const };
    mockTransactionService.withdraw.and.returnValue(of(withdrawTx));
    const emitSpy = spyOn(fixture.componentInstance.operationDone, 'emit');

    fixture.componentInstance.form.setValue({ amount: 30, currencyCode: 'EUR' });
    fixture.componentInstance.withdraw();

    expect(mockTransactionService.withdraw).toHaveBeenCalledWith('acc-1', { amount: 30, currencyCode: 'EUR' });
    expect(emitSpy).toHaveBeenCalled();
  });

  it('shows error message on insufficient funds', () => {
    mockTransactionService.withdraw.and.returnValue(
      throwError(() => ({ error: { message: 'Fonds insuffisants' } }))
    );

    fixture.componentInstance.form.setValue({ amount: 9999, currencyCode: 'EUR' });
    fixture.componentInstance.withdraw();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage).toBe('Fonds insuffisants');
    expect(fixture.nativeElement.textContent).toContain('Fonds insuffisants');
  });
});
