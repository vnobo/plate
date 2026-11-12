import { computed, inject, Service } from '@angular/core';
import { UserDetails } from '@plate/types';
import { TokenService } from './token.service';

/**
 * Display info derived from the user (name, nickname, avatar, initial, roles).
 * TokenService owns only the token and session; these display-only derived fields live here.
 */
@Service()
export class UserService {
  private readonly token = inject(TokenService);

  readonly details = computed<UserDetails | null>(() => this.token.current()?.details ?? null);

  /** Display name: nickname first, then name, then account code. */
  readonly name = computed(() => {
    const details = this.details();
    if (!details) return 'Not signed in';
    return details.nickname || details.name || details.code || 'User';
  });

  /** Primary display name: name first, falling back to nickname, then account code (use when name and nickname must render on separate lines). */
  readonly displayName = computed(() => {
    const details = this.details();
    if (!details) return 'Not signed in';
    return details.name || details.nickname || details.code || 'User';
  });

  /** Nickname; callers need not show it again when identical to the primary display name. */
  readonly nickname = computed(() => this.details()?.nickname ?? '');

  /** Avatar URL; null when the backend does not provide one. */
  readonly avatar = computed(() => this.details()?.avatar ?? null);

  /** Initial letter, used for the avatar placeholder. */
  readonly initial = computed(() => this.name().charAt(0).toUpperCase());

  /** Role string (joined authorities). */
  readonly role = computed(() => {
    const authorities = this.details()?.authorities;
    if (!authorities?.length) return '';
    return authorities.map((a) => (typeof a === 'string' ? a : a.authority)).join(' / ');
  });
}
