import {AfterContentInit, Component, OnInit} from '@angular/core';
import {LoadingService} from "./core/loading.service";
import {Observable, of} from "rxjs";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, AfterContentInit {

    loadingShow$: Observable<boolean> = of(false);

    constructor(private loading: LoadingService) {
        this.loadingShow$ = of(false);
    }

    ngOnInit(): void {
        //this.loadingShow$ = this.loading.progress$;
    }

    ngAfterContentInit(): void {
        this.loadingShow$ = this.loading.progress$;
    }

}
