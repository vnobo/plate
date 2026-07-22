import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { MessageService, ModalsService } from '@app/plugins';
import { environment } from '@envs/env';
import { Group, GroupAuthority, GroupMember, ROOT_PCODE } from './role.types';
import { RoleForm } from './role-form';
import { AuthorityForm } from './authority-form';
import { MemberForm } from './member-form';

interface RoleTreeNode {
  group: Group;
  depth: number;
  hasChildren: boolean;
  expanded: boolean;
}

@Component({
  selector: 'app-role',
  imports: [],
  templateUrl: './role.html',
  styleUrl: './role.scss',
})
export class Role {
  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly ROOT_PCODE = ROOT_PCODE;

  /** All roles (used for the tree structure and parent selection) */
  protected readonly groupsResource = httpResource<Group[]>(
    () => ({
      url: environment.secApiPath + '/groups/search',
      params: { size: 1000 },
    }),
    { defaultValue: [], debugName: 'role-groups' },
  );

  /** Currently selected role */
  protected readonly selectedGroup = signal<Group | null>(null);
  protected readonly selectedCode = computed(() => this.selectedGroup()?.code ?? null);

  /** Authorities of the selected role */
  protected readonly authoritiesResource = httpResource<GroupAuthority[]>(
    () => {
      const code = this.selectedCode();
      if (!code) return undefined;
      return {
        url: environment.secApiPath + '/groups/authorities/search',
        params: { groupCode: code, size: 1000 },
      };
    },
    { defaultValue: [], debugName: 'role-authorities' },
  );

  /** Members of the selected role */
  protected readonly membersResource = httpResource<GroupMember[]>(
    () => {
      const code = this.selectedCode();
      if (!code) return undefined;
      return {
        url: environment.secApiPath + '/groups/members/search',
        params: { groupCode: code, size: 1000 },
      };
    },
    { defaultValue: [], debugName: 'role-members' },
  );

  protected readonly isLoading = computed(() => this.groupsResource.isLoading());
  protected readonly groups = computed(() => this.groupsResource.value() ?? []);
  protected readonly authorities = computed(() => this.authoritiesResource.value() ?? []);
  protected readonly members = computed(() => this.membersResource.value() ?? []);

  protected readonly activeTab = signal<'authority' | 'member'>('authority');

  /** Set of collapsed role codes (all expanded by default) */
  private readonly collapsedCodes = signal<Set<string>>(new Set<string>());

  /** Visible tree nodes derived from the flat role list (with indentation depth) */
  protected readonly treeNodes = computed<RoleTreeNode[]>(() => {
    const groups = this.groups();
    const codeSet = new Set<string>(
      groups.map((g) => g.code).filter((c): c is string => !!c),
    );
    const childrenMap = new Map<string, Group[]>();
    for (const g of groups) {
      const pc = g.pcode;
      if (!pc || pc === ROOT_PCODE) continue;
      const list = childrenMap.get(pc) ?? [];
      list.push(g);
      childrenMap.set(pc, list);
    }
    const collapsed = this.collapsedCodes();
    const isRoot = (g: Group) => !g.pcode || g.pcode === ROOT_PCODE || !codeSet.has(g.pcode);
    const roots = groups.filter(isRoot);
    const result: RoleTreeNode[] = [];
    const recurse = (list: Group[], depth: number) => {
      for (const g of list) {
        const children = (g.code && childrenMap.get(g.code)) || [];
        const hasChildren = children.length > 0;
        const expanded = g.code ? !collapsed.has(g.code) : true;
        result.push({ group: g, depth, hasChildren, expanded });
        if (hasChildren && expanded) recurse(children, depth + 1);
      }
    };
    recurse(roots, 0);
    return result;
  });

  protected selectGroup(group: Group): void {
    this.selectedGroup.set(group);
    this.activeTab.set('authority');
  }

  protected toggleExpand(code: string, event: Event): void {
    event.stopPropagation();
    this.collapsedCodes.update((set) => {
      const next = new Set(set);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  }

  protected openRoleForm(group: Group | null): void {
    const ref = this._modal.create({
      title: group ? '编辑角色' : '新建角色',
      contentRef: RoleForm,
      contentInputs: { inputData: group, allGroups: this.groups() },
    });
    outputToObservable(ref.instance.dropped)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(() => this.groupsResource.reload());
  }

  protected openAuthorityForm(): void {
    const code = this.selectedCode();
    if (!code) return;
    const ref = this._modal.create({
      title: '添加权限',
      contentRef: AuthorityForm,
      contentInputs: { groupCode: code, inputData: null },
    });
    outputToObservable(ref.instance.dropped)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(() => this.authoritiesResource.reload());
  }

  protected openMemberForm(): void {
    const code = this.selectedCode();
    if (!code) return;
    const ref = this._modal.create({
      title: '添加成员',
      contentRef: MemberForm,
      contentInputs: { groupCode: code },
    });
    outputToObservable(ref.instance.dropped)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(() => this.membersResource.reload());
  }

  protected onDeleteGroup(group: Group): void {
    if (!group.code || group.id == null) return;
    if (!confirm(`确定删除角色「${group.name ?? group.code}」吗？其下权限与成员将一并移除。`)) {
      return;
    }
    this._http
      .delete<void>(environment.secApiPath + '/groups/delete', {
        body: { id: group.id, code: group.code },
      })
      .pipe(
        tap(() => this._message.success('角色已删除')),
        delay(800),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => {
        this.groupsResource.reload();
        if (this.selectedCode() === group.code) this.selectedGroup.set(null);
      });
  }

  protected onDeleteAuthority(item: GroupAuthority): void {
    if (item.id == null) return;
    this._http
      .delete<void>(environment.secApiPath + '/groups/authorities/delete', {
        body: { id: item.id, code: item.code },
      })
      .pipe(
        tap(() => this._message.success('权限已移除')),
        delay(600),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => this.authoritiesResource.reload());
  }

  protected onDeleteMember(item: GroupMember): void {
    if (item.id == null) return;
    this._http
      .delete<void>(environment.secApiPath + '/groups/members/delete', {
        body: { id: item.id, code: item.code },
      })
      .pipe(
        tap(() => this._message.success('成员已移除')),
        delay(600),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => this.membersResource.reload());
  }
}
