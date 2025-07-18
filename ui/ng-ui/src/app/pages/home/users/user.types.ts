import {Search, UserAuditor} from '@app/core/types';

export interface User extends Search {
  id?: number;
  code?: string;
  tenantCode?: string;
  username?: string;
  password?: string;
  disabled?: boolean;
  accountExpired?: boolean;
  accountLocked?: boolean;
  credentialsExpired?: boolean;
  email?: string;
  phone?: string;
  name?: string;
  avatar?: string;
  bio?: string;
  extend?: any;
  loginTime?: Date;
  creator?: UserAuditor;
  updater?: UserAuditor;
  updatedTime?: Date;
  createdTime?: Date;
}
