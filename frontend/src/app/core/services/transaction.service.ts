import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MoneyOperationRequest, Transaction, TransactionHistory } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly apiUrl = '/api/v1/accounts';

  constructor(private http: HttpClient) {}

  deposit(accountId: string, request: MoneyOperationRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/${accountId}/deposits`, request);
  }

  withdraw(accountId: string, request: MoneyOperationRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/${accountId}/withdrawals`, request);
  }

  getHistory(accountId: string): Observable<TransactionHistory> {
    return this.http.get<TransactionHistory>(`${this.apiUrl}/${accountId}/transactions`);
  }
}
