// IcsModel.ts
export default interface IcsModel {
	id: number;
	content: string;
	vcalendarId: number;
	start: string;
	end: string;
	created: number;
	updated: number;

	name: number
	summary: string;
	description: string;
	location: string;
	periods: IcsPeriodModel[];
	lastModified: string;
	dateEnd: string;
	uid: string;

	// not support field. user custom.
	custom: any;
}

export interface IcsPeriodModel {
	start: string;
	end: string;
	// not support field. user custom.
	custom: any;
}
