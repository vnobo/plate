export interface UserAuditor {
  code: string;
  username: string | null;
  name: string | null;
}

export interface Search {
  search?: string | null;
  query?: Map<string, any>;
}

export interface Page<T> {
  content: T[];
  pageable: Pageable;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface Pageable {
  page: number;
  size: number;
  sorts: string[];
}

export const defaultPageable: Pageable = {
  page: 0,
  size: 20,
  sorts: ['id,desc'],
};

export interface Authentication {
  token: string;
  expires: number;
  lastAccessTime: number;
  details: any;
}

export interface Credentials {
  password: string | null | undefined;
  username: string | null | undefined;
}
