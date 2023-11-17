export class Pageable {
  page: number = 0;
  totalElements: number;
  size: number = 25;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  sort: any;
  content: any;
  empty: boolean;
}
