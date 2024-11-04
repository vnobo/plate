import { UserAuditor } from 'plate-types';

export interface Group {
  id?: number;
  code?: string;
  tenantCode: string;
  name?: string;
  extend?: any;
  creator?: UserAuditor;
  updater?: UserAuditor;
  createdTime?: Date;
  updatedTime?: Date;
}
