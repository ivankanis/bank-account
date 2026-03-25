import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccountService } from './account.service';
import { Account } from '../models/account.model';

describe('AccountService', () => {
  let service: AccountService;
  let http: HttpTestingController;

  const mockAccount: Account = {
    accountId: '123e4567-e89b-12d3-a456-426614174000',
    ownerName: 'Alice',
    balance: 100.00,
    currencyCode: 'EUR',
    createdAt: '2026-01-01T10:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AccountService]
    });
    service = TestBed.inject(AccountService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loadAll updates signal with fetched accounts', () => {
    service.loadAll().subscribe();
    const req = http.expectOne('/api/v1/accounts');
    req.flush([mockAccount]);
    expect(service.accounts()).toEqual([mockAccount]);
  });

  it('create posts to accounts endpoint', () => {
    let result: Account | undefined;
    service.create({ ownerName: 'Alice', initialAmount: 100, currencyCode: 'EUR' })
      .subscribe(a => result = a);
    const req = http.expectOne('/api/v1/accounts');
    expect(req.request.method).toBe('POST');
    req.flush(mockAccount);
    expect(result).toEqual(mockAccount);
  });

  it('getById calls correct endpoint', () => {
    service.getById(mockAccount.accountId).subscribe();
    http.expectOne(`/api/v1/accounts/${mockAccount.accountId}`);
  });
});
