import { Search, UserAuditor } from '@plate/types';

/** 后端 MenuType；FOLDER 只是分组标题，其余才是可点的条目 */
export type MenuType = 'FOLDER' | 'MENU' | 'LINK' | 'API';

/** Menu（对应后端 se_menus，树形结构靠 pcode 串起来） */
export interface MenuItem extends Search {
  id?: number;
  code?: string;
  /** 父菜单 code，顶级菜单用 ROOT_PCODE */
  pcode?: string;
  /** 后端会自动转大写并补 ROLE_ 前缀 */
  authority?: string;
  type?: MenuType;
  name?: string;
  /** /platform 下的模块路径；FOLDER 没有落地页 */
  path?: string;
  sortNo?: number;
  /** 后端从 extend.icons 展平出来，侧栏按名字映射成内联 SVG */
  icons?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: UserAuditor;
  updatedBy?: UserAuditor;
  version?: number;
}

/** 顶级菜单的父 code，对应 se_menus.pcode 的默认值 */
export const ROOT_PCODE = '00000000-0000-0000-0000-000000000000';

export const MENU_TYPES: readonly MenuType[] = ['FOLDER', 'MENU', 'LINK', 'API'];
