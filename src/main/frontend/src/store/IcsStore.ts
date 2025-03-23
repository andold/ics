import { makeAutoObservable } from "mobx";
import moment from "moment";

import repository from "../repository/IcsRepository";

const cellStyleLeft = { textAlign: "left", padding: 1, };
const cellStyleRight = { textAlign: "right", padding: 1, paddingRight: 4, };
const cellStyleCenter = { textAlign: "center", padding: 1, };
// IcsStore.ts
class IcsStore {
	constructor() {
		makeAutoObservable(this);
	}

	crawlNaver(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.crawlNaver(request, onSuccess, onError, element);
	}
	createCalendar(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.createCalendar(request, onSuccess, onError, element);
	}
    searchCalendar(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.searchCalendar(request, onSuccess, onError, element);
    }

	batch(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.batch(request, onSuccess, onError, element);
	}
	create(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.create(request, onSuccess, onError, element);
	}
    search(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.search(request, onSuccess, onError, element);
    }
	update(request: any[], onSuccess?: any, onError?: any, element?: any) {
		repository.update(request, onSuccess, onError, element);
	}
	remove(request: any, onSuccess?: any, onError?: any, element?: any) {
		repository.remove(request, onSuccess, onError, element);
	}
	upload(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.upload(request, onSuccess, onError, element);
	}
	download(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.download(request, onSuccess, onError, element);
	}
	downloadIcs(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.downloadIcs(request, onSuccess, onError, element);
	}
	deduplicate(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.deduplicate(request, onSuccess, onError, element);
	}
	backup(request?: any, onSuccess?: any, onError?: any, element?: any) {
		repository.backup(request, onSuccess, onError, element);
	}


	//	utils
	isInDay(day: moment.Moment, start: moment.Moment, end: moment.Moment): boolean {
		const startx = day.clone().startOf("day");
		const endx = day.clone().add(1, "days").startOf("day");
		return start.isBefore(endx) && end.isAfter(startx);
	}
	isInDayEvent(day: moment.Moment, event: any): boolean {
		for (let period of event!.periods) {
			const start = moment(period.start);
			const end = moment(period.end);
			if (this.isInDay(day, start, end)) {
				return true;
			}
		}

		return false;
	}
	columnDefs(hides?: string[]): any {
		return [{
			field: "id",
			hide: hides && hides.includes("id"),
			editable: false,
			cellStyle: cellStyleRight,
			width: 32,
			rowDrag: true,
		}, {
			field: "name",
			hide: hides && hides.includes("name"),
			cellStyle: cellStyleLeft,
			width: 32,
		}, {
			field: "vcalendarId",
			hide: hides && hides.includes("vcalendarId"),
			cellStyle: cellStyleRight,
			width: 16,
		}, {
			field: "start",
			hide: hides && hides.includes("start"),
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD (dd) HH:mm"),
			width: 64,
			cellStyle: cellStyleCenter,
			flex: 1,
		}, {
			field: "end",
			hide: hides && hides.includes("end"),
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD HH:mm"),
			width: 32 + 16,
			cellStyle: cellStyleCenter,
			flex: 1,
		}, {
			field: "summary",
			hide: hides && hides.includes("summary"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 2,
		}, {
			field: "location",
			hide: hides && hides.includes("location"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 1,
		}, {
			field: "description",
			hide: hides && hides.includes("description"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 2,
		}, {
			field: "content",
			hide: hides && hides.includes("content"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 1,
		}, {
			field: "created",
			hide: hides && hides.includes("created"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 32,
			flex: 1,
			cellStyle: cellStyleCenter,
		}, {
			field: "updated",
			hide: hides && hides.includes("updated"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 32,
			flex: 1,
			cellStyle: cellStyleCenter,
		}];
	}
    columnDefsCalendar(hides?: string[]): any {
		return [{
			field: "id",
			hide: hides && hides.includes("id"),
			editable: false,
			cellStyle: cellStyleRight,
			width: 32,
			rowDrag: true,
		}, {
			field: "title",
			hide: hides && hides.includes("title"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 1,
		}, {
			field: "description",
			hide: hides && hides.includes("description"),
			cellStyle: cellStyleLeft,
			width: 64,
			flex: 2,
		}, {
			field: "created",
			hide: hides && hides.includes("created"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 64,
			flex: 1,
			cellStyle: cellStyleCenter,
		}, {
			field: "updated",
			hide: hides && hides.includes("updated"),
			editable: false,
			valueFormatter: (params: any) => moment(params.value).format("YYYY-MM-DD"),
			width: 64,
			flex: 1,
			cellStyle: cellStyleCenter,
		}];
    }
}
export default new IcsStore();
