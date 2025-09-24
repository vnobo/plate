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
    details: Record<string, unknown>;
  }

  export interface Credentials {
    password: string | null | undefined;
    username: string | null | undefined;
  }
}

declare module '@tabler/core' {
  export { bootstrap as Bootstrap } from '@tabler/core/js/tabler';

  // Bootstrap 组件选项接口
  interface ToastOptions {
    animation?: boolean;
    autohide?: boolean;
    delay?: number;
  }

  interface ModalOptions {
    backdrop?: boolean | 'static';
    keyboard?: boolean;
    focus?: boolean;
  }

  interface TooltipOptions {
    placement?: 'top' | 'right' | 'bottom' | 'left';
    trigger?: 'click' | 'hover' | 'focus' | 'manual';
    delay?: number | { show: number; hide: number };
  }

  interface PopoverOptions extends TooltipOptions {
    content?: string;
    html?: boolean;
  }

  interface CarouselOptions {
    interval?: number;
    keyboard?: boolean;
    pause?: 'hover' | false;
    ride?: boolean;
    wrap?: boolean;
  }

  interface CollapseOptions {
    toggle?: boolean;
    parent?: string | Element;
  }

  interface OffcanvasOptions {
    backdrop?: boolean | 'static';
    keyboard?: boolean;
    scroll?: boolean;
  }

  // Bootstrap 组件类
  export class Toast {
    constructor(element: Element, options?: ToastOptions);
    static getInstance(element: Element): Toast | null;
    static getOrCreateInstance(element: Element, config?: ToastOptions): Toast;
    show(): void;
    hide(): void;
    dispose(): void;
  }

  export class Modal {
    constructor(element: Element | string, options?: ModalOptions);
    static getInstance(element: Element): Modal | null;
    static getOrCreateInstance(element: Element, config?: ModalOptions): Modal;
    show(): void;
    hide(): void;
    toggle(): void;
    handleUpdate(): void;
    dispose(): void;
  }

  export class Tooltip {
    constructor(element: Element, options?: TooltipOptions);
    static getInstance(element: Element): Tooltip | null;
    static getOrCreateInstance(element: Element, config?: TooltipOptions): Tooltip;
    show(): void;
    hide(): void;
    toggle(): void;
    dispose(): void;
    enable(): void;
    disable(): void;
    toggleEnabled(): void;
  }

  export class Alert {
    constructor(element: Element);
    static getInstance(element: Element): Alert | null;
    static getOrCreateInstance(element: Element): Alert;
    close(): void;
    dispose(): void;
  }

  export class Tab {
    constructor(element: Element);
    static getInstance(element: Element): Tab | null;
    static getOrCreateInstance(element: Element): Tab;
    show(): void;
    dispose(): void;
  }

  export class Button {
    constructor(element: Element);
    static getInstance(element: Element): Button | null;
    static getOrCreateInstance(element: Element): Button;
    toggle(): void;
    dispose(): void;
  }

  export class Carousel {
    constructor(element: Element, options?: CarouselOptions);
    static getInstance(element: Element): Carousel | null;
    static getOrCreateInstance(element: Element, config?: CarouselOptions): Carousel;
    cycle(): void;
    pause(): void;
    prev(): void;
    next(): void;
    dispose(): void;
    to(index: number): void;
  }

  export class Collapse {
    constructor(element: Element, options?: CollapseOptions);
    static getInstance(element: Element): Collapse | null;
    static getOrCreateInstance(element: Element, config?: CollapseOptions): Collapse;
    toggle(): void;
    show(): void;
    hide(): void;
    dispose(): void;
  }

  export class Dropdown {
    constructor(element: Element);
    static getInstance(element: Element): Dropdown | null;
    static getOrCreateInstance(element: Element): Dropdown;
    toggle(): void;
    show(): void;
    hide(): void;
    dispose(): void;
  }

  export class Popover {
    constructor(element: Element, options?: PopoverOptions);
    static getInstance(element: Element): Popover | null;
    static getOrCreateInstance(element: Element, config?: PopoverOptions): Popover;
    show(): void;
    hide(): void;
    toggle(): void;
    dispose(): void;
    enable(): void;
    disable(): void;
    toggleEnabled(): void;
  }

  export class ScrollSpy {
    constructor(element: Element, options?: { target?: string | Element });
    static getInstance(element: Element): ScrollSpy | null;
    static getOrCreateInstance(element: Element): ScrollSpy;
    refresh(): void;
    dispose(): void;
  }

  export class Offcanvas {
    constructor(element: Element, options?: OffcanvasOptions);
    static getInstance(element: Element): Offcanvas | null;
    static getOrCreateInstance(element: Element, config?: OffcanvasOptions): Offcanvas;
    toggle(relatedTarget?: Element): void;
    show(relatedTarget?: Element): void;
    hide(): void;
    dispose(): void;
  }
}
