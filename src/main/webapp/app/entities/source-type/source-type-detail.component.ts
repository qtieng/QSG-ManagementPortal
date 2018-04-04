import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';

@Component({
    selector: 'jhi-source-type-detail',
    templateUrl: './source-type-detail.component.html'
})
export class SourceTypeDetailComponent implements OnInit, OnDestroy {

    sourceType: SourceType;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private sourceTypeService: SourceTypeService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['sourceType', 'sourceTypeScope']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion']);
        });
        this.registerChangeInSourceTypes();
    }

    load(producer: string, model: string, version: string) {
        this.sourceTypeService.find(producer, model, version).subscribe((sourceType) => {
            this.sourceType = sourceType;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInSourceTypes() {
        this.eventSubscriber = this.eventManager.subscribe('sourceTypeListModification',
            (response) => this.load(this.sourceType.producer, this.sourceType.model, this.sourceType.catalogVersion));
    }
}
