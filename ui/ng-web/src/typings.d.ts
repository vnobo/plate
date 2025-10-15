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

// Type definitions for @tabler/core
// These definitions provide type safety when using Tabler components

declare module '@tabler/core' {
  // Import Bootstrap types (since Tabler extends Bootstrap)
  import * as BootstrapNS from '@tabler/core';

  // Re-export all Bootstrap types and components
  export namespace Bootstrap {
    export type Alert = BootstrapNS.Alert;
    export type AlertOptions = BootstrapNS.AlertOptions;
    export type Button = BootstrapNS.Button;
    export type ButtonOptions = BootstrapNS.ButtonOptions;
    export type Carousel = BootstrapNS.Carousel;
    export type CarouselOptions = BootstrapNS.CarouselOptions;
    export type Collapse = BootstrapNS.Collapse;
    export type CollapseOptions = BootstrapNS.CollapseOptions;
    export type Dropdown = BootstrapNS.Dropdown;
    export type DropdownOptions = BootstrapNS.DropdownOptions;
    export type Modal = BootstrapNS.Modal;
    export type ModalOptions = BootstrapNS.ModalOptions;
    export type Offcanvas = BootstrapNS.Offcanvas;
    export type OffcanvasOptions = BootstrapNS.OffcanvasOptions;
    export type Popover = BootstrapNS.Popover;
    export type PopoverOptions = BootstrapNS.PopoverOptions;
    export type ScrollSpy = BootstrapNS.ScrollSpy;
    export type ScrollSpyOptions = BootstrapNS.ScrollSpyOptions;
    export type Tab = BootstrapNS.Tab;
    export type TabOptions = BootstrapNS.TabOptions;
    export type Toast = BootstrapNS.Toast;
    export type ToastOptions = BootstrapNS.ToastOptions;
    export type Tooltip = BootstrapNS.Tooltip;
    export type TooltipOptions = BootstrapNS.TooltipOptions;
  }

  // Export Bootstrap components individually for direct usage
  export { Alert } from 'bootstrap';
  export { Button } from 'bootstrap';
  export { Carousel } from 'bootstrap';
  export { Collapse } from 'bootstrap';
  export { Dropdown } from 'bootstrap';
  export { Modal } from 'bootstrap';
  export { Offcanvas } from 'bootstrap';
  export { Popover } from 'bootstrap';
  export { ScrollSpy } from 'bootstrap';
  export { Tab } from 'bootstrap';
  export { Toast } from 'bootstrap';
  export { Tooltip } from 'bootstrap';

  // Tabler-specific exports (if any exist)
  // Note: Tabler may provide additional functionality beyond Bootstrap
  export const tabler: any; // Placeholder for Tabler-specific exports

  // Additional utility types that might be useful
  export interface TablerConfig {
    theme?: 'light' | 'dark';
    components?: string[];
  }
}
