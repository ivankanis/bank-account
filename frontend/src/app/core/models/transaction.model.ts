export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export interface Transaction {
  transactionId: string;
  type: TransactionType;
  amount: number;
  currencyCode: string;
  balanceAfter: number;
  occurredAt: string;
}

export interface TransactionHistory {
  accountId: string;
  transactions: Transaction[];
}

export interface MoneyOperationRequest {
  amount: number;
  currencyCode: string;
}
