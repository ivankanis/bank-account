import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Account, CreateAccountRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly apiUrl = '/api/v1/accounts';

  readonly accounts = signal<Account[]>([]);

  constructor(private http: HttpClient) {}

  loadAll(): Observable<Account[]> {
    return this.http.get<Account[]>(this.apiUrl).pipe(
      tap(accounts => this.accounts.set(accounts))
    );
  }

  getById(accountId: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/${accountId}`);
  }

  create(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.apiUrl, request).pipe(
      tap(account => this.accounts.update(list => [...list, account]))
    );
  }
}
