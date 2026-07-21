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

/** 顶级（根）租户的父级编码，对应数据库 se_tenants.pcode 列的默认值 */
export const ROOT_PCODE = '00000000-0000-0000-0000-000000000000';
