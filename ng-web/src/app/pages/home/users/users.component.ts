import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, type OnInit, signal, WritableSignal } from '@angular/core';
import { NzTableModule } from 'ng-zorro-antd/table';
import { Subject, takeUntil } from 'rxjs';
import { GroupsService } from '../groups/groups.service';
import { Group } from '../groups/groups.types';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, NzTableModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  groupsList: WritableSignal<Group[]> = signal([]);
  private _subject: Subject<void> = new Subject<void>();

  constructor(private groupService: GroupsService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    const groupRequest: Group = {
      tenantCode: '0',
    };
    this.groupService
      .getGroups(groupRequest)
      .pipe(takeUntil(this._subject))
      .subscribe(result => this.groupsList.set(result));
  }

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}
