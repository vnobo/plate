import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';

import { UserService } from '@app/core';
import { environment } from '@envs/env';
import { Group, ROOT_PCODE as GROUP_ROOT_PCODE } from '../role/role.types';
import { ROOT_PCODE as TENANT_ROOT_PCODE, Tenant } from '../tenant/tenant.types';
import { User } from '../users/user.types';

/** One slice of the pie; percent/offset are SVG arc-length params (full circumference counted as 100). */
interface PieSlice {
  label: string;
  value: number;
  /** Tabler text-color utility class; SVG uses stroke=currentColor to inherit the color. */
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
 * The real /page response is a Spring PagedModel: { content, page: { totalElements } }.
 * This differs from Page<T> in @plate/types (top-level totalElements), so we declare the actual shape here.
 */
interface Paged<T> {
  content: T[];
  page?: { size?: number; number?: number; totalElements?: number; totalPages?: number };
}

/** Resource that only fetches a count, avoiding a repeated ?? 0 for every dimension. */
interface Countable {
  value: () => Paged<unknown>;
}

const EMPTY_PAGE: Paged<never> = {
  content: [],
  page: { size: 0, number: 0, totalElements: 0, totalPages: 0 },
};

/** Backend max-page-size is 100, so stats use size=1 to fetch only totalElements; dimensions are separated by filter conditions. */
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
  protected readonly user: UserService = inject(UserService);

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
      this.toChart('users', 'User Distribution', [
        { label: 'Enabled', value: this.count(this.usersEnabled), color: 'text-green' },
        { label: 'Disabled', value: this.count(this.usersDisabled), color: 'text-red' },
      ]),
      this.toChart('tenants', 'Tenant Distribution', [
        { label: 'Root Tenant', value: tenantsRoot, color: 'text-primary' },
        { label: 'Sub Tenant', value: Math.max(tenantsTotal - tenantsRoot, 0), color: 'text-azure' },
      ]),
      this.toChart('groups', 'Role Distribution', [
        { label: 'Top-level Role', value: groupsRoot, color: 'text-indigo' },
        { label: 'Sub Role', value: Math.max(groupsTotal - groupsRoot, 0), color: 'text-cyan' },
      ]),
    ];
  });

  /** Spring PagedModel stores the total inside page; default to 0 when the field is absent. */
  private count(res: Countable): number {
    return res.value().page?.totalElements ?? 0;
  }

  /** Computes each slice's arc length and starting offset from its share, tiling the full circle end to end. */
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
