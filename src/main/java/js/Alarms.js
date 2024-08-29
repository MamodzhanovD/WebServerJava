import {BootstrapTable, TableHeaderColumn} from "react-bootstrap-table";
import {dateFormatterWithDotDelimeter, timeFormatter} from "../../lib/formatters";
import {channelFormatter} from "../../lib/formatters";
import {connect} from "react-redux";
import {openAlarm} from "./tabs/TabsActions";
import {acceptAlarmHandler, completeAlarmHandler} from "./OperatorService";

function Alarms({alarms, height, openAlarm}) {

    const alarmsClassName = (row, rowIdx) => {
        switch (row.event_handling) {
            case 0:
                return "bootstrap-tr red-blink";
            case 1:
                return "bootstrap-tr red";
            default:
                return "";
        }
    }

    const buttonFormatter = (cell, row) => {
        if (row.event_handling === 0) {
            return (
                <div className="bootstrap-table-button bootstrap-table-button-default" onClick={() => acceptAlarmHandler(row)}>
                    <div className="text-in-bootstrap-table-button">
                        <i className="fa-solid fa-check"></i>
                    </div>
                </div>);
        }
        if (row.event_handling === 1) {
            return (
                <div className="bootstrap-table-button bootstrap-table-button-default" onClick={() => completeAlarmHandler(row)}>
                    <div className="text-in-bootstrap-table-button">
                        <i className="fa-solid fa-check-double"></i>
                    </div>
                </div>);
        }
    }

    const continueButtonFormatter = (cell, row) => {
        return (
            <div className="bootstrap-table-button bootstrap-table-button-default" onClick={() => openAlarm(row.num_obj, row.id_alarm)}>
                <div className="text-in-bootstrap-table-button">
                    <i className="fa-solid fa-arrow-right"></i>
                </div>
            </div>);
    };

    return (
        <div className="flex-column window-body-background-color window-body-text-color height-100">
            <div className="height-30-px flex-row space-between align-items-center border-bottom window-header-background-color window-header-text-color">
                <div className="ml-10 font-weight-bold">Тревоги</div>
            </div>
            {alarms.length !== 0 ?
                <BootstrapTable data={alarms}
                                options={{noDataText: 'Нет тревог',}}
                                height={height - 30} scrollTop={'Bottom'}
                                trClassName={alarmsClassName}
                                tableStyle={{border: 'none', padding: 0, margin: 0, color: "var(--window-body-text-color)"}}
                                containerStyle={{padding: 0, margin: 0}}
                                headerStyle={{height: "20px", padding: 0}}>
                    <TableHeaderColumn dataField="id_journal_events" isKey hidden></TableHeaderColumn>
                    <TableHeaderColumn dataField="time_sys"
                                       dataAlign="center"
                                       dataFormat={dateFormatterWithDotDelimeter}
                                       width="90"
                                       thStyle={{border: "none", borderRight: "var(--border)", color: "var(--window-body-text-color)"}}
                                       tdStyle={{
                                           padding: 0,
                                           border: "none",
                                           borderTop: "var(--border)",
                                           borderRight: "var(--border)",
                                           borderBottom: "var(--border)",
                                           position: "relative",
                                           color: "--window-body-text-color"
                                       }}>
                        <div style={{position: "relative", top: "-8px"}}>Дата</div>
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="time_sys"
                                       dataAlign="center"
                                       dataFormat={timeFormatter}
                                       width="80"
                                       thStyle={{borderRight: "var(--border)", color: "var(--window-body-text-color)"}}
                                       tdStyle={{
                                           padding: 0,
                                           borderTop: "var(--border)",
                                           borderRight: "var(--border)",
                                           borderBottom: "var(--border)",
                                           position: "relative",
                                           color: "--window-body-text-color"
                                       }}>
                        <div className="bootstrap-table-operator-header">Время</div>
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="num_obj"
                                       dataAlign="center"
                                       width="74"
                                       thStyle={{borderRight: "var(--border)", color: "var(--window-body-text-color)"}}
                                       tdStyle={{
                                           padding: 0,
                                           borderTop: "var(--border)",
                                           borderRight: "var(--border)",
                                           borderBottom: "var(--border)",
                                           position: "relative"
                                       }}>
                        <div className="bootstrap-table-operator-header">Объект</div>
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="text_event"
                                       headerAlign="center"
                                       dataAlign="left"
                                       thStyle={{color: "var(--window-body-text-color)"}}
                                       tdStyle={{
                                           whiteSpace: 'normal',
                                           padding: "0px 5px 0px 5px",
                                           borderTop: "var(--border)",
                                           borderBottom: "var(--border)",
                                           borderRight: "var(--border)",
                                           position: "relative"
                                       }}>
                        <div className="bootstrap-table-operator-header">Событие</div>
                    </TableHeaderColumn>
                    
                    <TableHeaderColumn dataField="continueButton" dataAlign="center" editable={false}
                                       className="border-bottom text-color border-left-none"
                                       dataFormat={continueButtonFormatter}
                                       width={40}
                                       tdStyle={{
                                           padding: 0,
                                           alignItems: "center",
                                           borderTop: "var(--border)",
                                           borderBottom: "var(--border)",
                                           position: "relative"
                                       }}></TableHeaderColumn>
                </BootstrapTable> :
                <div className="flex-row center height-100 width-100 align-items-center">
                    Тревоги отсутствуют</div>}
        </div>
    )
}

const mapDispatchToProps = dispatch => {
    return {
        openAlarm: (objectNumber, alarmId) => dispatch(openAlarm(objectNumber, alarmId))
    }
}

export default connect(null, mapDispatchToProps)(Alarms)
