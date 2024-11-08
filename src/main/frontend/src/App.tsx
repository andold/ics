import React, { useRef, useEffect, } from "react";
import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "ag-grid-community/styles/ag-grid.css"; // Mandatory CSS required by the Data Grid
import "ag-grid-community/styles/ag-theme-balham.css"; // Optional Theme applied to the Data Grid

import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import moment from "moment-timezone";
import "moment/locale/ko";

// container
import IcsContainter from "./container/IcsContainter";

function App() {
	const ref = useRef(null);

	useEffect(() => {
		moment.tz.setDefault("Asia/Seoul");
		moment.locale("ko");
	}, []);
	return (
		<div ref={ref} className="App">
			<DndProvider backend={HTML5Backend}>
				<IcsContainter />
			</DndProvider>
		</div>
	);
}

export default App;
