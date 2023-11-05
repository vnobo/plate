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
    extend?: any;
    creator?: UserAuditor;
    updater?: UserAuditor;
    createdTime?: Date;
    updatedTime?: Date;
    getPermissions?: Permission[];
    getIcons?: string;
    children?: Menu[]
}

export interface UserAuditor {
    code: string;
    username: string;
    name?: string;
}

export enum MenuType {
    FOLDER = "FOLDER",
    MENU = "MENU",
}

export enum HttpMethod {
    GET = "GET",
    POST = "POST",
    PUT = "PUT",
    DELETE = "DELETE",
    ALL = "ALL",
}

export interface Permission {
    method: HttpMethod;
    name: string;
    path: string;
    authority: string;
}
