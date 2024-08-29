import { useContext, useEffect, useState } from 'react';
import { connect } from 'react-redux';
import Events from './Events';
import BlockIndicatorForm from './BlockIndicatorForm';
import { getRequest, } from "../../lib/connections/http/requests";
import { SERVER_URL } from "../../lib/connections/url";
import DynamicPagination from "./DynamicPagination";
import { alarm, alarmStop, fail, failStop, lostConnection, notification, reconnect, take, takeOff } from "./sounds/reducer/SoundPlayerActions";
import Info from "./info/Info";
import { openAlarm } from "./tabs/TabsActions";
import useResizableWindows from "../../lib/useResizableWindows";
import { acceptAlarmHandler, completeAlarmHandler } from "./OperatorService";
import { SocketServiceContext } from "../../lib/connections/websocket/SocketServiceContext";
import useNotificationService from "./NotificationService";
import { NotificationServiceContext } from "./NotificationServiceContext";


function OperatorContent(props) {
    const {
        content, moveX, stopResize, leftDivWidth, leftFirstDivHeight, alarmsHeight, failsHeight, leftSecondDivHeight,
        rightDivWidth, startResizeYLeft, startResizeXmet, serverMode, loadServerMode
    } = useResizableWindows();
    const { subscribe, unsubscribe, callAfterReconnect, dontCallAfterReconnect } = useContext(SocketServiceContext);
    const { countNotifications, notifications, newMessage, markAsRead, markAllAsRead } = useNotificationService();

    const [objectsState, setObjectsState] = useState([]);
    const [events, setEvents] = useState([]);
    const [unacceptedAlarms, setUnacceptedAlarms] = useState(false);
    const [alarms, setAlarms] = useState([]);
    useEffect(() => {
        let unacceptedAlarms = false;
        for (let i = 0; i < alarms.length; i++) {
            if (alarms[i].event_handling === 0) {
                unacceptedAlarms = true;
                break;
            }
        }
        setUnacceptedAlarms(unacceptedAlarms);
    }, [alarms]);

    const [fails, setFails] = useState([]);

    const acceptAlarm = id => {
        let index = alarms.findIndex(alarm => alarm.id_alarm === id);
        if (index !== -1) {
            acceptAlarmHandler(alarms[index]);
        } else {
            index = fails.findIndex(alarm => alarm.id_alarm === id);
            if (index !== -1) acceptAlarmHandler(fails[index]);
        }
    }

    const completeAlarm = id => {
        let index = alarms.findIndex(alarm => alarm.id_alarm === id);
        if (index !== -1) {
            completeAlarmHandler(alarms[index]);
        } else {
            index = fails.findIndex(alarm => alarm.id_alarm === id);
            if (index !== -1) completeAlarmHandler(fails[index]);
        }
    }

    const loadObjectsState = () => {
        getRequest(SERVER_URL + '/api/status_object/', objectsState => setObjectsState(prev => objectsState));
    }

    useEffect(() => {
        loadServerMode();
        loadObjectsState();
        loadEvents();
        getRequest(SERVER_URL + '/api/journal_alarm_alarm/', alarms => {
            setAlarms(prev => alarms);
        });
        getRequest(SERVER_URL + '/api/journal_alarm_fail/', fails => {
            setFails(prev => fails);
        });

        subscribe('/user/topic/updateStateObject', updateStateObject);
        subscribe('/user/topic/EVENT', defineEvent);
        subscribe('/user/topic/NOTIFICATION', newNotification);
        callAfterReconnect('OperatorContent', updateAfterReconnect);
        return () => {
            unsubscribe('/user/topic/updateStateObject');
            unsubscribe('/user/topic/EVENT');
            unsubscribe('/user/topic/NOTIFICATION');
            dontCallAfterReconnect('OperatorContent');
        }
    }, []);

    const loadEvents = () => {
        getRequest(SERVER_URL + '/api/journal_events_last_day/', events => {
            setEvents(prev => events);            
        });
    }

    const updateAfterReconnect = () => {
        getRequest(SERVER_URL + '/api/stateIndicatorMonitoringPcn/', props.onSetPcnIndicatorsAfterReconnect);
        getRequest(SERVER_URL + '/api/stateIndicatorMonitoringGsmTerminal/', props.onSetGsmTerminalIndicatorsAfterReconnect);
    }

    //Приходящие уведомления
    const newNotification = message => {
        newMessage(message);
        props.notification();
    }

    const updateStateObject = objectState => {
        setObjectsState(prev => {
            prev[prev.findIndex(object => object.numobj === objectState.numobj)] = objectState;
            return prev;
        });
    }

    const defineEvent = message => {
        const event = JSON.parse(message.body);
        switch (event.state_object) {
            case 1:
            case 3://Тревога
                switch (event.event_handling) {
                    case 0:// Новая
                        setAlarms(prev => [event, ...prev]);
                        setEvents(prev => [event, ...prev]);
                        props.alarm();
                        break;
                    case 1:// Принята
                        setAlarms(prev => {
                            const i = prev.findIndex(alarm => alarm.id_alarm === event.id_alarm);
                            if (i !== -1) prev[i].event_handling = 1;
                            return [...prev];
                        });
                        props.alarmStop();
                        break;
                    case 2:// Завершена
                        setAlarms(prev => {
                            const j = prev.findIndex(alarm => alarm.id_alarm === event.id_alarm);
                            if (j !== -1) prev.splice(j, 1);
                            return [...prev];
                        });
                        break;
                    default:
                        break;
                }
                break;
            case 5://Неисправность
                switch (event.event_handling) {
                    case 0://Новая
                        setFails(prev => [event, ...prev]);
                        setEvents(prev => [event, ...prev]);
                        props.fail();
                        break;
                    case 1://Принята
                        setFails(prev => {
                            const i = prev.findIndex(fail => fail.id_alarm === event.id_alarm);
                            if (i !== -1) prev[i].event_handling = 1;
                            return [...prev];
                        });
                        break;
                    case 2://Завершена
                        setFails(prev => {
                            const j = prev.findIndex(fail => fail.id_alarm === event.id_alarm);
                            if (j !== -1) prev.splice(j, 1);
                            return [...prev];
                        });
                        break;
                    default:
                        break;
                }
                break;
            default:
                setEvents(prev => [event, ...prev]);
        }
        loadObjectsState();
    }

    return (
        <NotificationServiceContext.Provider value={{ countNotifications, notifications, markAsRead, markAllAsRead }}>
            <div className="flex-column height-100 width-100 overflow-y-auto window-body-background-color">
                {/*Состояние связи с ПЦН и GSM терминалами*/}
                {/*Нужны подключенные для тестирования*/}
                {serverMode === 1 && <BlockIndicatorForm />}
                <div ref={content} className="flex-row" style={{ padding: "3px", height: serverMode === 1 ? "calc(100% - 43px)" : "100%" }}>
                    <div style={{ display: "flex", flexDirection: "column", height: "100%", width: "100%" }} onMouseMove={moveX} onMouseUp={stopResize}>
                        <div style={{ display: "flex", height: leftFirstDivHeight, width: "100%" }}>
                            <div className="border" style={{ display: "flex", width: leftDivWidth, height: leftFirstDivHeight }}>
                                <Info events={events}
                                    alarms={alarms}
                                    unacceptedAlarms={unacceptedAlarms}
                                    fails={fails}
                                    height={leftFirstDivHeight - 30}
                                    failsHeight={failsHeight - 15}
                                    alarmsHeight={alarmsHeight - 14}
                                    width={leftDivWidth}
                                    acceptAlarm={acceptAlarm}
                                    completeAlarm={completeAlarm}
                                    leftDivWidth={leftDivWidth}
                                    // tabs={tabs}
                                    // closeTab={id => delete tabs[id]}
                                    // showTab={id => setIndex(id)}
                                    loadObjectsState={loadObjectsState} />
                            </div>

                            <div style={{ width: "3px", height: "100%", backgroundColor: "transparent", cursor: "col-resize" }} onMouseDown={startResizeXmet}></div>

                            <div className="border" style={{ display: "flex", flexDirection: "column", width: rightDivWidth, height: leftFirstDivHeight }}>
                                <DynamicPagination items={objectsState}
                                    title="Состояние объектов"
                                    itemMinWidth={40}
                                    itemMinHeight={25}
                                    timeout={5000}
                                    itemName="nameobj"
                                    itemKey="numobj"
                                    height={leftFirstDivHeight - 32} />
                            </div>
                        </div>

                        <div style={{ height: "3px", backgroundColor: "transparent", cursor: "row-resize" }} onMouseDown={startResizeYLeft}></div>

                        <div className="border" style={{ height: leftSecondDivHeight, width: "100%" }}>
                            <Events events={events} height={leftSecondDivHeight - 32} />
                        </div>
                    </div>
                </div>
            </div>
        </NotificationServiceContext.Provider>
    )
}

const mapDispatchToProps = dispatch => {
    return {
        alarm: () => dispatch(alarm()),
        alarmStop: () => dispatch(alarmStop()),
        fail: () => dispatch(fail()),
        failStop: () => dispatch(failStop()),
        lostConnection: () => dispatch(lostConnection()),
        reconnect: () => dispatch(reconnect()),
        notification: () => dispatch(notification()),
        take: () => dispatch(take()),
        takeOff: () => dispatch(takeOff()),
        openAlarm: (objectNumber, alarmId) => dispatch(openAlarm(objectNumber, alarmId))
    };
};

export default connect(null, mapDispatchToProps)(OperatorContent);

