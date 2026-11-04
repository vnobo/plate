import { computed, inject, Service } from '@angular/core';
import { UserDetails } from '@plate/types';
import { TokenService } from './token.service';

/**
 * 用户展示信息（姓名、昵称、头像、首字母、角色）。
 * TokenService 只负责令牌与会话，这些展示用的派生字段放这里。
 */
@Service()
export class UserService {
  private readonly token = inject(TokenService);

  readonly details = computed<UserDetails | null>(() => this.token.current()?.details ?? null);

  /** 显示名：昵称优先，否则姓名，否则账号 */
  readonly name = computed(() => {
    const details = this.details();
    if (!details) return '未登录';
    return details.nickname || details.name || details.code || '用户';
  });

  /** 主显示名：姓名优先，缺省退回昵称，再退回账号（需要与昵称分行展示时用这个） */
  readonly displayName = computed(() => {
    const details = this.details();
    if (!details) return '未登录';
    return details.name || details.nickname || details.code || '用户';
  });

  /** 昵称；与主显示名相同时调用方不必重复展示 */
  readonly nickname = computed(() => this.details()?.nickname ?? '');

  /** 头像地址，后端未下发时为 null */
  readonly avatar = computed(() => this.details()?.avatar ?? null);

  /** 首字母，用于头像占位 */
  readonly initial = computed(() => this.name().charAt(0).toUpperCase());

  /** 角色串（authority 拼接） */
  readonly role = computed(() => {
    const authorities = this.details()?.authorities;
    if (!authorities?.length) return '';
    return authorities.map((a) => (typeof a === 'string' ? a : a.authority)).join(' / ');
  });
}
