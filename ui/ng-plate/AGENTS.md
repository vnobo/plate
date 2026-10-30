# Angular AGENT

You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## Project At A Glance

Verified against the current `package.json`, `angular.json`, `tsconfig*.json` and `src/` — prefer these over generic Angular habits:

- Angular 22.2 + TypeScript 6.0, managed with `pnpm@12.16.0`.
- **Zoneless**: no `zone.js` in dependencies or polyfills. State must go through signals — do not rely on patched async callbacks or `NgZone` to trigger change detection.
- **Services are declared with `@Service()`** (Angular 22 API), never `@Injectable({ providedIn: 'root' })`. There are zero `@Injectable` usages in `src/`.
- Path aliases are declared in `tsconfig.app.json`, not the root `tsconfig.json`: `@app/*` → `src/app/*`, `@envs/*` → `src/envs/*`, `@styles/*` → `src/styles/*`.
- `@plate/types` is **not an npm package** — it is a `declare module` block in `src/typings.d.ts` holding `Authentication`, `UserDetails`, `Page<T>`, `Pageable`, `Search` and audit types. Edit that file, not `package.json`.
- `tabler` is a **browser global** from `@tabler/core`, wired through `angular.json` `scripts`/`styles` and typed as `declare var tabler: any` in `src/typings.d.ts`. It does not exist on the server.
- i18n source locale and `LOCALE_ID` are `zh-Hans`; all user-facing copy is Chinese.
- SSR (`outputMode: server`) and the service worker are configured, but `src/app/app.routes.server.ts` maps **every** route to `RenderMode.Client`.
- Tests run on **Vitest + jsdom** through `@angular/build:unit-test`; no browser unless `--browsers` is passed.

## Project Workflow

- Use `pnpm@12.16.0`; keep `package.json` and `pnpm-lock.yaml` synchronized when dependencies change.
- The project uses Angular 22, standalone components, signals, `computed()`, `linkedSignal()`, `inject()`, and native `@if`/`@for` control flow.
- Organize pages by business domain under `src/app/pages`. Keep top-level routes in `src/app/app.routes.ts` and domain routes near their feature pages; prefer lazy loading.
- Put cross-cutting services and storage adapters in `src/app/core`, layouts in `src/app/layout`, reusable UI plugins in `src/app/plugins`, and shared primitives in `src/app/shared`.
- Reuse the existing HTTP interceptor and environment configuration for API requests. Do not duplicate the API host, timeout, or version headers in individual services.
- Reuse `ModalsService` for dynamic forms and preserve the existing output-based refresh flow when changing modal inputs or results.
- Signal Forms are the project's form pattern — every feature form imports `form`, `submit`, the validators, and the `FormField` directive from `@angular/forms/signals`. Build a `signal` model, derive `form(model, (p) => { required(p.x) })`, bind with `[formField]`, and submit through `await submit(...)`. Never introduce template-driven or `FormGroup`-based reactive forms.

## Code Update And Synchronization

- After ordinary source changes, run `pnpm exec ng build`.
- After changing tests or component behavior, run `pnpm exec ng test --no-watch` in addition to the build.
- After changing routes, shared services, interceptors, layouts, modals, environment files, SSR, or dependencies, run both checks and manually verify the affected page with `pnpm start`.
- When changing dependencies, build configuration, Angular versions, or test tooling, update the relevant documentation in `README.md` and keep the lockfile in sync.
- When adding a page, update its feature route and the corresponding route index/export when the local feature pattern requires it.
- Before considering a change synchronized, inspect the diff, confirm no generated `dist/` output or local environment files were added, and report any unrelated pre-existing changes instead of reverting them.
- Direct use of `document`, `window`, browser storage, Tabler globals, or `confirm` must remain safe for SSR and tests; interactive elements must also retain keyboard and focus accessibility.
- A guard is not active merely because it exists: verify that it is attached to the relevant route before relying on it for protection.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default in Angular v20+.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Prefer inline templates for small components
- Prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead
- When using external templates/styles, use paths relative to the component TS file.

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables
- Do not assume globals like (`new Date()`) are available.

## Services

- Design services around a single responsibility
- Use the `@Service()` decorator from `@angular/core` for singleton services; it is tree-shakable and is what every service in `src/` already uses
- Use the `inject()` function instead of constructor injection

# Tabler Agent

> Tabler is packed with beautifully crafted components and powerful features. Jump in and start building a stunning dashboard — all for free!

Tabler is a free and open source dashboard UI kit built on Bootstrap. This file indexes the documentation at <https://docs.tabler.io>.

Every page is available as markdown by appending `.md` to its url, for example `https://docs.tabler.io/ui/components/button.md`. Those files contain the same prose as the html page plus the markup of every example, as fenced code blocks. The whole documentation concatenated into one file is available at `https://docs.tabler.io/llms-full.txt`.

The markup is taken from the rendered pages, so every example is plain html — the same markup the copy button on the page gives you.

An agent skill for building with Tabler is published at `https://docs.tabler.io/.well-known/agent-skills/tabler/SKILL.md` (index: `https://docs.tabler.io/.well-known/agent-skills/index.json`).

### Tabler In This Project

- Installed as `@tabler/core`. Its CSS and JS are registered globally in `angular.json` (`tabler.css`, `tabler-themes.css`, `tabler-socials.css`, `tabler.js`, `tabler-theme.js`), so there is nothing to import per component.
- Tabler JS is reached through the `tabler` global, e.g. `tabler.Modal.getOrCreateInstance(el)`. It only exists in the browser — call it from `afterNextRender` or an event handler, never during construction or SSR.
- Where a Tabler component needs real behaviour, prefer the existing wrappers in `src/app/plugins` (`ModalsService`, `MessageService` for toasts, `ConfirmDialogComponent`, `DataTableComponent`, `TransferComponent`) over driving the global directly.
- Theme settings are written as `data-bs-*` attributes on `document.documentElement` plus `tabler-*` keys in storage; see `SettingsService` and `src/app/layout`.

## Tabler UI — Getting started

- [Introduction](https://docs.tabler.io/ui/getting-started.md): Get started with Tabler - a free, open-source dashboard UI kit built on Bootstrap - and learn how to set it up in your project.
- [Installation](https://docs.tabler.io/ui/getting-started/installation.md): Set up Tabler in a plain HTML file with the CSS and JavaScript from the CDN.
- [Download](https://docs.tabler.io/ui/getting-started/download.md): Download Tabler compiled CSS and JavaScript, get the source code from GitHub, or install it with npm, yarn, or a CDN link.
- [Customize Tabler](https://docs.tabler.io/ui/getting-started/customize.md): Customize Tabler fonts, colors, and styles with CSS variables and Sass to make the framework match your brand.
- [Color modes](https://docs.tabler.io/ui/getting-started/color-modes.md): Set up Tabler's light, dark, and auto color modes, and learn the theme attributes for base color, font, primary color, corner radius and layout.
- [RTL support](https://docs.tabler.io/ui/getting-started/rtl.md): How to enable RTL in Tabler, which stylesheet to load, how the RTL CSS is generated and which utilities are direction-aware.
- [Browser support](https://docs.tabler.io/ui/getting-started/browser-support.md): Which browsers Tabler supports, and the CSS features that set the minimum versions of Chrome, Firefox, Edge, Safari and Opera.

## Tabler UI — Frameworks

- [Frameworks](https://docs.tabler.io/ui/getting-started/frameworks.md): Step-by-step guides for integrating Tabler with popular frameworks.
- [Angular](https://docs.tabler.io/ui/getting-started/frameworks/angular.md): Install Tabler in Angular - register the compiled CSS and JS in angular.json and build a first working page with a Tabler card. This is how this project is wired; the other framework guides do not apply.
- [Angular](https://docs.tabler.io/ui/getting-started/frameworks/angular.md): Install Tabler in Angular - register the compiled CSS and JS in angular.json and build a first working page with a Tabler card.
- [Nuxt](https://docs.tabler.io/ui/getting-started/frameworks/nuxt.md): Install Tabler in Nuxt - register the CSS globally, load the JS in a client-only plugin, and render a minimal page with a Tabler card.
- [Symfony](https://docs.tabler.io/ui/getting-started/frameworks/symfony.md): Install Tabler in Symfony - import the CSS and JS with Webpack Encore and render a minimal Twig layout with a Tabler card.
- [Django](https://docs.tabler.io/ui/getting-started/frameworks/django.md): Install Tabler in Django - serve the CSS and JS through static files and render a minimal template layout with a Tabler card.
- [Rails](https://docs.tabler.io/ui/getting-started/frameworks/rails.md): Install Tabler in Rails - load the CSS and JS with the standard bundling gems and render a minimal ERB layout with a Tabler card.
- [SvelteKit](https://docs.tabler.io/ui/getting-started/frameworks/sveltekit.md): Install Tabler in SvelteKit - import the CSS, load the JS on the client, and render a minimal page layout with a Tabler card.
- [Astro](https://docs.tabler.io/ui/getting-started/frameworks/astro.md): Install Tabler in Astro - import the CSS, load the client-side JavaScript, and render a minimal page layout with a Tabler card.

## Tabler UI — Upgrade

- [Upgrade](https://docs.tabler.io/ui/getting-started/upgrade.md): Upgrade guides for Tabler, one per release, listing the breaking changes with before and after examples.
- [Upgrade to 1.7](https://docs.tabler.io/ui/getting-started/upgrade/1-7.md): Upgrade guide from Tabler 1.6 to 1.7 with the components that moved from the marketing bundle into the core bundle, the visual changes to check, and what to change in your HTML and Sass imports.
- [Upgrade to 1.6](https://docs.tabler.io/ui/getting-started/upgrade/1-6.md): Upgrade guide from Tabler 1.5 to 1.6 with every deprecated class, color variable, Sass variable and JavaScript helper, and what to use instead.
- [Upgrade to 1.5](https://docs.tabler.io/ui/getting-started/upgrade/1-5.md): Upgrade guide from Tabler 1.4 to 1.5 with all breaking changes, renamed classes and Sass updates.

## Tabler UI — Base

- [Base](https://docs.tabler.io/ui/base.md): Foundational styles that every Tabler project builds on - colors, typography, and long-form content - for a consistent user interface.
- [Colors](https://docs.tabler.io/ui/base/colors.md): The Tabler color palette, with base and light shades, the gray scale and social brand colors.
- [Prose](https://docs.tabler.io/ui/base/prose.md): Style long-form content with the prose class. Apply consistent typography to headings, paragraphs, lists, and tables without extra classes.
- [Typography](https://docs.tabler.io/ui/base/typography.md): How Tabler styles headings, paragraphs and text elements to keep content readable.

## Tabler UI — Layout

- [Layout](https://docs.tabler.io/ui/layout.md): How Tabler pages are structured, from navigation placement and content arrangement to page headers.
- [Navbars](https://docs.tabler.io/ui/layout/navbars.md): Responsive navigation bars, with the markup, the variants and a language selector.
- [Navs and tabs](https://docs.tabler.io/ui/layout/navs-tabs.md): Nav styles: tabs, pills, underline, dropdowns and vertical navs.
- [Page headers](https://docs.tabler.io/ui/layout/page-headers.md): Ready-to-use page header examples with titles, subtitles, breadcrumbs, and action buttons for consistent page layouts.
- [Page layouts](https://docs.tabler.io/ui/layout/page-layouts.md): Combine containers, navigation and page structure into a complete dashboard layout, step by step.

## Tabler UI — Components

- [Components](https://docs.tabler.io/ui/components.md): Browse all Tabler UI components - buttons, cards, modals, tables, and more - with live previews and ready-to-copy HTML code.
- [Accordion](https://docs.tabler.io/ui/components/accordion.md): Build collapsible content sections with accordion classes and modifiers.
- [Alert](https://docs.tabler.io/ui/components/alert.md): Show alert messages that inform users about success, info, warning, or danger states, with icons, links, and dismissible options.
- [Avatar](https://docs.tabler.io/ui/components/avatar.md): Display user avatars with images, initials, or icons. Change sizes, shapes, and colors, or group them into stacked avatar lists.
- [Badge](https://docs.tabler.io/ui/components/badge.md): Add badges to show counts, labels, or statuses. Use colors, outline and pill styles, links, and notification dots to add context.
- [Breadcrumb](https://docs.tabler.io/ui/components/breadcrumb.md): A breadcrumb trail for the current page location, with separators, icons and a muted style.
- [Button](https://docs.tabler.io/ui/components/button.md): Buttons in many colors, sizes and shapes, with icons, loading states and social styles.
- [Card](https://docs.tabler.io/ui/components/card.md): Organize content with flexible cards. Combine headers, footers, images, and actions to build sections that work on any screen size.
- [Card gradient](https://docs.tabler.io/ui/components/card-gradient.md): Gradient card backgrounds, with color variants, multi-color themes, directions and animation.
- [Carousel](https://docs.tabler.io/ui/components/carousel.md): Display multiple images or slides in a compact carousel with indicators, captions, and automatic or manual navigation.
- [Chat](https://docs.tabler.io/ui/components/chat.md): Build a conversation view with message bubbles, sender info, and a typing indicator.
- [Confetti](https://docs.tabler.io/ui/components/confetti.md): Celebrate a success with a confetti shower triggered from a button or from JavaScript, with the amount, length, speed and colors set by data attributes.
- [Data grid](https://docs.tabler.io/ui/components/datagrid.md): Display labeled data in a compact datagrid - a responsive list of title and content pairs, ideal for detail panels and summaries.
- [Divider](https://docs.tabler.io/ui/components/divider.md): Separate content with horizontal dividers. Add optional text labels aligned left, center, or right to keep layouts clear.
- [Dropdown](https://docs.tabler.io/ui/components/dropdown.md): Build dropdown menus that reveal lists of options or actions. Add icons, dividers, and custom triggers to keep the interface clean.
- [Empty state](https://docs.tabler.io/ui/components/empty.md): Empty states for first use, no results and error screens, with an image, text and an action.
- [Hover gallery](https://docs.tabler.io/ui/components/hover-gallery.md): Show several photos in the space of one with the hover gallery component - a good fit for product cards, portfolios and listings.
- [Icon](https://docs.tabler.io/ui/components/icon.md): Use over 5000 free MIT-licensed Tabler icons in your interface. Learn how to embed, size, and animate them in your projects.
- [Icon list](https://docs.tabler.io/ui/components/icon-list.md): Show a list with icon markers instead of bullets. Change the icon and the color per list or per entry, or lay the entries out in a row.
- [Image label](https://docs.tabler.io/ui/components/image-label.md): Pin a short label to a corner of an image with the image label component. Change the corner and the color, or add an icon.
- [Legend](https://docs.tabler.io/ui/components/legend.md): Label chart series with colored markers. Group legend items in a list, add values, and let users toggle series on click.
- [List group](https://docs.tabler.io/ui/components/list-group.md): Group related items in one list.
- [Map](https://docs.tabler.io/ui/components/map.md): Show interactive maps with markers, popups and a matching dark mode from plain HTML, using MapLibre GL JS and free map tiles. No account or access token is needed.
- [Mention](https://docs.tabler.io/ui/components/mention.md): Add inline mention chips to text - people, tags, apps and colors - with an avatar, a logo or a color swatch inside.
- [Modal](https://docs.tabler.io/ui/components/modal.md): Create modal dialogs for notifications, confirmations, and custom content. Adjust sizes, add forms, and control them with JavaScript.
- [Mockup browser](https://docs.tabler.io/ui/components/mockup-browser.md): Show a screenshot or any content inside a browser window frame with the mockup browser component. Add an address, a lock icon or your own colors.
- [Nav overflow](https://docs.tabler.io/ui/components/nav-overflow.md): Keep a nav on one line by moving the items that do not fit into a More dropdown.
- [Offcanvas](https://docs.tabler.io/ui/components/offcanvas.md): Sidebar panel that slides in from the edge of the viewport.
- [Pagination](https://docs.tabler.io/ui/components/pagination.md): Navigate through multi-page content with pagination controls.
- [Placeholder](https://docs.tabler.io/ui/components/placeholder.md): Reserve space for loading content with placeholders. Use lines, headings, and image blocks to build skeleton screens.
- [Popover](https://docs.tabler.io/ui/components/popover.md): Show extra information in popovers when a tooltip is not enough. Set the placement and trigger to control how they appear.
- [Pricing](https://docs.tabler.io/ui/components/pricing.md): Show your plans side by side with the pricing component. Each card has a title, a price, a feature list and a button. Mark one card as featured to raise it above the others.
- [Progress bar](https://docs.tabler.io/ui/components/progress.md): Show task progress with progress bars. Set the value, color, and size, or use indeterminate bars for unknown durations.
- [Progress steps](https://docs.tabler.io/ui/components/progress-step.md): Use progress steps to display compact onboarding, checkout, and setup progress.
- [Ribbon](https://docs.tabler.io/ui/components/ribbon.md): Highlight cards and other elements with ribbons. Change the position, color, and shape, or add icons and text to draw attention.
- [Segmented control](https://docs.tabler.io/ui/components/segmented-control.md): A set of mutually exclusive options displayed as connected buttons.
- [Shape](https://docs.tabler.io/ui/components/shape.md): Put an icon on a soft, tinted background with the shape component. Pick a size and a color, or set the corner radius with a CSS variable.
- [Signal](https://docs.tabler.io/ui/components/signal.md): Show priority, strength or any other level with the signal component - a compact row of stepped bars that sits next to a label.
- [Sparkline](https://docs.tabler.io/ui/components/sparkline.md): Show a trend inline with a sparkline. Line, bar and circle charts rendered as SVG from data attributes, themed with text colors, sized with classes.
- [Spinner](https://docs.tabler.io/ui/components/spinner.md): Show loading states with spinners. Choose between border and growing styles, change colors and sizes, or place them in buttons.
- [Star rating](https://docs.tabler.io/ui/components/star-rating.md): Build star rating UI with stars classes and data-star-rating state selectors.
- [Status](https://docs.tabler.io/ui/components/status.md): Highlight the state of an element with a small status badge. Add colors, dots, and animations to make statuses easy to scan.
- [Step](https://docs.tabler.io/ui/components/step.md): Guide users through multi-step processes with a steps indicator. Show completed, active, and upcoming steps in a clear layout.
- [Switch icon](https://docs.tabler.io/ui/components/switch-icon.md): Animate a smooth transition between two icons with the switch icon component - useful for likes, toggles, and favorite buttons.
- [Tab](https://docs.tabler.io/ui/components/tab.md): Split content into tabs so users can switch between related views in the same context. Add icons or style the tabs as pills.
- [Table](https://docs.tabler.io/ui/components/table.md): Present data in clean, responsive tables. Add hover states, striped rows, sorting, and other options to improve readability.
- [Tag](https://docs.tabler.io/ui/components/tag.md): Build inline tag elements with the tag class API and related tag part classes.
- [Text shimmer](https://docs.tabler.io/ui/components/text-shimmer.md): Show that something is in progress with the text shimmer - a light band that moves across a label such as "Thinking…" until the result arrives.
- [Timeline](https://docs.tabler.io/ui/components/timeline.md): Visualize events in chronological order with a timeline. Add icons, avatars, and text to present processes and activity feeds.
- [Toast](https://docs.tabler.io/ui/components/toast.md): Display a lightweight alert notification with a toast.
- [Tooltip](https://docs.tabler.io/ui/components/tooltip.md): Explain interface elements with tooltips shown on hover. Control placement, add HTML content, and customize how they appear.
- [Tracking](https://docs.tabler.io/ui/components/tracking.md): Visualize activity logs and uptime data with the tracking component - a compact row of colored blocks that show status over time.
- [Trending](https://docs.tabler.io/ui/components/trending.md): Show how a value changed with a trend indicator. Combine a number, arrow icon, and color so users can read direction at a glance.

## Tabler UI — Forms

- [Forms](https://docs.tabler.io/ui/forms.md): Everything you need to build accessible forms - inputs, selects, checkboxes, validation states, and helper elements.
- [Elements](https://docs.tabler.io/ui/forms/elements.md): Styled inputs, selects, checkboxes and radios, with states, sizes and layout options.
- [Fieldset](https://docs.tabler.io/ui/forms/fieldset.md): Group related form fields with a styled fieldset to make long forms better organized and easier to understand.
- [Floating labels](https://docs.tabler.io/ui/forms/floating-labels.md): Show labels inside form controls.
- [Helpers](https://docs.tabler.io/ui/forms/helpers.md): Add help icons, hints, and required field markers to forms to give users extra guidance and reduce input errors.
- [Select group](https://docs.tabler.io/ui/forms/select-group.md): Offer sets of options with select groups - labeled, icon-only, or pill-shaped controls built on checkboxes and radios.
- [Image check](https://docs.tabler.io/ui/forms/image-check.md): Checkboxes and radios with an image as the label, for choosing templates, themes or products in a form.
- [Color check](https://docs.tabler.io/ui/forms/color-check.md): Checkboxes and radios styled as color swatches, plus the native color picker input.
- [Password strength](https://docs.tabler.io/ui/forms/strength.md): Show how strong a password is with a segmented meter that rates it as the user types, with configurable scoring and labels.
- [Validation](https://docs.tabler.io/ui/forms/validation.md): Show valid and invalid states on form fields with feedback messages, so users know exactly what to correct before submitting.
- [OTP input](https://docs.tabler.io/ui/forms/otp-input.md): Add a one-time password (OTP) input with grouped slots, masking, and keyboard, paste and SMS autofill support.
- [Number input](https://docs.tabler.io/ui/forms/number-input.md): Add a number input with min, max and step, hold-to-repeat buttons, arrow key support and change events.
- [CSS variables](https://docs.tabler.io/ui/forms/css-variables.md): Reference of the CSS variables that size and color Tabler form controls, with examples of runtime overrides.

## Tabler UI — Utilities

- [Utilities](https://docs.tabler.io/ui/utilities.md): Small single-purpose CSS classes for spacing, borders, cursors, and interactions - adjust details without writing custom CSS.
- [Background blur](https://docs.tabler.io/ui/utilities/background-blur.md): Use the bg-blur utility to add a backdrop blur behind translucent overlays, sticky bars and floating cards.
- [Background patterns](https://docs.tabler.io/ui/utilities/background-patterns.md): Add decorative background patterns with utility classes.
- [Borders](https://docs.tabler.io/ui/utilities/borders.md): Style borders with utility classes - control sides, colors, width, and radius without writing any custom CSS.
- [Cursors](https://docs.tabler.io/ui/utilities/cursors.md): Change the mouse cursor with utility classes to signal what users can do with an element, from pointers to resize handles.
- [Gradients](https://docs.tabler.io/ui/utilities/gradients.md): Create directional gradients with utility classes.
- [Interactions](https://docs.tabler.io/ui/utilities/interactions.md): Control how users interact with content - manage text selection and pointer events with simple utility classes.
- [Margins](https://docs.tabler.io/ui/utilities/margins.md): Control the space between elements with margin utilities.
- [Printing](https://docs.tabler.io/ui/utilities/printing.md): What Tabler hides when a page is printed, the d-print-\* utilities and the media-print mixin for custom print styles.
- [Vertical align](https://docs.tabler.io/ui/utilities/vertical-align.md): Utility classes for the vertical-align property on inline and table-cell elements.
- [Visually hidden](https://docs.tabler.io/ui/utilities/visually-hidden.md): Hide elements visually while keeping them accessible to screen readers.

## Tabler UI — Plugins

- [Plugins](https://docs.tabler.io/ui/plugins.md): Optional Tabler plugins - charts, calendars, editors, file uploads, flags, payment logos, social icons and extra theme base colors. Each one needs a file you load on demand.
- [Advanced select](https://docs.tabler.io/ui/plugins/advanced-select.md): Build searchable single and multi-value selects with the Tom Select plugin, including optgroups, validation states, and rich options with avatars or flags.
- [Autosize](https://docs.tabler.io/ui/plugins/autosize.md): Make textareas grow automatically as users type with autosize, so longer text stays visible without manual resizing.
- [Chart](https://docs.tabler.io/ui/plugins/chart.md): Build interactive line, bar, pie, and other charts with ApexCharts. Copy ready-to-use configurations styled to match Tabler.
- [Clipboard](https://docs.tabler.io/ui/plugins/clipboard.md): Add copy to clipboard buttons that copy a field, an element or a literal string, with a copied state and events.
- [Color picker](https://docs.tabler.io/ui/plugins/color-picker.md): Let users pick a color in a form.
- [Countup](https://docs.tabler.io/ui/plugins/countup.md): Animate numbers with the countup component, for statistics on dashboards and landing pages.
- [Date picker](https://docs.tabler.io/ui/plugins/date-picker.md): Let users pick a date, a range or several dates with the Datepicker plugin, as a text input, a button or an inline calendar.
- [Dropzone](https://docs.tabler.io/ui/plugins/dropzone.md): Add drag-and-drop file uploads to your forms with the Dropzone library, styled to match Tabler out of the box.
- [Flags](https://docs.tabler.io/ui/plugins/flags.md): Display country flags with the Tabler flags plugin. Add a flag with two CSS classes and scale it with ready-made size modifiers.
- [FullCalendar](https://docs.tabler.io/ui/plugins/fullcalendar.md): Show events in a calendar.
- [Inline player](https://docs.tabler.io/ui/plugins/inline-player.md): Embed audio and video with a lightweight, accessible media player that supports HTML5, YouTube, and Vimeo sources.
- [Input mask](https://docs.tabler.io/ui/plugins/input-mask.md): Format user input automatically with input masks for dates, phone numbers, and other patterns to reduce validation errors.
- [Lightbox](https://docs.tabler.io/ui/plugins/lightbox.md): Open images and videos in a fullscreen lightbox.
- [Range slider](https://docs.tabler.io/ui/plugins/range-slider.md): Let users pick a value or range with a slider powered by noUiSlider, styled to match the rest of your Tabler interface.
- [Signature](https://docs.tabler.io/ui/plugins/signature.md): Collect a drawn signature from users.
- [Social icons](https://docs.tabler.io/ui/plugins/social-icons.md): Add social media icons with the socials plugin. Link users to your profiles with recognizable, consistently styled buttons.
- [Sortable](https://docs.tabler.io/ui/plugins/sortable.md): Make lists, table rows and cards draggable with the data-sortable attribute, SortableJS options as JSON and the Sortable component.
- [Theme base colors](https://docs.tabler.io/ui/plugins/theme-base-colors.md): Load the tabler-themes plugin and switch between six gray base palettes (slate, gray, zinc, neutral, stone, mist) with data-bs-theme-base.
- [Vector map](https://docs.tabler.io/ui/plugins/vector-map.md): Draw a world map as inline SVG from data attributes. Shade countries by value, add markers and lines, show tooltips and let users zoom, with no map library.
- [WYSIWYG editor](https://docs.tabler.io/ui/plugins/wysiwyg.md): Add rich text editing to your forms with HugeRTE, a flexible WYSIWYG editor that works well with Tabler styles.

## Tabler UI — Resources

- [FAQ](https://docs.tabler.io/ui/getting-started/faq.md): Find answers to the most common questions about using Tabler, including licensing, framework support, and reporting issues.
- [Docs for LLMs](https://docs.tabler.io/ui/getting-started/llms.md): Use llms.txt and the markdown version of every Tabler docs page to give AI tools accurate, up-to-date context about Tabler.
- [3rd-party libraries & resources](https://docs.tabler.io/ui/getting-started/references.md): Open source libraries and resources used by Tabler.
- [How to contribute](https://docs.tabler.io/ui/getting-started/how-to-contribute.md): Guide to contributing to Tabler and setting up for development.
- [Docs callouts](https://docs.tabler.io/ui/getting-started/callouts.md): Info, Tip, Warning, Danger and Note callouts for Tabler documentation pages, with usage rules and accessibility notes.
- [Tabler license](https://docs.tabler.io/ui/getting-started/license.md): Tabler's MIT license: freedom to use, modify, and share.

## Tabler UI

- [Tabler UI](https://docs.tabler.io/ui.md): Free and open source web application UI kit based on Bootstrap

## Tabler Icons — Libraries

- [Libraries](https://docs.tabler.io/icons/libraries.md): Official Tabler Icons libraries for React, Vue, Svelte, Angular and more. Use over 5000 icons as native components or a simple webfont.
- [Astro](https://docs.tabler.io/icons/libraries/astro.md): Add Tabler Icons to Astro projects with the official tree-shakable package. Import any of over 5000 icons as native Astro components.
- [Preact](https://docs.tabler.io/icons/libraries/preact.md): Add Tabler Icons to Preact apps with the official tree-shakable package. Import any of over 5000 lightweight SVG icons as components.
- [React](https://docs.tabler.io/icons/libraries/react.md): Add Tabler Icons to React apps with the official tree-shakable package. Import over 5000 scalable SVG icons as customizable components.
- [SolidJS](https://docs.tabler.io/icons/libraries/solidjs.md): Add Tabler Icons to SolidJS apps with the official tree-shakable package. Import over 5000 high-quality SVG icons as components.
- [Svelte](https://docs.tabler.io/icons/libraries/svelte.md): Add Tabler Icons to Svelte apps with the official tree-shakable package. Import over 5000 clean SVG icons as customizable components.
- [Vue](https://docs.tabler.io/icons/libraries/vue.md): Add Tabler Icons to Vue apps with the official package. Import over 5000 SVG icons as Vue components with tree-shaking and full styling control.
- [Webfont](https://docs.tabler.io/icons/libraries/webfont.md): Use Tabler Icons as a webfont and add any of the icons with a CSS class.

## Tabler Icons — Plugins

- [Plugins](https://docs.tabler.io/icons/plugins.md): Bring Tabler Icons into your design tools with official plugins. Access the full, always up-to-date icon set right inside Figma.
- [Figma plugin](https://docs.tabler.io/icons/plugins/figma.md): Insert any of the Tabler Icons into your designs with the Figma plugin, without leaving Figma.

## Tabler Icons — Static files

- [Static files](https://docs.tabler.io/icons/static-files.md): Download Tabler Icons as static files in SVG, PNG and PDF formats. Pick the format that fits your design or development workflow.
- [PDF version](https://docs.tabler.io/icons/static-files/pdf.md): Download Tabler Icons as PDF files, ready to place in documents, presentations, and print workflows without conversion.
- [PNG version](https://docs.tabler.io/icons/static-files/png.md): Download Tabler Icons as PNG files - raster images for mockups, chat apps, and tools that do not support vector formats.
- [SVG version](https://docs.tabler.io/icons/static-files/svg.md): Download Tabler Icons as SVG files that scale without quality loss. Use them as images, backgrounds, or inline elements on the web.

## Tabler Icons

- [Tabler Icons](https://docs.tabler.io/icons.md): Over 5000 pixel-perfect icons for web design and development
- [Icon design guide](https://docs.tabler.io/icons/design-guide.md): Design rules for Tabler Icons. Canvas size, stroke, corner radius and spacing, plus the SVG format, naming and icon requests.

## Tabler Illustrations

- [Tabler Illustrations](https://docs.tabler.io/illustrations.md): Customizable SVG illustrations for web and mobile designs, in light and dark variants.
- [Preview](https://docs.tabler.io/illustrations/preview.md): Browse all Tabler Illustrations in light and dark themes.
- [Contents](https://docs.tabler.io/illustrations/contents.md): The folder structure of the Tabler Illustrations package and where to find each format, color variant and theme.
- [Customization](https://docs.tabler.io/illustrations/customization.md): Customize Tabler Illustrations with CSS variables: the colors, the dark variant and auto dark mode.
- [License](https://docs.tabler.io/illustrations/license.md): License terms for personal and team use of Tabler Illustrations.

## Tabler Emails

- [Tabler Emails](https://docs.tabler.io/emails.md): Customizable email templates for over 90 clients and devices.
- [Contents](https://docs.tabler.io/emails/contents.md): See what is inside the Tabler Emails package - folder structure, compiled HTML templates ready to use, and editable source files.
- [Compiled templates](https://docs.tabler.io/emails/compiled-html.md): How to use the compiled HTML email templates from the Tabler Emails package.
- [Source templates](https://docs.tabler.io/emails/source-html.md): How to use the source HTML email templates from the Tabler Emails package.

## Tabler Payments — Libraries

- [Libraries](https://docs.tabler.io/payments/libraries.md): Official Tabler Payments packages for React, Vue, Preact and Astro. Use over 100 payment provider logos as native, tree-shakable components.
- [Astro](https://docs.tabler.io/payments/libraries/astro.md): Add Tabler payment provider logos to Astro projects with the official tree-shakable package. Render 100+ payment provider logos as native components.
- [Preact](https://docs.tabler.io/payments/libraries/preact.md): Add Tabler payment provider logos to Preact apps with the official tree-shakable package. Render 100+ payment provider logos as components.
- [React](https://docs.tabler.io/payments/libraries/react.md): Add Tabler payment provider logos to React apps with the official tree-shakable package. Render 100+ payment provider logos as components.
- [Vue](https://docs.tabler.io/payments/libraries/vue.md): Add Tabler payment provider logos to Vue apps with the official tree-shakable package. Render 100+ payment provider logos as components.

## Tabler Payments

- [Tabler Payments](https://docs.tabler.io/payments.md): Over 160 payment provider logos for checkout pages and payment forms.
- [CSS plugin](https://docs.tabler.io/payments/css.md): Show payment provider logos like Visa, Mastercard, and PayPal with the payments CSS plugin to make checkout pages clearer.

## Other

- [Welcome to Tabler Documentation](https://docs.tabler.io/index.md): Guides, examples and reference for Tabler UI components, plugins, icons and the other Tabler tools.

## Class names

Every class each component ships, grouped by kind. A name in braces stands for a family: `alert-{color}` means any base color, `table-mobile-{breakpoint}` any breakpoint.

### Accordion

[Accordion documentation](https://docs.tabler.io/ui/components/accordion)

- component: `accordion`
- part: `accordion-item`, `accordion-header`, `accordion-button`, `accordion-body`
- style: `accordion-button-toggle`, `accordion-button-toggle-plus`
- modifier: `accordion-flush`, `accordion-inverted`, `accordion-tabs`

### Alert

[Alert documentation](https://docs.tabler.io/ui/components/alert)

- component: `alert`
- part: `alert-heading`, `alert-description`, `alert-icon`, `alert-link`, `alert-action`, `alert-list`
- style: `alert-important`, `alert-minor`, `alert-dismissible`
- color: `alert-{color}`

### Avatar

[Avatar documentation](https://docs.tabler.io/ui/components/avatar)

- component: `avatar`
- part: `avatar-brand`, `avatar-cover`, `avatar-upload-text`, `avatar-list`
- style: `avatar-square`, `avatar-rounded`, `avatar-upload`
- modifier: `avatar-list-stacked`
- color: `bg-{color}`
- size: `avatar-{size}`

### Badge

[Badge documentation](https://docs.tabler.io/ui/components/badge)

- component: `badge`
- part: `badge-dot`, `badge-icononly`, `badge-notification`, `badge-list`
- style: `badge-outline`
- modifier: `badge-pill`
- behavior: `badge-blink`
- color: `bg-{color}`
- size: `badge-sm`, `badge-lg`

### Breadcrumb

[Breadcrumb documentation](https://docs.tabler.io/ui/components/breadcrumb)

- component: `breadcrumb`
- part: `breadcrumb-item`
- style: `breadcrumb-arrows`, `breadcrumb-bullets`, `breadcrumb-dots`, `breadcrumb-muted`

### Button

[Button documentation](https://docs.tabler.io/ui/components/button)

- component: `btn`
- part: `btn-action`, `btn-actions`, `btn-list`, `btn-group`, `btn-toolbar`, `btn-check`
- style: `btn-outline`, `btn-ghost`, `btn-link`
- modifier: `btn-pill`, `btn-square`, `btn-icon`, `btn-floating`
- behavior: `btn-loading`, `btn-animate-icon`
- color: `btn-{color}`
- size: `btn-sm`, `btn-lg`, `btn-xl`

### Card

[Card documentation](https://docs.tabler.io/ui/components/card)

- component: `card`
- part: `card-header`, `card-title`, `card-subtitle`, `card-body`, `card-footer`, `card-meta`, `card-actions`, `card-btn`, `card-img-top`, `card-img-overlay`, `card-cover`, `card-stamp`, `card-status-top`, `card-progress`, `card-tabs`, `card-header-tabs`, `card-table`, `card-code`, `card-list-group`
- style: `card-borderless`, `card-transparent`, `card-dashed`
- modifier: `card-stacked`, `card-rotate-start`, `card-body-scrollable`
- behavior: `card-active`, `card-inactive`, `card-link`
- size: `card-sm`, `card-md`, `card-lg`

### Card gradient

[Card gradient documentation](https://docs.tabler.io/ui/components/card-gradient)

- component: `card-gradient`
- style: `card-gradient-{preset}`
- behavior: `card-gradient-animated`
- direction: `card-gradient-bottom`
- color: `card-gradient-{color}`, `card-gradient-start`, `card-gradient-end`

### Carousel

[Carousel documentation](https://docs.tabler.io/ui/components/carousel)

- component: `carousel`
- part: `carousel-inner`, `carousel-item`, `carousel-caption`, `carousel-indicators`, `carousel-control-prev`, `carousel-control-next`
- style: `carousel-caption-background`, `carousel-indicators-dot`, `carousel-indicators-thumb`, `carousel-indicators-vertical`
- behavior: `carousel-fade`

### Chat

[Chat documentation](https://docs.tabler.io/ui/components/chat)

- component: `chat`
- part: `chat-bubbles`, `chat-bubble`, `chat-bubble-title`, `chat-bubble-author`, `chat-bubble-date`, `chat-bubble-body`
- style: `chat-bubble-me`

### Data grid

[Data grid documentation](https://docs.tabler.io/ui/components/datagrid)

- component: `datagrid`
- part: `datagrid-item`, `datagrid-title`, `datagrid-content`
- modifier: `datagrid-cols-{n}`
- direction: `datagrid-horizontal`

### Divider

[Divider documentation](https://docs.tabler.io/ui/components/divider)

- component: `hr`
- part: `hr-text`
- modifier: `hr-text-spaceless`
- direction: `hr-text-start`, `hr-text-end`
- color: `text-{color}`

### Dropdown

[Dropdown documentation](https://docs.tabler.io/ui/components/dropdown)

- component: `dropdown`
- part: `dropdown-toggle`, `dropdown-menu`, `dropdown-item`, `dropdown-item-icon`, `dropdown-item-indicator`, `dropdown-item-text`, `dropdown-header`, `dropdown-divider`, `dropdown-menu-columns`, `dropdown-menu-card`
- style: `dropdown-menu-arrow`, `dropdown-menu-dark`, `dropdown-toggle-split`
- behavior: `dropdown-menu-scrollable`
- direction: `dropdown-menu-end`, `dropend`

### Empty state

[Empty state documentation](https://docs.tabler.io/ui/components/empty)

- component: `empty`
- part: `empty-header`, `empty-img`, `empty-icon`, `empty-title`, `empty-subtitle`, `empty-action`
- style: `empty-bordered`

### Flags

[Flags documentation](https://docs.tabler.io/ui/plugins/flags)

- component: `flag`
- color: `flag-country-{code}`
- size: `flag-{size}`

### Hover gallery

[Hover gallery documentation](https://docs.tabler.io/ui/components/hover-gallery)

- component: `hover-gallery`
- part: `hover-gallery-item`
- modifier: `hover-gallery-indicators`

### Icon

[Icon documentation](https://docs.tabler.io/ui/components/icon)

- component: `icon`
- style: `icon-filled`, `icon-inline`
- behavior: `icon-pulse`, `icon-tada`, `icon-rotate`
- size: `icon-sm`, `icon-md`, `icon-lg`

### Icon list

[Icon list documentation](https://docs.tabler.io/ui/components/icon-list)

- component: `list-icon`
- part: `icon`
- direction: `list-icon-inline`
- color: `text-{color}`

### Image label

[Image label documentation](https://docs.tabler.io/ui/components/image-label)

- component: `img-labeled`
- part: `img-label`
- modifier: `img-label-icon`
- direction: `img-label-top-start`, `img-label-top-end`, `img-label-bottom-start`
- color: `bg-{color}`

### Legend

[Legend documentation](https://docs.tabler.io/ui/components/legend)

- component: `legend-dot`, `legend`, `legend-list`
- part: `legend-value`, `legend-unit`
- style: `legend-list-vertical`, `legend-list-divided`
- color: `legend-{color}`
- size: `legend-lg`
- state: `active`, `legend-off`

### List group

[List group documentation](https://docs.tabler.io/ui/components/list-group)

- component: `list-group`
- part: `list-group-item`, `list-group-header`, `list-group-item-media`, `list-group-item-content`, `list-group-item-title`, `list-group-item-description`, `list-group-item-meta`, `list-group-item-actions`, `list-group-selectable-check`, `list-group-nested`, `list-group-item-toggle`
- style: `list-group-flush`, `list-group-transparent`, `list-group-numbered`, `list-separated`
- behavior: `list-group-hoverable`, `list-group-item-action`, `list-group-selectable`
- direction: `list-group-horizontal`
- color: `list-group-item-{color}`
- size: `list-group-sm`, `list-group-lg`

### Map

[Map documentation](https://docs.tabler.io/ui/components/map)

- component: `ratio`, `map-marker`
- part: `map-marker-label`, `map-controls`
- modifier: `map-marker-dot`, `map-marker-pulse`, `map-marker-pin`, `map-popup-card`, `map-tooltip`
- color: `map-marker-{color}`
- size: `ratio-{ratio}`

### Mention

[Mention documentation](https://docs.tabler.io/ui/components/mention)

- component: `mention`
- part: `mention-avatar`, `mention-app`, `mention-color`, `mention-count`

### Mockup browser

[Mockup browser documentation](https://docs.tabler.io/ui/components/mockup-browser)

- component: `mockup-browser`
- part: `mockup-browser-toolbar`, `mockup-browser-controls`, `mockup-browser-url`, `mockup-browser-body`
- size: `mockup-browser-sm`

### Modal

[Modal documentation](https://docs.tabler.io/ui/components/modal)

- component: `modal`
- part: `modal-dialog`, `modal-content`, `modal-header`, `modal-title`, `modal-body`, `modal-footer`, `modal-status`
- style: `modal-blur`
- modifier: `modal-dialog-centered`, `modal-dialog-scrollable`, `modal-full-width`, `modal-fullscreen`
- size: `modal-sm`, `modal-lg`, `modal-xl`

### Nav overflow

[Nav overflow documentation](https://docs.tabler.io/ui/components/nav-overflow)

- component: `nav-overflow`
- items: `nav-overflow-keep`
- toggle: `nav-overflow-item`, `nav-overflow-toggle`, `nav-overflow-menu`, `nav-overflow-icon`, `nav-overflow-text`

### Number input

[Number input documentation](https://docs.tabler.io/ui/forms/number-input)

- component: `number-input`
- size: `number-input-sm`, `number-input-lg`

### Offcanvas

[Offcanvas documentation](https://docs.tabler.io/ui/components/offcanvas)

- component: `offcanvas`
- part: `offcanvas-header`, `offcanvas-title`, `offcanvas-body`, `offcanvas-footer`
- modifier: `offcanvas-narrow`, `offcanvas-floating`
- direction: `offcanvas-start`, `offcanvas-end`, `offcanvas-top`, `offcanvas-bottom`
- size: `offcanvas-{breakpoint}`

### OTP input

[OTP input documentation](https://docs.tabler.io/ui/forms/otp-input)

- component: `otp`
- part: `otp-slots`, `otp-slot`, `otp-separator`
- modifier: `otp-connected`
- size: `otp-sm`, `otp-lg`

### Pagination

[Pagination documentation](https://docs.tabler.io/ui/components/pagination)

- component: `pagination`
- part: `page-item`, `page-link`, `page-item-title`, `page-item-subtitle`
- style: `pagination-outline`, `pagination-circle`
- size: `pagination-sm`, `pagination-lg`

### Placeholder

[Placeholder documentation](https://docs.tabler.io/ui/components/placeholder)

- component: `placeholder`
- behavior: `placeholder-glow`, `placeholder-wave`
- size: `placeholder-xs`, `placeholder-sm`, `placeholder-lg`

### Popover

[Popover documentation](https://docs.tabler.io/ui/components/popover)

- component: `popover`
- part: `popover-header`, `popover-body`, `popover-arrow`

### Pricing

[Pricing documentation](https://docs.tabler.io/ui/components/pricing)

- component: `pricing`
- part: `pricing-card`, `pricing-title`, `pricing-label`, `pricing-price`, `pricing-price-currency`, `pricing-price-description`, `pricing-features`, `pricing-btn`
- modifier: `featured`

### Progress bar

[Progress bar documentation](https://docs.tabler.io/ui/components/progress)

- component: `progress`
- part: `progress-bar`
- style: `progress-bar-striped`
- modifier: `progress-stacked`, `progress-separated`
- behavior: `progress-bar-animated`, `progress-bar-indeterminate`
- color: `bg-{color}`
- size: `progress-sm`, `progress-lg`, `progress-xl`

### Progress steps

[Progress steps documentation](https://docs.tabler.io/ui/components/progress-step)

- component: `progress-steps`
- part: `progress-steps-item`

### Ribbon

[Ribbon documentation](https://docs.tabler.io/ui/components/ribbon)

- component: `ribbon`
- style: `ribbon-bookmark`
- direction: `ribbon-top`, `ribbon-bottom`, `ribbon-start`
- color: `bg-{color}`

### Segmented control

[Segmented control documentation](https://docs.tabler.io/ui/components/segmented-control)

- component: `nav-segmented`
- direction: `nav-segmented-vertical`
- size: `nav-sm`, `nav-lg`

### Shape

[Shape documentation](https://docs.tabler.io/ui/components/shape)

- component: `shape`
- color: `shape-{color}`
- size: `shape-{size}`

### Signal

[Signal documentation](https://docs.tabler.io/ui/components/signal)

- component: `signal`
- part: `signal-bar`
- color: `signal-{color}`
- state: `active`

### Social icons

[Social icons documentation](https://docs.tabler.io/ui/plugins/social-icons)

- component: `social`
- style: `social-gray`
- color: `social-app-{app}`
- size: `social-{size}`

### Sparkline

[Sparkline documentation](https://docs.tabler.io/ui/components/sparkline)

- component: `sparkline`
- part: `sparkline-svg`, `sparkline-label`
- size: `sparkline-sm`, `sparkline-lg`, `sparkline-square`
- state: `sparkline-disabled`

### Spinner

[Spinner documentation](https://docs.tabler.io/ui/components/spinner)

- component: `spinner-border`, `spinner-grow`
- color: `text-{color}`
- size: `spinner-border-sm`, `spinner-grow-sm`

### Star rating

[Star rating documentation](https://docs.tabler.io/ui/components/star-rating)

- component: `stars`

### Status

[Status documentation](https://docs.tabler.io/ui/components/status)

- component: `status`
- part: `status-dot`, `status-indicator`
- style: `status-lite`, `status-indicator-circle`
- behavior: `status-dot-animated`, `status-indicator-animated`
- color: `status-{color}`

### Step

[Step documentation](https://docs.tabler.io/ui/components/step)

- component: `steps`
- part: `step-item`
- modifier: `steps-counter`
- direction: `steps-vertical`
- color: `steps-{color}`

### Switch icon

[Switch icon documentation](https://docs.tabler.io/ui/components/switch-icon)

- component: `switch-icon`
- part: `switch-icon-a`, `switch-icon-b`
- behavior: `switch-icon-fade`, `switch-icon-flip`, `switch-icon-scale`, `switch-icon-slide-{direction}`
- state: `active`, `switch-icon-loading`

### Tab

[Tab documentation](https://docs.tabler.io/ui/components/tab)

- component: `nav-tabs`
- part: `nav-link`, `tab-content`, `tab-pane`
- style: `nav-underline`, `nav-bordered`
- modifier: `nav-fill`

### Table

[Table documentation](https://docs.tabler.io/ui/components/table)

- component: `table`
- part: `td-truncate`
- style: `table-striped`, `table-bordered`, `table-borderless`, `table-transparent`
- modifier: `table-responsive`, `table-mobile-{breakpoint}`, `table-vcenter`, `table-center`, `table-nowrap`
- behavior: `table-hover`, `table-selectable`, `table-sort`
- color: `table-{color}`
- size: `table-sm`

### Tag

[Tag documentation](https://docs.tabler.io/ui/components/tag)

- component: `tag`
- part: `tag-avatar`, `tag-flag`, `tag-payment`, `tag-icon`, `tag-check`, `tag-badge`, `tag-list`

### Text shimmer

[Text shimmer documentation](https://docs.tabler.io/ui/components/text-shimmer)

- component: `text-shimmer`
- part: `text-shimmer-loader`

### Timeline

[Timeline documentation](https://docs.tabler.io/ui/components/timeline)

- component: `timeline`
- part: `timeline-event`, `timeline-event-icon`, `timeline-event-card`
- style: `timeline-simple`

### Toast

[Toast documentation](https://docs.tabler.io/ui/components/toast)

- component: `toast`
- part: `toast-container`, `toast-header`, `toast-body`
- color: `toast-{color}`

### Tooltip

[Tooltip documentation](https://docs.tabler.io/ui/components/tooltip)

- component: `tooltip`
- part: `tooltip-inner`, `tooltip-arrow`

### Tracking

[Tracking documentation](https://docs.tabler.io/ui/components/tracking)

- component: `tracking`
- part: `tracking-block`
- modifier: `tracking-squares`

### Trending

[Trending documentation](https://docs.tabler.io/ui/components/trending)

- component: `d-inline-flex align-items-center lh-1`
- color: `text-green`, `text-red`, `text-muted`

### Vector map

[Vector map documentation](https://docs.tabler.io/ui/plugins/vector-map)

- component: `vector-map`
- part: `vector-map-svg`, `vector-map-region`, `vector-map-region-point`, `vector-map-region-point-dot`, `vector-map-region-point-halo`, `vector-map-marker`, `vector-map-marker-dot`, `vector-map-marker-halo`, `vector-map-line`, `vector-map-tooltip`, `vector-map-controls`, `vector-map-legend`, `vector-map-legend-label`, `vector-map-legend-scale`, `vector-map-legend-step`
- style: `vector-map-colors`
