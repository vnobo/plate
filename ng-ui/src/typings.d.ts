//bootstrap js any
declare var bootstrap: any;

declare type PageableWithDefaults = {
  [K in keyof Pageable]?: Pageable[K] extends Array<infer U> ? U[] : Pageable[K] extends object ? object : Pageable[K];
};
