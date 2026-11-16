import {afterNextRender, Service, signal} from '@angular/core';

export interface PlateTheme {
  id: string;
  name: string;
  description: string;
  swatch: string;
}

/** Spring palette. The id is the persisted localStorage key, so it keeps the
    historical 'azure' value even though the palette now carries the spring season. */
const SPRING: PlateTheme = {
  id: 'azure',
  name: 'Verdant Spring',
  description: 'Spring · fresh green on mint paper',
  swatch: '#0c7a52',
};

/** One palette per season, listed in calendar order so the menu reads spring → winter.
    Ids stay stable because they are storage keys. Each season has both a light and a
    dark form in styles.scss, selected by html[data-bs-theme], independently of the id. */
export const PLATE_THEMES: ReadonlyArray<PlateTheme> = [
  SPRING,
  {
    id: 'ledger',
    name: 'Amber Summer',
    description: 'Summer · sunlit gold on ivory',
    swatch: '#8f6300',
  },
  {
    id: 'atelier',
    name: 'Maple Autumn',
    description: 'Autumn · persimmon on parchment',
    swatch: '#a63b23',
  },
  {
    id: 'slate',
    name: 'Frost Winter',
    description: 'Winter · frost blue on ice',
    swatch: '#7cc4e8',
  },
];

const STORAGE_KEY = 'plate-theme';
const MODE_KEY = 'tabler-theme';

@Service()
export class SettingsService {
  readonly palette = signal<string>(SPRING.id);

  constructor() {
    // localStorage is browser-only; afterNextRender keeps this off the SSR pass.
    // Season and light/dark mode are two independent axes: a saved "winter + light"
    // combination must survive a reload just as "winter + dark" does.
    afterNextRender(() => {
      this.setPalette(localStorage.getItem(STORAGE_KEY) ?? SPRING.id);
      this.setTheme(localStorage.getItem(MODE_KEY) === 'dark' ? 'dark' : 'light');
    });
  }

  /** Toggles only the light/dark form; the chosen season stays put. */
  setTheme(mode: 'light' | 'dark'): void {
    document.documentElement.setAttribute('data-bs-theme', mode);
    // Prevent tabler-theme.js from restoring the system preference on the next load.
    localStorage.setItem(MODE_KEY, mode);
  }

  /** Selects only the season; the current light/dark form stays put. */
  setPalette(id: string): void {
    const theme = PLATE_THEMES.find((item) => item.id === id) ?? SPRING;
    document.documentElement.setAttribute('data-plate-theme', theme.id);
    localStorage.setItem(STORAGE_KEY, theme.id);
    this.palette.set(theme.id);
  }
}
