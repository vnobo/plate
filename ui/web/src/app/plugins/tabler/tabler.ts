import { afterNextRender, AfterViewInit, Directive, ElementRef, inject } from '@angular/core';
import { fromEvent } from 'rxjs';

// Define types for better TypeScript support
interface TablerCollapseInstance {
  show: () => void;
  hide: () => void;
  toggle: () => void;
  dispose: () => void;
  // Tabler's Collapse doesn't have isShown method, we'll track state differently
}

@Directive({
  selector: '[tablerInit]',
})
export class TablerInit implements AfterViewInit {
  private readonly el = inject(ElementRef);

  constructor() {
    afterNextRender(async () => {
      const tabler = await import('@tabler/core');
      const ele = this.el.nativeElement;
      const collapseElementList = ele.querySelectorAll('[data-bs-toggle="collapse"]');
      [...collapseElementList].map(collapseTriggerEl => {
        const collapseElId = collapseTriggerEl.getAttribute('data-bs-target');
        const cel = tabler.Collapse.getOrCreateInstance(collapseElId);
        fromEvent(ele.querySelector(collapseElId), 'hide.bs.collapse').subscribe(() =>
          console.debug('collapse hide event!'),
        );
      });
    });
  }

  async ngAfterViewInit(): Promise<void> {
    const tabler = await import('@tabler/core');
    const ele = this.el.nativeElement;
    const dropdownElementList = ele.querySelectorAll('[data-bs-toggle="dropdown"]');
    [...dropdownElementList].map(dropdownTriggerEl => {
      fromEvent(dropdownTriggerEl, 'show.bs.dropdown').subscribe(() =>
        console.debug(`${dropdownTriggerEl}dropdown show event!`),
      );
      fromEvent(dropdownTriggerEl, 'shown.bs.dropdown').subscribe(() =>
        console.debug('dropdown shown event!'),
      );
      return tabler.Dropdown.getOrCreateInstance(dropdownTriggerEl);
    });
    const tooltipList = ele.querySelectorAll('[data-bs-toggle="tooltip"]');
    [...tooltipList].map(tooltipEl => {
      return new tabler.Tooltip(tooltipEl);
    });
  }
}
