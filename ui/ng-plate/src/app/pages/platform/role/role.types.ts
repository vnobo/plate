import { Search, UserAuditor } from '@plate/types';

/** Role (maps to the backend Group entity; tree structure linked via pcode) */
export interface Group extends Search {
  id?: number;
  code?: string;
  /** Parent role code; top-level roles use ROOT_PCODE */
  pcode?: string;
  name?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: UserAuditor;
  updatedBy?: UserAuditor;
  version?: number;
}

/** Role authority (maps to GroupAuthority; authority is the permission identifier string) */
export interface GroupAuthority extends Search {
  id?: number;
  code?: string;
  groupCode?: string;
  authority?: string;
}

/** Role member (maps to a user; GroupMemberRes additionally carries name) */
export interface GroupMember extends Search {
  id?: number;
  code?: string;
  groupCode?: string;
  userCode?: string;
  name?: string;
}

/** Parent code for top-level roles; matches the se_groups.pcode default value */
export const ROOT_PCODE = '00000000-0000-0000-0000-000000000000';
