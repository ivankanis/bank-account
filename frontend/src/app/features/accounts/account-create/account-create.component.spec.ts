import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountCreateComponent } from './account-create.component';
import { AccountService } from '../../../core/services/account.service';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Account } from '../../../core/models/account.model';

describe('AccountCreateComponent', () => {
  let fixture: ComponentFixture<AccountCreateComponent>;
  let mockAccountService: jasmine.SpyObj<AccountService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAccount: Account = {
    accountId: 'id-1',
    ownerName: 'Alice',
    balance: 100,
    currencyCode: 'EUR',
    createdAt: ''
  };

  beforeEach(async () => {
    mockAccountService = jasmine.createSpyObj('AccountService', ['create']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AccountCreateComponent],
      providers: [
        provideRouter([]),
        { provide: AccountService, useValue: mockAccountService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountCreateComponent);
    fixture.detectChanges();
  });

  it('submit button is disabled when form is invalid', () => {
    const btn = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(btn.disabled).toBeTrue();
  });

  it('submits valid form and navigates to /accounts', () => {
    mockAccountService.create.and.returnValue(of(mockAccount));
    const component = fixture.componentInstance;
    component.form.setValue({ ownerName: 'Alice', initialAmount: 100, currencyCode: 'EUR' });
    fixture.detectChanges();

    component.submit();

    expect(mockAccountService.create).toHaveBeenCalledWith({
      ownerName: 'Alice',
      initialAmount: 100,
      currencyCode: 'EUR'
    });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/accounts']);
  });

  it('displays error message on API error', () => {
    mockAccountService.create.and.returnValue(
      throwError(() => ({ error: { message: 'Erreur serveur' } }))
    );
    const component = fixture.componentInstance;
    component.form.setValue({ ownerName: 'Alice', initialAmount: 100, currencyCode: 'EUR' });

    component.submit();
    fixture.detectChanges();

    expect(component.errorMessage).toBe('Erreur serveur');
    expect(fixture.nativeElement.textContent).toContain('Erreur serveur');
  });
});
