import { Search, UserAuditor } from '@plate/types';

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
  extend?: Record<string, unknown>;
  loginTime?: Date;
  createdBy?: UserAuditor;
  updatedBy?: UserAuditor;
  updatedAt?: Date;
  createdAt?: Date;
}

/** Direct authority assigned to a user (maps to the se_authorities table). */
export interface UserAuthority extends Search {
  id?: number;
  code?: string;
  userCode?: string;
  authority?: string;
}
