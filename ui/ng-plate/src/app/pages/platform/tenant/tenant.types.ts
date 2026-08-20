import { Search, UserAuditor } from '@plate/types';

export interface Tenant extends Search {
  id?: number;
  code?: string;
  pcode?: string;
  tenantCode?: string;
  name?: string;
  description?: string;
  createdBy?: UserAuditor;
  updatedBy?: UserAuditor;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
  extend?: Record<string, unknown>;
}

/** Parent code for top-level (root) tenants; matches the se_tenants.pcode column default */
export const ROOT_PCODE = '00000000-0000-0000-0000-000000000000';

/** Membership of a user in a tenant (maps to the se_tenant_members table). */
export interface TenantMember extends Search {
  id?: number;
  code?: string;
  tenantCode?: string;
  userCode?: string;
  enabled?: boolean;
  createdAt?: Date;
}
