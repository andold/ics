import { AgGridReact } from "ag-grid-react";
import moment, { Moment } from "moment";
import React, { forwardRef, useEffect, useImperativeHandle, useRef, useState } from "react";
import { Accordion, Button, CloseButton, Col, Dropdown, Form, InputGroup, Modal, Popover, Row, Spinner } from "react-bootstrap";
import DragAndDropFile from "../view/DragAndDropFile";

// domain

// store
import store from "../store/IcsStore";

// view

// IcsContainter.tsx
interface IcsForm {
	vcalendarId: number;
	today: moment.Moment;
	showUploadModal: boolean;

	start: moment.Moment | null;
	end: moment.Moment | null;
	keyword: string;

	mode: number;
}
export default ((props: any) => {
	const [form, setForm] = useState<IcsForm>({
		vcalendarId: 1028,
		today: moment().startOf("day"),
		showUploadModal: false,

		start: null,
		end: null,
		keyword: "",

		mode: 0,
	});

	const modes = [
		<EventCalendarMode
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>,
		<EventAgGridMode
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>,
		<CalendarAgGridMode
			form={form}
			onChange={(params: any) => setForm({ ...form, ...params, })}
		/>,
	];

	useEffect(() => {
	}, [form]);

	function handleOnChange(params: any) {
		setForm({
			...form,
			...params,
		});
	}

	return (<>
		<Header
			form={form}
			onChange={handleOnChange}
		/>
		<Row className="m-0 py-1 bg-dark py-0 border-top">
			<Col className="text-danger">Sunday</Col>
			<Col className="text-white">Monday</Col>
			<Col className="text-white">Tuesday</Col>
			<Col className="text-white">Wednesday</Col>
			<Col className="text-white">Thursday</Col>
			<Col className="text-white">Friday</Col>
			<Col className="text-primary">Saturday</Col>
		</Row>
		{modes[form.mode % modes.length]}
	</>);
});

function CalendarAgGridMode(props: any) {
	const form = props.form as IcsForm;

	const gridRef = useRef<AgGridReact>(null);
	const [rowData, setRowData] = useState([]);
	const [columnDefs, setColumnDefs] = useState([]);

	useEffect(() => {
		const comlumDefs = store.columnDefsCalendar([]);
		comlumDefs.push({
			field: "",
			headerName: "▦",
			maxWidth: 64,
			cellStyle: {
				textAlign: "center",
			},
			checkboxSelection: true,
			sortable: false,
		});
		setColumnDefs(comlumDefs);
	}, []);
	useEffect(() => {
		const request: any = {
			vcalendarId: form.vcalendarId,
			start: null,
			end: null,
			keyword: form.keyword,
		};
		store.search(request, (_: any, result: any) => setRowData(result));
		return function() { setRowData([]); };
	}, [form]);

	function handleOnGridReady() {
		gridRef.current!.api.sizeColumnsToFit();
		gridRef.current!.api.setGridOption("domLayout", "autoHeight");
	}

	return (<>
		<AgGridReact
			className="ag-theme-balham-dark"
			ref={gridRef}
			rowData={rowData}
			columnDefs={columnDefs}
			defaultColDef={{
				editable: true,
				sortable: true,
				resizable: true,
				suppressHeaderMenuButton: true,
			}}
			rowDragManaged={true}
			rowSelection="multiple"
			onGridReady={handleOnGridReady}
		/>
	</>);
}
function EventCalendarMode(props: any) {
	const form = props.form as IcsForm;

	const [weeks, setWeeks] = useState<Moment[]>([]);
	const [events, setEvents] = useState([]);

	useEffect(() => {
		const start: Moment = form.today.clone().date(1).startOf("week");
		const end: Moment = form.today.clone().add(1, "months").date(1).add(6, "days").startOf("week");
		const days: Moment[] = [];
		for (let cx: Moment = start.clone(); cx.isBefore(end); cx = cx.add(7, "days")) {
			days.push(cx.clone());
		}
		setWeeks(days);

		store.search({
			vcalendarId: form.vcalendarId,
			start: start.format("YYYY-MM-DDTHH:mm:ss.SSSZ"),
			end: end.format("YYYY-MM-DDTHH:mm:ss.SSSZ"),
		}, (_: any, result: any) => setEvents(result));
		return function() { setEvents([]); };
	}, [form]);

	return (<>
		{
			weeks.map((week: any) => (
				<Week
					key={week.valueOf()}
					start={week}
					events={events}
				/>
			))
		}
	</>);
}
function EventAgGridMode(props: any) {
	const form = props.form as IcsForm;

	const gridRef = useRef<AgGridReact>(null);
	const [rowData, setRowData] = useState([]);
	const [columnDefs, setColumnDefs] = useState([]);

	useEffect(() => {
		const comlumDefs = store.columnDefs(["created"]);
		comlumDefs.push({
			field: "operate",
			headerName: "▦",
			width: 16,
			cellStyle: { textAlign: "center", },
			valueFormatter: () => "▦",
			cellEditor: AgGridOperateCellEditor,
			cellEditorPopup: true,
			cellEditorParams: {
				form: form,
			},
		});
		setColumnDefs(comlumDefs);
	}, []);
	useEffect(() => {
		const request: any = {
			vcalendarId: form.vcalendarId,
			start: null,
			end: null,
			keyword: form.keyword,
		};
		if (form.keyword?.length > 0) {
			request.start = form.start?.format("YYYY-MM-DDTHH:mm:ss.SSSZ");
			request.end = form.end?.format("YYYY-MM-DDTHH:mm:ss.SSSZ");
		} else {
			request.start = (form.start && form.start.format("YYYY-MM-DDTHH:mm:ss.SSSZ")) || form.today.clone().date(1).startOf("week").format("YYYY-MM-DDTHH:mm:ss.SSSZ");
			request.end = (form.end && form.end.format("YYYY-MM-DDTHH:mm:ss.SSSZ")) || form.today.clone().add(1, "months").date(1).add(6, "days").startOf("week").format("YYYY-MM-DDTHH:mm:ss.SSSZ");
		}
		store.search(request, (_: any, result: any) => setRowData(result));
		return function() { setRowData([]); };
	}, [form]);

	function handleOnGridReady() {
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.sizeColumnsToFit();
		gridRef.current!.api.setGridOption("domLayout", "autoHeight");
	}

	return (<>
		<AgGridReact
			className="ag-theme-balham-dark"
			ref={gridRef}
			rowData={rowData}
			columnDefs={columnDefs}
			defaultColDef={{
				editable: true,
				sortable: true,
				resizable: true,
				suppressMenu: true,
			}}
			rowDragManaged={true}
			rowSelection="multiple"
			onGridReady={handleOnGridReady}
		/>
	</>);
}
const AgGridOperateCellEditor = forwardRef((props: any, ref: any) => {
	const { api, value, data } = props;

	useImperativeHandle(ref, () => {
		return {
			getValue: () => !value,
			isPopup: () => true,
		};
	});

	function handleOnRemoved() {
		api.applyTransaction({ remove: [data], });
		api.stopEditing(true);
	}

	return (<>
		<Popover className="bg-black text-white" style={{ width: 640 }} >
			<Popover.Header as="h3" className="bg-black text-white">{data.title}</Popover.Header>
			<Popover.Body className="p-1 bg-dark text-white">
				<ul className="mb-1 px-2">id: {data.id}</ul>
				<ul className="mb-1 px-2">name: {data.name}</ul>
				<ul className="mb-1 px-2">vcalendarId: {data.vcalendarId}</ul>
				<ul className="mb-1 px-2">start: {data.start}</ul>
				<ul className="mb-1 px-2">end: {data.end}</ul>
				<ul className="mb-1 px-2">summary: {data.summary}</ul>
				<ul className="mb-1 px-2">location: {data.location}</ul>
				<ul className="mb-1 px-2 text-truncate">content: {data.content}</ul>
				<ul className="mb-1 px-2">created: {data.created}</ul>
				<ul className="mb-1 px-2">updated: {data.updated}</ul>
				<Button className="m-1" size="sm"
					onClick={() => {
						store.remove(data, handleOnRemoved);
					}}
				>삭제</Button>
			</Popover.Body>
		</Popover>
	</>);
});
function Week(props: any) {
	const { start, events } = props;

	const [days, setDays] = useState<any[]>([]);

	useEffect(() => {
		const end = start.clone().add(7, "days");
		const things: any[] = [];
		for (let cy = start.clone(); cy.isBefore(end); cy = cy.add(1, "days")) {
			things.push(cy.clone());
		}
		setDays(things);
	}, [start]);

	return (<>
		<Row xs={7} md={7} lg={7} className="mx-0 px-0 bg-black text-white" style={{ minHeight: 128 }}>
			{days.map((day: any) => (<Day
				key={day.valueOf()}
				today={day}
				events={events}
			/>))}
		</Row>
	</>);
}
function Day(props: any) {
	const { today, events } = props;

	const isToday = moment().startOf("day").isSame(today.clone().startOf("day"));
	const clazz = isToday ? "mx-0 px-1 text-truncate bg-dark border border-primary" : "mx-0 px-1 text-truncate border-top border-secondary";
	const ref = useRef<any>(null);

	useEffect(() => {
	}, [today]);

	if (isToday && ref.current) {
		ref.current.scrollIntoView();
	}

	return (<>
		<Col ref={ref} className={clazz}>
			<DayHeader
				today={today}
			/>
			<DayEvent
				today={today}
				events={events!.filter((event: any) => store.isInDayEvent(today, event))}
			/>
		</Col>
	</>);
}
function DayHeader(props: any) {
	const { today } = props;

	function classDayOfWeek(day: any) {
		const clazzes = ["text-danger", "text-white", "text-white", "text-white", "text-white", "text-white", "text-primary"];
		return clazzes[day.day()];
	}

	useEffect(() => {
	}, []);

	return (<>
		<Row className="mx-0 px-0 py-1 border-bottom justify-content-center">
			<Col xs={2} className="mx-1 p-0"></Col>
			<Col className={`m-0 p-0 h6 ${classDayOfWeek(today)}`} title={`${today.format("YYYY-MM-DD (dd)")}`}>
				{today.format("D")}
			</Col>
			<Col xs={2} className="mx-1 p-0"></Col>
		</Row>
	</>);
}
function DayEvent(props: any) {
	const { today, events } = props;

	function colorTimeOfDay(time: moment.Moment) {
		if (time.hours() < 6) { return "#808080"; }
		if (time.hours() < 9) { return "#C0C0C0"; }
		if (time.hours() < 12) { return "#FFFFFF"; }
		if (time.hours() < 18) { return "#C0C0D0"; }
		if (time.hours() < 21) { return "#808090"; }
		return "#808080";
	}

	return (<>{
		events!.map((event: any) => event.periods.filter((period: any) => store.isInDay(today, moment(period.start), moment(period.end)))
			.map((period: any) => {
				const start = moment(period.start);
				const end = moment(period.end);
				if (end.diff(start, "days") >= 1 && start.isSame(start.clone().startOf("day"))) {
					const title = `${start.format("YYYY-MM-DD (dd)")} ${event.summary} ${event.location}`;
					return (
						<Row key={Math.random()} className="mx-0 px-0 text-start bg-black text-white" title={title}>
							<Col className="text-truncate mx-0 ms-1 px-0" >{event.summary} {event.location}</Col>
						</Row>
					);
				}

				const title = `${start.format("YYYY-MM-DD (dd) HH:mm")} ~ ${end.format("HH:mm")} ${event.summary} ${event.location}`;
				const times = `${start.format("HH:mm")}~${end.format("HH:mm")}`;
				return (
					<Row key={Math.random()} className="mx-0 px-0 text-start bg-black text-white" title={title}>
						<Col xs="auto" className="mx-0 px-0" style={{
							color: colorTimeOfDay(start),
						}}
						>{times}</Col>
						<Col className="text-truncate mx-0 ms-1 px-0" >{event.summary} {event.location}</Col>
					</Row>
				);
			}))
	}</>);
}
function Header(props: any) {
	const { form, onChange } = props;

	const [showUploadModal, setShowUploadModal] = useState(false);
	const inputRef = useRef<any>(null);
	const [calendars, setCalendars] = useState<any[]>([]);

	useEffect(() => {
		store.searchCalendar({}, (_: any, result: any[]) => setCalendars(result));
	}, []);

	function handleOnClickDownload() {
		const yyyymmdd = moment().format("YYYYMMDD");
		store.downloadIcs({ filename: `calendar-${yyyymmdd}.ics`, vcalendarId: form.vcalendarId, });
	}
	function handleOnKeyPressKeyword(event: any) {
		if (event.key === "Enter") {
			onChange && onChange({ keyword: event.target.value, });
		}
	}
	function handleOnClickSearch() {
		inputRef.current && onChange && onChange({ keyword: inputRef.current!.value, });
	}
	function handleOnClickCreateCalendar() {
		store.createCalendar({
			title: "홍길동",
			description: "예제",
		});
	}

	return (<>
		<Row className="mx-0 py-1 bg-dark border-top border-secondary">
			<Col xs="auto" className="text-white text-start m-0 p-0 me-auto">
				<Form.Select size="sm" className="bg-dark text-white" value={form.vcalendarId || ""}
					onChange={(event: any) => onChange && onChange({ vcalendarId: Number(event.target.value), })}
				>
					<option value="">일정</option>
					{calendars.map((calendar: any) => (<option key={calendar.id} value={calendar.id}>{calendar.title}</option>))}
				</Form.Select>
			</Col>
			{form.mode % 3 === 0 ? (
				<Col xs="auto" className="text-white text-start m-0 p-0 me-auto">
					<Button size="sm" className="mx-4 mb-1 fs-6 py-0" style={{ cursor: "pointer" }}
						onClick={() => onChange({ today: moment().startOf("day") })}
					>today</Button>
					<small className="mx-1 border rounded px-1" style={{ cursor: "pointer" }}
						onClick={() => onChange({ today: form.today.clone().subtract(1, "years").startOf("day") })}
					>{form.today.clone().subtract(1, "years").format("YYYY")}</small>
					<span className="mx-1 fs-6 border rounded px-2" style={{ cursor: "pointer" }}
						onClick={() => onChange({ today: form.today.clone().subtract(1, "months").startOf("day") })}
					>{form.today.clone().subtract(1, "months").format("MM")}</span>
					<span className="mx-3 fs-4" style={{ cursor: "pointer" }}>{form.today.clone().format("YYYY-MM")}</span>
					<span className="mx-1 fs-6 border rounded px-2" style={{ cursor: "pointer" }}
						onClick={() => onChange({ today: form.today.clone().add(1, "months").startOf("day") })}
					>{form.today.clone().add(1, "months").format("MM")}</span>
					<small className="mx-1 border rounded px-1" style={{ cursor: "pointer" }}
						onClick={() => onChange({ today: form.today.clone().add(1, "years").startOf("day") })}
					>{form.today.clone().add(1, "years").format("YYYY")}</small>
				</Col>
			) : (
				<Col xs="auto" className="px-1 me-auto">
					<InputGroup size="sm">
						<Form.Control type="date" className="bg-dark text-white" value={form.start && form.start.format("YYYY-MM-DD")} onChange={(event: any) => onChange && onChange({
							start: event.target.value ? moment(event.target.value) : undefined,
						})} />
						<Form.Control type="date" className="bg-dark text-white" value={form.end ? form.end.format("YYYY-MM-DD") : ""} onChange={(event: any) => onChange && onChange({
							end: event.target.value ? moment(event.target.value) : undefined,
						})} />
						<Col xs="auto" className="ms-4">
							<Form.Control size="sm" type="search" className="bg-dark text-white" defaultValue={form.keyword}
								ref={inputRef}
								onKeyPress={handleOnKeyPressKeyword}
							/>
						</Col>
						<Col xs="auto" className="ms-1">
							<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={handleOnClickSearch}>찾기</Button>
						</Col>
					</InputGroup>
				</Col>
			)}
			<Col xs="auto" className="">
				<InputGroup size="sm">
					<Dropdown>
						<Dropdown.Toggle id="dropdown-basic">메뉴</Dropdown.Toggle>
						<Dropdown.Menu>
							<Dropdown.Item onClick={(param: any) => {
								store.backup();
							}}>백업</Dropdown.Item>
							<Dropdown.Item onClick={(param: any) => {
								store.crawlNaver({});
							}}>크롤 네이버</Dropdown.Item>
						</Dropdown.Menu>
					</Dropdown>

					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={handleOnClickCreateCalendar}>만들기</Button>
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={(_: any) => setShowUploadModal(true)}>올리기</Button>
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={handleOnClickDownload}>다운로드</Button>
					<Button size="sm" variant="secondary" className="ms-1" title={form.mode.toString()} onClick={(_: any) => onChange && onChange({ mode: form.mode + 1 })}>모드</Button>
				</InputGroup>
			</Col>
		</Row>
		<UploadModal
			form={form}
			show={showUploadModal}
			onChange={onChange}
			onClose={() => setShowUploadModal(false)}
		/>
	</>);
}
function UploadModal(props: any) {
	const { form, show, onChange, onClose } = props;

	const [spinner, setSpinner] = useState<string>("");

	function handleOnSubmit(file: any) {
		onChange && onChange({ map: new Map() });
		setSpinner("올린 파일 분석중");
		store.upload({ file: file, vcalendarId: form.vcalendarId, }, (_: any, result: any) => {
			onChange && onChange({ map: result });
			setSpinner("");
		});
	}
	function handleOnClickDeduplicate() {
		onChange && onChange({ map: new Map() });
		setSpinner("중복제거 분석중");
		store.deduplicate({ vcalendarId: form.vcalendarId }, (_: any, result: any) => {
			onChange && onChange({
				map: result,
			});
			setSpinner("");
		});
	}

	return (<>
		<Modal show={show} size="lg" fullscreen={"xxl-down"} centered>
			<Modal.Header className="bg-black text-white">
				<Modal.Title>
					{spinner.length > 0 && <Spinner animation="grow" variant="warning" className="ms-0 me-1 align-middle" title={spinner} />}
					일정 올리기
					<Button variant="secondary" size="sm" className="bg-black text-white mx-4" name="deduplicate" onClick={handleOnClickDeduplicate}>중복제거</Button>
				</Modal.Title>
				<CloseButton onClick={onClose} />
			</Modal.Header>
			<Modal.Body className="bg-black text-white">
				<DragAndDropFile
					onSubmit={handleOnSubmit}
					onDrop={handleOnSubmit}
					types={["application/json", "text/calendar"]}
				/>
				<UploadResult
					form={form}
					onChange={onChange}
				/>
			</Modal.Body>
			<Modal.Footer className="bg-black text-white" style={{ maxHeight: 128, overflowY: "scroll", }}>
				<Button variant="primary" size="sm" onClick={onClose}>Close</Button>
			</Modal.Footer>
		</Modal>
	</>);
}
function UploadResult(props: any) {
	const { form, onChange } = props;

	const [creates, setCreates] = useState([]);
	const [updates, setUpdates] = useState([]);
	const [removes, setRemoves] = useState([]);
	const [duplicates, setDuplicates] = useState([]);

	function asort(a: any, b: any) {
		const xa = !a ? "" : a["content"] || "";
		const xb = !b ? "" : b["content"] || "";

		return xa.localeCompare(xb);
	}

	useEffect(() => {
		if (!form.map) {
			setCreates([]);
			setDuplicates([]);
			setUpdates([]);
			setRemoves([]);
			return;
		}

		form.map.creates && setCreates(form.map.creates.sort((a: any, b: any) => asort(a, b)));
		form.map.duplicates && setDuplicates(form.map.duplicates.sort((a: any, b: any) => asort(a, b)));
		form.map.updates && setUpdates(form.map.updates.sort((a: any, b: any) => asort(a, b)));
		form.map.removes && setRemoves(form.map.removes.sort((a: any, b: any) => asort(a, b)));
		return () => {
			setCreates([]);
			setDuplicates([]);
			setUpdates([]);
			setRemoves([]);
		};
	}, [form]);

	return (<>
		<Accordion defaultActiveKey="create" className="p-0">
			{
				(!creates || !creates.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Create Data!</Row>
				) : (
					<Accordion.Item eventKey={"create"}>
						<Accordion.Header>Create #{creates.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadCreate
								form={form}
								creates={creates}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!removes || !removes.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Remove Data!</Row>
				) : (
					<Accordion.Item eventKey={"remove"}>
						<Accordion.Header>Remove #{removes.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadRemove
								removes={removes}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!updates || !updates.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Update Data!</Row>
				) : (
					<Accordion.Item eventKey={"update"}>
						<Accordion.Header>Update #{updates.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadUpdate
								updates={updates}
								onChange={onChange}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
			{
				(!duplicates || !duplicates.length) ? (
					<Row className="mx-1 p-2 bg-dark text-white">No Identical Data!</Row>
				) : (
					<Accordion.Item eventKey={"read"}>
						<Accordion.Header>Just Read #{duplicates.length}</Accordion.Header>
						<Accordion.Body className="p-0 bg-dark text-white">
							<UploadDuplicate
								duplicates={duplicates}
							/>
						</Accordion.Body>
					</Accordion.Item>
				)
			}
		</Accordion>
	</>);
}
function UploadCreate(props: any) {
	const { form, creates, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.create(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const creates = nodes.map((node: any) => node.data);
		store.batch({ vcalendarId: form.vcalendarId, creates: creates }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={creates}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadUpdate(props: any) {
	const { updates, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.update(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const updates = nodes.map((node: any) => node.data);
		store.batch({ updates: updates }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={updates}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadRemove(props: any) {
	const { removes, onChange } = props;

	function handleOnClickDo(nodes: any) {
		nodes.forEach((node: any) => {
			store.remove(node.data, () => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}
	function handleOnClickDoBatch(nodes: any) {
		const removes = nodes.map((node: any) => node.data);
		store.batch({ removes: removes }, () => {
			nodes.forEach((node: any) => {
				node.setSelected(false);
				node.selectable = false;
				onChange && onChange(node.data);
			});
		});
	}

	return (
		<Common
			events={removes}
			onClickDo={handleOnClickDo}
			onClickDoBatch={handleOnClickDoBatch}
		/>
	);
}
function UploadDuplicate(props: any) {
	const { duplicates, onChange } = props;

	return (
		<Common
			events={duplicates}
			onChange={onChange}
		/>
	);
}
function Common(props: any) {
	const { events, onClickDo, onClickDoBatch } = props;
	const gridRef = useRef<AgGridReact>(null);
	const [rowData, setRowData] = useState([]);
	const [columnDefs, setColumnDefs] = useState([]);

	useEffect(() => {
		const comlumDefs = store.columnDefs();
		comlumDefs.push({
			field: "",
			headerName: "▦",
			maxWidth: 64,
			cellStyle: {
				textAlign: "center",
			},
			checkboxSelection: true,
			sortable: false,
		});
		setColumnDefs(comlumDefs);
	}, []);
	useEffect(() => {
		if (!events) {
			return;
		}
		setRowData(events);
		return function() { setRowData([]); };
	}, [events]);

	function handleOnClickDo() {
		const nodes = gridRef.current!.api.getSelectedNodes();
		onClickDo(nodes);
	}
	function handleOnClickDoBatch() {
		const nodes = gridRef.current!.api.getSelectedNodes();
		onClickDoBatch(nodes);
	}
	function handleOnClickToggleSelectAll() {
		const nodes = gridRef.current!.api.getSelectedNodes();
		nodes.length ? gridRef.current!.api.deselectAll() : gridRef.current!.api.selectAll();
	}
	function handleOnClickSelectAllAndDoBatch() {
		gridRef.current!.api.selectAll();
		handleOnClickDoBatch();
	}
	function handleOnGridReady() {
		gridRef && gridRef.current && gridRef.current.api && gridRef.current.api.sizeColumnsToFit();
		gridRef.current!.api.setGridOption("domLayout", "autoHeight");
	}

	return (<>
		<Row className="mx-1 my-1">
			<Col className="me-auto">
				{onClickDo && (<Button variant="primary" size="sm" className="mx-2" onClick={handleOnClickDo}>Do</Button>)}
				{onClickDoBatch && (<>
					<Button variant="primary" size="sm" className="mx-1" onClick={handleOnClickDoBatch}>Do Batch</Button>
					<Button variant="outline-warning" size="sm" className="mx-4" onClick={handleOnClickSelectAllAndDoBatch}>Select All And Do Batch</Button>
				</>)}
			</Col>
			<Col xs="auto">
				<Button variant="primary" size="sm" onClick={handleOnClickToggleSelectAll}>Toggle Select All</Button>
			</Col>
		</Row>
		<AgGridReact
			className="ag-theme-balham-dark"
			ref={gridRef}
			rowData={rowData}
			columnDefs={columnDefs}
			defaultColDef={{
				editable: true,
				sortable: true,
				resizable: true,
				suppressHeaderMenuButton: true,
			}}
			rowDragManaged={true}
			rowSelection="multiple"
			onGridReady={handleOnGridReady}
		/>
	</>);
}
