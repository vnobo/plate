
export interface Menu {
  id?: number;
  code?: string;
  pcode?: string;
  tenantCode?: string;
  type?: MenuType;
  authority?: string;
  name?: string;
  path?: string;
  sort?: number;
  extend?: never;
  creator?: UserAuditor;
  updater?: UserAuditor;
  createdTime?: Date;
  updatedTime?: Date;
  permissions?: Permission[];
  icons?: string;
  children?: Menu[];
  level?: number;
  expand?: boolean;
  parent?: Menu;
}

export interface UserAuditor {
  code: string;
  username: string;
  name?: string;
}

export enum MenuType {
  FOLDER = 'FOLDER',
  MENU = 'MENU',
  LINK = 'LINK',
  API = 'API',
}

export enum HttpMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  ALL = 'ALL',
}

export interface Permission {
  method: HttpMethod;
  name: string;
  path: string;
  authority: string;
}
