import { Search, UserAuditor } from '@plate/types';

/** 角色（对应后端 Group 实体，树形结构通过 pcode 关联） */
export interface Group extends Search {
  id?: number;
  code?: string;
  /** 父级角色编码，顶级角色为 ROOT_PCODE */
  pcode?: string;
  name?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: UserAuditor;
  updatedBy?: UserAuditor;
  version?: number;
}

/** 角色权限（对应 GroupAuthority，authority 为权限标识字符串） */
export interface GroupAuthority extends Search {
  id?: number;
  code?: string;
  groupCode?: string;
  authority?: string;
}

/** 角色成员（对应用户，GroupMemberRes 额外带 name） */
export interface GroupMember extends Search {
  id?: number;
  code?: string;
  groupCode?: string;
  userCode?: string;
  name?: string;
}

/** 顶级角色的父级编码，对应数据库 se_groups.pcode 默认值 */
export const ROOT_PCODE = '00000000-0000-0000-0000-000000000000';
