import {useContext, useEffect, useState} from "react";
import SimpleList from "../../../components/simple-list/SimpleList";
import {getRequest, postRequest} from "../../../lib/connections/http/requests";
import {SocketServiceContext} from "../../../lib/connections/websocket/SocketServiceContext";

const configuration = {
    "backgroundColor": "white",
    "selectedColor": "#DDB9B8",
    "headerColor": "#cdd1da",
    "fontColor": "#3c3c3c",
    "fontColorSelected": "#838383"
};

const headers = {id: "id", title: "name"};

export default function SettingAlarmsOnTheServer(props) {
    const {subscribe, unsubscribe} = useContext(SocketServiceContext);
    const [currentClassId, setCurrentClassId] = useState('');
    const [currentTypeId, setCurrentTypeId] = useState('');
    const [classes, setClasses] = useState([]);
    const [types, setTypes] = useState([]);
    const [actions, setActions] = useState(undefined);
    const [cancels, setCancels] = useState(undefined);

    useEffect(() => {
        load();
        subscribe('/user/topic/UPDATE_ALARM_PROCESSING_LOG', load);
        return () => unsubscribe('/user/topic/UPDATE_ALARM_PROCESSING_LOG');
    }, []);

    const load = () => {
        getRequest('/api/alarms_event_classes/', classes => setClasses(classes));
    }





    const selectClass = classId => {
        postRequest('/api/alarms_event_types/', {classId}, types => {
            setCurrentClassId(classId);
            setTypes(types);
            setCurrentTypeId('0');
            setActions(undefined);
            setCancels(undefined);
        });
    }

    const selectType = typeId => {
        postRequest('/api/alarms_actions_by_type_id/', {typeId: typeId}, data => {
            setCurrentTypeId(typeId);
            setActions(data.actions);
            setCancels(data.cancels);
        });
    }

    const createAction = name => {
        let newAction = {typeId: currentTypeId, name, position: actions.length};
        postRequest('/api/alarms_add_action/', newAction, action => {
            setActions(prev => [...prev, action]);
        });
    }

    const createCancel = name => {
        let newAction = {typeId: currentTypeId, name, position: cancels.length};
        postRequest('/api/alarms_add_cancel/', newAction, action => {
            setCancels(prev => [...prev, action]);
        });
    }

    const renameAction = (id, name) => {
        postRequest('/api/alarms_rename_action/', {id, name});
        setActions(prev => {
            const index = prev.findIndex(action => action.id == id)
            prev[index] = {id: Number(id), name};
            return [...prev];
        });
    }

    const renameCancel = (id, name) => {
        postRequest('/api/alarms_rename_cancel/', {id, name});
        setCancels(prev => {
            const index = prev.findIndex(action => action.id == id);
            prev[index] = {id: Number(id), name};
            return [...prev];
        });
    }

    const upAction = id => {
        let newActions = [...actions];
        let index;
        let item;
        for (let i = 1; i < newActions.length; i++) {
            if (newActions[i].id == id) {
                index = i - 1;
                item = newActions[i];
                newActions.splice(i, 1);
                break;
            }
        }
        if (item !== undefined) {
            newActions.splice(index, 0, item);
            setActions(newActions);
        }
        if (index !== undefined) {
            let move = {typeId: currentTypeId, actionId: id, previousPosition: index + 1, currentPosition: index};
            postRequest('/api/alarms_move_action/', move);
        }
    }

    const downAction = id => {
        let newActions = [...actions];
        let index;
        let item;
        for (let i = 0; i < newActions.length - 1; i++) {
            if (newActions[i].id == id) {
                index = i + 1;
                item = newActions[i];
                newActions.splice(i, 1);
                break;
            }
        }
        if (item !== undefined) {
            newActions.splice(index, 0, item);
            setActions(newActions);
        }
        if (index !== undefined) {
            let move = {typeId: currentTypeId, actionId: id, previousPosition: index - 1, currentPosition: index};
            postRequest('/api/alarms_move_action/', move);
        }
    }

    const upCancel = id => {
        let newCancels = [...cancels];
        let index;
        let item;
        for (let i = 1; i < newCancels.length; i++) {
            if (newCancels[i].id == id) {
                index = i - 1;
                item = newCancels[i];
                newCancels.splice(i, 1);
                break;
            }
        }
        if (item !== undefined) {
            newCancels.splice(index, 0, item);
            setCancels(newCancels);
        }
        if (index !== undefined) {
            let move = {typeId: currentTypeId, actionId: id, previousPosition: index + 1, currentPosition: index};
            postRequest('/api/alarms_move_cancel/', move);
        }
    }

    const downCancel = id => {
        let newCancels = [...cancels];
        let index;
        let item;
        for (let i = 0; i < newCancels.length - 1; i++) {
            if (newCancels[i].id == id) {
                index = i + 1;
                item = newCancels[i];
                newCancels.splice(i, 1);
                break;
            }
        }
        if (item !== undefined) {
            newCancels.splice(index, 0, item);
            setCancels(newCancels);
        }
        if (index !== undefined) {
            let move = {typeId: currentTypeId, actionId: id, previousPosition: index - 1, currentPosition: index};
            postRequest('/api/alarms_move_cancel/', move);
        }
    }

    const deleteAction = actionId => {
        postRequest('/api/alarms_delete_action/', {actionId}, () => {
            setActions(prev => {
                const index = prev.findIndex(action => action.id == actionId);
                prev.splice(index, 1);
                return [...prev];
            })
        });
    }

    const deleteCancel = actionId => {
        postRequest('/api/alarms_delete_cancel/', {actionId}, () => {
            setCancels(prev => {
                const index = prev.findIndex(cancel => cancel.id == actionId);
                prev.splice(index, 1);
                return [...prev];
            })
        });
    }

    return (
        <div className="flex-column window-body-background-color window-body-text-color height-100 white border-box border">
            <div className="height-30-px flex-row space-between align-items-center window-header-background-color window-header-text-color border-bottom">
                <div className="ml-10 font-weight-bold">Действия по тревоге</div>
            </div>
            <div className="window-body-background-color flex-row space-between align-items-center" style={{minHeight: "320px",}}>
                <SimpleList id="0"
                            title="Классы событий"
                            headers={headers}
                            data={classes}
                            positions={false}
                            selectItem={selectClass}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Class">
                </SimpleList>
                <SimpleList id={currentClassId}
                            title="Типы событий"
                            headers={headers}
                            data={types}
                            positions={false}
                            selectItem={selectType}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Type">
                </SimpleList>
                <SimpleList id={currentTypeId}
                            title="Действия"
                            headers={headers}
                            data={actions}
                            positions={true}
                            createItem={createAction}
                            placeholder="Добавить действие"
                            up={upAction}
                            down={downAction}
                            changeItem={renameAction}
                            deleteItem={deleteAction}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Action">
                </SimpleList>
                <SimpleList id={currentTypeId}
                            title="Отмены"
                            headers={headers}
                            data={cancels}
                            positions={true}
                            createItem={createCancel}
                            placeholder="Добавить отмену"
                            up={upCancel}
                            down={downCancel}
                            changeItem={renameCancel}
                            deleteItem={deleteCancel}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Action">
                </SimpleList>
            </div>
        </div>
    );
}