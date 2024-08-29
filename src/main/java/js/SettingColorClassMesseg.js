import {useContext, useEffect, useState} from "react";
import SimpleList from "../../../components/simple-list/SimpleList";
import {getRequest, postRequest} from "../../../lib/connections/http/requests";
import {SocketServiceContext} from "../../../lib/connections/websocket/SocketServiceContext";

import Switch from "../../../components/Switch";
import {SketchPicker} from 'react-color';

const configuration = {
    "backgroundColor": "white",
    "selectedColor": "#DDB9B8",
    "headerColor": "#cdd1da",
    "fontColor": "#3c3c3c",
    "fontColorSelected": "#838383"
};

const headers = {id: "id", title: "name"};

//отоброжение формы выбора цвета
const pickerStyles = {
        default: {
            picker: {
                width: "225px",
                height: "420px",
                padding: "0",
                borderRadius: "0",
                backgroundColor: "#eeeeee"
            }
        }
    };
    

export default function SettingColorClassMessegr(props) {
    const {subscribe, unsubscribe} = useContext(SocketServiceContext);
    const [currentClassId, setCurrentClassId] = useState('');
    const [currentTypeId, setCurrentTypeId] = useState('');
    const [classes, setClasses] = useState([]);
    const [types, setTypes] = useState([]);
    const [colors, setColors] = useState('');
    const [actions, setActions] = useState(undefined);
    const [cancels, setCancels] = useState(undefined);
    const [currentColor, setCurrentColor] = useState('');
    const [selectClassMess, setSelectClassMess] = useState('');
    const [selectTemplateColor, setSelectTemplateColor] = useState('');
    const [commandGroupValue, setCommandGroupValue] = useState("1");
    const [enabled, setEnabled] = useState(false);
    

   //Выбор цвета на панеле цветов 
    const handleOnChange = (color) => {        
        setCurrentColor(color.hex)
    } 
    
    
    

    useEffect(() => {
        load();
        loadSelectTemplateColor();
        subscribe('/user/topic/UPDATE_ALARM_PROCESSING_LOG', load);
        return () => unsubscribe('/user/topic/UPDATE_ALARM_PROCESSING_LOG');
    }, []);

    //Загрузка названий шаблонов
    const load = () => {
        getRequest('/api/alarms_color_event_name_classes/', types => setTypes(types));
    }
    
    //Загрузка названий шаблонов в select
    const loadSelectTemplateColor = () => {
        getRequest('/api/select_color_event_name_classes/', types => setSelectTemplateColor(types));
    }



    //Обновить цвет по нажатию кнопки
    const updateColor = () => {
        const sendData = {
            id: selectClassMess,
            id_template_color: currentClassId,
            color: currentColor
        };  
        postRequest('/api/update_template_color_name/', sendData, classes => {
            setClasses(classes);
        }); 
    }
    
    //Загрузка цветов выбранного шаблона
    const selectClassName = classId => {
        postRequest('/api/template_color_name/', {classId}, classes => {
            setCurrentClassId(classId);
            setClasses(classes);
        });
                
    }
    
    //Загрузка выбранного цвета на форму цветов
    const selectClassColor = classId => {
        postRequest('/api/template_class_color/', {classId},  colors => {
            setSelectClassMess(classId);
            setCurrentColor( colors);
        });
    }


    
    //Созание шаблона цветов
    const createAction = name => {
        let newAction = {typeId: currentTypeId, name,  position: types.length + 1};
        postRequest('/api/add_template/', newAction,  types => {
            setTypes(prev => [...prev, types]);
        });
        
    }
    
    const stateButtonFormatter = () => {
        return <Switch  checked={enabled} onChange={(e)=> setEnabled(e.target.checked)}/>
    }
    
    //Переименовать шаблон цветов
    const renameAction = (id, name) => {
        if (id > 2) {
            postRequest('/api/rename_color_template/', {id, name});
            setTypes(prev => {
                const index = prev.findIndex(action => action.id == id)
                prev[index] = {id: Number(id), name};
                return [...prev];
            });
	}        
    }
    
    //Удалить шаблон цветов
    const deleteAction = actionId => {        
        if (actionId > 2) {
		postRequest('/api/delete_color_template/', {actionId}, () => {
                setTypes(prev => {
                    const index = prev.findIndex(action => action.id == actionId);
                    prev.splice(index, 1);
                    return [...prev];
                })
            });
	}            
    }
    


//Обновить цвет по нажатию кнопки
    const updateColor2 = event => {
        const sendData2 = {            
            id_template_color: event.target.value,           
        };  
        setSelectTemplateColor(event.target.value);
        postRequest('/api/update_status_template_color_name/', sendData2, selectTemplateColor => {
            
        }); 
    }
   

    return (
        <div className="flex-column window-body-background-color window-body-text-color height-100 white border-box border">
            <div className="height-30-px flex-row space-between align-items-center window-header-background-color window-header-text-color border-bottom">
                <div className="ml-10 font-weight-bold">Настройка цвета классов событий</div>
                                <div className="flex-row align-items-center g-10">
                    <label style={{width: "100px"}}>Шаблон</label>
                    <select className="proton-input" name="template"
                            style={{width: "210px", textAlign: "center"}}
                            value={selectTemplateColor} 
                            onChange={updateColor2}>
                        {types.map(types => <option value={types.id}>{types.name}</option>)}
                    </select>
                </div>
            </div>
            <div className="window-body-background-color flex-row space-between align-items-center" style={{minHeight: "320px",}}>                             
                <SimpleList id="0"
                            title="Имя шаблона"
                            headers={headers}
                            data={types}
                            positions={false}                            
                            createItem={createAction}
                            changeItem={renameAction}
                            deleteItem={deleteAction}
                            selectItem={selectClassName}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Type">
                </SimpleList>
                <SimpleList id={currentClassId}
                            title="Классы событий"
                            headers={headers}
                            data={classes}
                            positions={false}
                            selectItem={selectClassColor}
                            width="225px"
                            height="450px"
                            border={true}
                            borderRadius="0px"
                            configuration={configuration}
                            entity="Class">
                </SimpleList>
                <div lassName="window-body-background-color flex-row space-between align-items-center" style={{minHeight: "450px" ,}}>
                    <SketchPicker
                                styles={pickerStyles}
                                color={currentColor}
                                onChangeComplete={handleOnChange}
                    />
                    <div className="flex-row">
                        <div style={{width: "3px"}}></div>
                        <button className="menu-btn width-100"  onClick={updateColor}>
                            Изменить цвет
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}