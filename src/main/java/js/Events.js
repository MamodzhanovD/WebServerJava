import { trClassNameFormat } from "../../lib/formatters";
import { nomberNameFormat } from "../../lib/formatters";
import { nameFormat } from "../../lib/formatters";
import { adressNameFormat } from "../../lib/formatters";
import { eventsNameFormat } from "../../lib/formatters";
import { BootstrapTable, TableHeaderColumn } from "react-bootstrap-table";
import { dateFormatterWithDotDelimeter, timeFormatter } from "../../lib/formatters";
import { channelFormatter } from "../../lib/formatters";
import Switch from "../../components/Switch";
import { data } from "../../lib/data";
import { useState, useEffect, useCallback } from "react";

export default function Events({objectColor, events, height }) {

    const [tests, setTests] = useState(true);
    

    const eventsWithoutTests = () => {
        return events.filter(event =>
            !data.some(testEvent => testEvent === event.code_mess))
    }

    const tableVisible = () => {
        if (!events.length) return false;
        if (tests && !eventsWithoutTests().length) return false;
        return true;
    }

    const tableData = () => {
        if (tests) return events;
        return eventsWithoutTests();
    }
  
    
    return (
        <div className="window-body-background-color window-body-text-color">
            <div className="height-30-px flex-row space-between align-items-center border-bottom window-header-background-color window-header-text-color">
                <div className="ml-10 font-weight-bold">События</div>
                <div className="flex-row align-items-center mr-10 g-10">
                    <div>Тестовые сообщения</div>
                    <Switch state={tests ? "enable" : "disable"} onClick={() => setTests(prev => !prev)} />
                </div>
            </div>
            <BootstrapTable
                // data={events.length ? events : []}
                data={tableVisible() ? tableData() : []}
                options={{ withoutNoDataText: true }}
                height={tableVisible() ? height : 20}   
                scrollTop={'Top'}   
                tableStyle={{ border: "none", padding: 0, margin: 0 }}
                containerStyle={{ padding: 0, margin: 0 }}
                headerStyle={{ height: "20px", padding: 0 }}>
                    
                <TableHeaderColumn
                    dataField="num_channel"
                    isKey={true}
                    dataAlign="center"
                    dataFormat={channelFormatter}
                    filterFormatted
                    width="80"
                    className="border-bottom"
                    thStyle={{ borderRight: "var(--border)" }}
                    style=""                    
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Канал</div>
                </TableHeaderColumn>
                <TableHeaderColumn dataField="time_sys"
                    dataAlign="center"
                    dataFormat={dateFormatterWithDotDelimeter}
                    filterFormatted
                    width="90"
                    className="border-bottom"
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Дата</div>
                </TableHeaderColumn>
                <TableHeaderColumn dataField="time_sys"
                    dataAlign="center"
                    dataFormat={timeFormatter}
                    filterFormatted
                    width="80"
                    className="border-bottom"
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Время</div>
                </TableHeaderColumn>
                <TableHeaderColumn dataField="num_obj"
                    dataAlign="center"                    
                    dataFormat={nomberNameFormat}
                    filterFormatted
                    width="74"
                    className="border-bottom"
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">№</div>
                </TableHeaderColumn>
                <TableHeaderColumn dataField="name_obj"
                    headerAlign="center"
                    dataFormat={nameFormat}
                    dataAlign="left"
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Объект</div>
                </TableHeaderColumn>
                <TableHeaderColumn
                    dataField="address_obj"
                    headerAlign="center"
                    dataFormat={adressNameFormat}
                    dataAlign="left"                    
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Адрес</div>
                </TableHeaderColumn>
                <TableHeaderColumn dataField="text_event"
                    headerAlign="center"
                    dataFormat={eventsNameFormat}
                    dataAlign="left"
                    className="border-bottom"                    
                    thStyle={{ borderRight: "var(--border)" }}
                    tdStyle={trClassNameFormat}>
                    <div className="bootstrap-table-operator-header">Событие</div>
                </TableHeaderColumn>                
            </BootstrapTable>
            {!tableVisible() ?
                <div className="flex-row align-items-center center border-top"
                    style={{ height: `${height - 20}px` }}>
                    Нет ни одного события
                </div> : null}
        </div>
    );
}
