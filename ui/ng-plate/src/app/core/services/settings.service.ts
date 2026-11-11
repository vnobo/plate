import { afterNextRender, Service, signal } from '@angular/core';

export interface PlateTheme {
  id: string;
  name: string;
  description: string;
  mode: 'light' | 'dark';
  swatch: string;
}

const AZURE: PlateTheme = {
  id: 'azure',
  name: 'Azure Console',
  description: 'Cool blue, balanced density',
  mode: 'light',
  swatch: '#0a6ed1',
};

/** Palettes derived from the project's example theme tokens. They override only
    Tabler semantic variables, so component markup and utilities stay shared. */
export const PLATE_THEMES: ReadonlyArray<PlateTheme> = [
  AZURE,
  {
    id: 'slate',
    name: 'Slate Night',
    description: 'Deep navy, compact dark UI',
    mode: 'dark',
    swatch: '#4a9be8',
  },
  {
    id: 'ledger',
    name: 'Ledger Brass',
    description: 'Cool grey with brass, crisp edges',
    mode: 'light',
    swatch: '#8a6320',
  },
  {
    id: 'atelier',
    name: 'Atelier Clay',
    description: 'Warm paper, generous spacing',
    mode: 'light',
    swatch: '#ad5b38',
  },
];

const STORAGE_KEY = 'plate-theme';

@Service()
export class SettingsService {
  readonly palette = signal<string>(AZURE.id);

  constructor() {
    // localStorage is browser-only; afterNextRender keeps this off the SSR pass.
    afterNextRender(() => this.setPalette(localStorage.getItem(STORAGE_KEY) ?? AZURE.id));
  }

  /** The existing light/dark toggle maps each mode to its default palette. */
  setTheme(mode: 'light' | 'dark'): void {
    this.setPalette(mode === 'dark' ? 'slate' : AZURE.id);
  }

  setPalette(id: string): void {
    const theme = PLATE_THEMES.find((item) => item.id === id) ?? AZURE;
    const root = document.documentElement;
    root.setAttribute('data-plate-theme', theme.id);
    root.setAttribute('data-bs-theme', theme.mode);
    localStorage.setItem(STORAGE_KEY, theme.id);
    // Prevent tabler-theme.js from restoring the system preference on the next load.
    localStorage.setItem('tabler-theme', theme.mode);
    this.palette.set(theme.id);
  }
}
