import {isPlatformBrowser} from '@angular/common';
import {afterNextRender, inject, PLATFORM_ID, Service, signal} from '@angular/core';

export interface PlateTheme {
  id: string;
  name: string;
  description: string;
  swatch: string;
}

/** One palette per season, in calendar order. The id is the persisted value of
    localStorage['theme'] and the value of <html data-season>, so it must match
    the selectors in styles.scss exactly. */
const SPRING: PlateTheme = {
  id: 'spring',
  name: 'Verdant Spring',
  description: 'Spring · new growth in morning mist',
  swatch: '#86c57d',
};

export const PLATE_THEMES: ReadonlyArray<PlateTheme> = [
  SPRING,
  {
    id: 'summer',
    name: 'Amber Summer',
    description: 'Summer · open sky and sea breeze',
    swatch: '#38a3d4',
  },
  {
    id: 'autumn',
    name: 'Maple Autumn',
    description: 'Autumn · harvest at dusk',
    swatch: '#c4703a',
  },
  {
    id: 'winter',
    name: 'Frost Winter',
    description: 'Winter · falling snow, quiet peaks',
    swatch: '#b8d2e5',
  },
];

const STORAGE_KEY = 'theme';

/** Storage can throw in private mode or when cookies are blocked. The inline
    bootstrap script guards the same access; the service must not break the
    theme dropdown when a user clicks it under the same conditions. */
function readStoredSeason(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function writeStoredSeason(id: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, id);
  } catch {
    // Keep the DOM attribute and the signal authoritative for this session.
  }
}

@Service()
export class SettingsService {
  /** The active season id (spring / summer / autumn / winter). */
  readonly palette = signal<string>(SPRING.id);

  constructor() {
    // index.html has already applied the stored season before first paint.
    // Seed the signal from that attribute on the browser so the first Angular
    // render (login art, switcher label) already matches the painted skin;
    // reading it here, instead of in afterNextRender, also keeps SSR safe.
    if (isPlatformBrowser(inject(PLATFORM_ID))) {
      const applied = document.documentElement.getAttribute('data-season');
      if (PLATE_THEMES.some((item) => item.id === applied)) {
        this.palette.set(applied!);
      }
    }

    // Still reconcile from storage: covers a blocked inline script (e.g. CSP).
    afterNextRender(() => {
      const saved = readStoredSeason();
      const season = PLATE_THEMES.some((item) => item.id === saved) ? saved! : SPRING.id;
      this.setPalette(season);
    });
  }

  /** Selects the season: updates the data-season attribute, storage and signal.
      There is no separate light/dark axis — each season is a complete skin. */
  setPalette(id: string): void {
    const theme = PLATE_THEMES.find((item) => item.id === id) ?? SPRING;
    document.documentElement.setAttribute('data-season', theme.id);
    writeStoredSeason(theme.id);
    this.palette.set(theme.id);
  }
}
