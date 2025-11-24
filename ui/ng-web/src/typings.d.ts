declare module '@plate/types' {
  export interface UserAuditor {
    code: string;
    username: string | null;
    name: string | null;
  }

  export interface Search {
    search?: string | null;
    query?: Map<string, unknown>;
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
    details: UserDetails;
  }

  export interface UserDetails {
    authorities: Authority[];
    attributes: UserAttributes;
    accountNonExpired: boolean;
    accountNonLocked: boolean;
    avatar: string | null;
    bio: string | null;
    code: string;
    credentialsNonExpired: boolean;
    enabled: boolean;
    name: string;
    nickname: string;
    tenantCode: string;
    tenantName: string;
  }

  export interface Authority {
    authority: string;
  }

  export interface UserAttributes {
    username: string;
    userCode: string;
  }

  export interface Credentials {
    password: string | null | undefined;
    username: string | null | undefined;
  }
}
declare var tabler: any;
declare var bootstrap: any;
