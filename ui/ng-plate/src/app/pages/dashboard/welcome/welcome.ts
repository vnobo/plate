import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TokenService } from '@app/core';

interface ModuleCard {
  title: string;
  desc: string;
  link: string;
  icon: 'users' | 'tenant' | 'role';
}

@Component({
  selector: 'app-welcome',
  imports: [RouterLink],
  templateUrl: './welcome.html',
  styleUrl: './welcome.scss',
})
export class Welcome {
  protected readonly token = inject(TokenService);

  protected readonly modules: ReadonlyArray<ModuleCard> = [
    { title: '用户管理', desc: '账号与直接权限', link: '/platform/users', icon: 'users' },
    { title: '租户管理', desc: '租户与成员', link: '/platform/tenant', icon: 'tenant' },
    { title: '角色管理', desc: '角色与授权', link: '/platform/role', icon: 'role' },
  ];
}
