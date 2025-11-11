export interface Tenant {
  id: string;
  name: string;
  description?: string;
  status: 'active' | 'inactive' | 'suspended';
  createdAt: Date;
  updatedAt: Date;
  subscriptionType?: string;
  expirationDate?: Date;
}
