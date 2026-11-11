import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserService } from '@app/core';

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
  protected readonly user: UserService = inject(UserService);

  protected readonly modules: ReadonlyArray<ModuleCard> = [
    { title: 'User Management', desc: 'Accounts and direct permissions', link: '/platform/users', icon: 'users' },
    { title: 'Tenant Management', desc: 'Tenants and members', link: '/platform/tenant', icon: 'tenant' },
    { title: 'Role Management', desc: 'Roles and authorization', link: '/platform/role', icon: 'role' },
  ];
}
