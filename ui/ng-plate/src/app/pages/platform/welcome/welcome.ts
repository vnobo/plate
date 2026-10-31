import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';

import { UserService } from '@app/core';
import { environment } from '@envs/env';
import { Group, ROOT_PCODE as GROUP_ROOT_PCODE } from '../role/role.types';
import { ROOT_PCODE as TENANT_ROOT_PCODE, Tenant } from '../tenant/tenant.types';
import { User } from '../users/user.types';

/** 饼图的一块；percent/offset 是 SVG 弧长参数（整圈周长按 100 计） */
interface PieSlice {
  label: string;
  value: number;
  /** Tabler 文字色工具类，SVG 用 stroke=currentColor 取色 */
  color: string;
  percent: number;
  percentText: string;
  offset: number;
}

interface PieChart {
  key: string;
  title: string;
  total: number;
  slices: PieSlice[];
}

interface SliceSource {
  label: string;
  value: number;
  color: string;
}

/**
 * /page 端点的真实响应是 Spring PagedModel：{ content, page: { totalElements } }。
 * 与 @plate/types 里的 Page<T>（顶层 totalElements）不是一回事，故这里按实际结构声明。
 */
interface Paged<T> {
  content: T[];
  page?: { size?: number; number?: number; totalElements?: number; totalPages?: number };
}

/** 只取计数的资源，避免为每个维度都写一遍 ?? 0 */
interface Countable {
  value: () => Paged<unknown>;
}

const EMPTY_PAGE: Paged<never> = {
  content: [],
  page: { size: 0, number: 0, totalElements: 0, totalPages: 0 },
};

/** 后端 max-page-size=100，所以统计只用 size=1 取 totalElements，维度靠过滤条件区分 */
function emptyPage<T>(): Paged<T> {
  return EMPTY_PAGE;
}

@Component({
  selector: 'app-platform-welcome',
  imports: [],
  templateUrl: './welcome.html',
  styleUrl: './welcome.scss',
})
export class PlatformWelcome {
  protected readonly user = inject(UserService);

  private readonly usersEnabled = httpResource<Paged<User>>(
    () => ({
      url: environment.secApiPath + '/users/page',
      params: { page: 0, size: 1, disabled: false },
    }),
    { defaultValue: emptyPage<User>(), debugName: 'stats-users-enabled' },
  );

  private readonly usersDisabled = httpResource<Paged<User>>(
    () => ({
      url: environment.secApiPath + '/users/page',
      params: { page: 0, size: 1, disabled: true },
    }),
    { defaultValue: emptyPage<User>(), debugName: 'stats-users-disabled' },
  );

  private readonly tenantsAll = httpResource<Paged<Tenant>>(
    () => ({ url: environment.secApiPath + '/tenants/page', params: { page: 0, size: 1 } }),
    { defaultValue: emptyPage<Tenant>(), debugName: 'stats-tenants-all' },
  );

  private readonly tenantsRoot = httpResource<Paged<Tenant>>(
    () => ({
      url: environment.secApiPath + '/tenants/page',
      params: { page: 0, size: 1, pcode: TENANT_ROOT_PCODE },
    }),
    { defaultValue: emptyPage<Tenant>(), debugName: 'stats-tenants-root' },
  );

  private readonly groupsAll = httpResource<Paged<Group>>(
    () => ({ url: environment.secApiPath + '/groups/page', params: { page: 0, size: 1 } }),
    { defaultValue: emptyPage<Group>(), debugName: 'stats-groups-all' },
  );

  private readonly groupsRoot = httpResource<Paged<Group>>(
    () => ({
      url: environment.secApiPath + '/groups/page',
      params: { page: 0, size: 1, pcode: GROUP_ROOT_PCODE },
    }),
    { defaultValue: emptyPage<Group>(), debugName: 'stats-groups-root' },
  );

  protected readonly isLoading = computed(() =>
    [
      this.usersEnabled,
      this.usersDisabled,
      this.tenantsAll,
      this.tenantsRoot,
      this.groupsAll,
      this.groupsRoot,
    ].some((r) => r.isLoading()),
  );

  protected readonly charts = computed<PieChart[]>(() => {
    const tenantsTotal = this.count(this.tenantsAll);
    const tenantsRoot = this.count(this.tenantsRoot);
    const groupsTotal = this.count(this.groupsAll);
    const groupsRoot = this.count(this.groupsRoot);

    return [
      this.toChart('users', '用户分布', [
        { label: '启用', value: this.count(this.usersEnabled), color: 'text-green' },
        { label: '禁用', value: this.count(this.usersDisabled), color: 'text-red' },
      ]),
      this.toChart('tenants', '租户分布', [
        { label: '根租户', value: tenantsRoot, color: 'text-primary' },
        { label: '子租户', value: Math.max(tenantsTotal - tenantsRoot, 0), color: 'text-azure' },
      ]),
      this.toChart('groups', '角色分布', [
        { label: '顶层角色', value: groupsRoot, color: 'text-indigo' },
        { label: '子角色', value: Math.max(groupsTotal - groupsRoot, 0), color: 'text-cyan' },
      ]),
    ];
  });

  /** Spring PagedModel 把总数放在 page 里，缺字段时按 0 计 */
  private count(res: Countable): number {
    return res.value().page?.totalElements ?? 0;
  }

  /** 按占比算出每块的弧长与起始偏移，首尾相接铺满一圈 */
  private toChart(key: string, title: string, sources: SliceSource[]): PieChart {
    const total = sources.reduce((sum, s) => sum + s.value, 0);
    let offset = 0;
    const slices = sources.map<PieSlice>((s) => {
      const percent = total > 0 ? (s.value / total) * 100 : 0;
      const slice: PieSlice = {
        ...s,
        percent,
        percentText: `${percent.toFixed(1)}%`,
        offset,
      };
      offset += percent;
      return slice;
    });
    return { key, title, total, slices };
  }
}
