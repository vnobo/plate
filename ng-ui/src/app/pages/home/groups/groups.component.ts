import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, type OnInit, signal, WritableSignal } from '@angular/core';
import { Group } from './groups.types';
import { GroupsService } from './groups.service';
import { Subject, takeUntil } from 'rxjs';
import { NzTableModule } from 'ng-zorro-antd/table';

@Component({
  selector: 'app-groups',
  standalone: true,
  imports: [CommonModule, NzTableModule],
  templateUrl: './groups.component.html',
  styleUrl: './groups.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupsComponent implements OnInit, OnDestroy {
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
