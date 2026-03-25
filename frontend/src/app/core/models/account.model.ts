export interface Account {
  accountId: string;
  ownerName: string;
  balance: number;
  currencyCode: string;
  createdAt: string;
}

export interface CreateAccountRequest {
  ownerName: string;
  initialAmount: number;
  currencyCode: string;
}
