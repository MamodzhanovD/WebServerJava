import {useEffect, useRef, useState} from 'react';
import MessageReceptionSettings from './form/MessageReceptionSettings';
import RemoteArm from './form/RemoteArm';
import Pcn from './form/Pcn';
import GsmTerminals from './form/GsmTerminals';
import UniversalArm from './form/UniversalArm';
import Archiving from './form/Archiving';
import EmailServerSettings from "./form/EmailServerSettings";
import EmailEvents from "./form/EmailEvents";
import SettingAlarmsOnTheServer from "./form/SettingAlarmsOnTheServer";
import SettingColorClassMesseg from "./form/SettingColorClassMesseg";
import AlarmProcessingLogRemoving from "./form/AlarmProcessingLogRemoving";
import SurGardMessages from "./form/SurGardMessages";
import ShutdownForm from "./form/ShutdownForm";
import ServerMode from "./form/ServerMode";
import {getRequest} from "../../lib/connections/http/requests";
import IncomingMessageFilter from "./form/IncomingMessageFilter";
import ProtonServers from "./form/ProtonServers";
import TestsBackend from './form/TestsBackend';
import UserFilters from './form/UserFilters';

export function SettingsContent(props) {
    const content = useRef();
    const [license, setLicense] = useState({});
    const [currentElements, setCurrentElements] = useState([]);

    useEffect(() => {
        getRequest('/api/license/', license => {
            console.log(license);
            setLicense(license);
        })
    }, []);

    useEffect(() => {
        currentVisibleElement();
        content.current.addEventListener('scroll', currentVisibleElement);
    }, [license]);

    const scrollIntoView = currentElement => {
        document.getElementById(currentElement).scrollIntoView();
    }

    function currentVisibleElement() {
        const scrollElements = [
            'testsBackend', 'shutdown', 'serverMode', 'messageReceptionSettings', 'incomingMessageFilter',
            'userFilters', 'remoteArm', 'universalArm', 'protonServers', 'emailServerSettings', 'emailEvents',
            'archive', 'settingAlarmsOnTheServer', 'alarmProcessingLogRemoving',
        ];
        if (license.gprs) scrollElements.push('surGardMessages');
        if (license.radio) scrollElements.push('pcn');
        if (license.gsm) scrollElements.push('gsmTerminals');
        let currentElements = [];
        for (let i = 0; i < scrollElements.length; i++) {
            if ((document.getElementById(scrollElements[i]).getBoundingClientRect().top >= content.current.getBoundingClientRect().top
                    && document.getElementById(scrollElements[i]).getBoundingClientRect().top < content.current.getBoundingClientRect().bottom)
                || (document.getElementById(scrollElements[i]).getBoundingClientRect().bottom > content.current.getBoundingClientRect().top
                    && document.getElementById(scrollElements[i]).getBoundingClientRect().bottom <= content.current.getBoundingClientRect().bottom)
                || (document.getElementById(scrollElements[i]).getBoundingClientRect().top <= content.current.getBoundingClientRect().top
                    && document.getElementById(scrollElements[i]).getBoundingClientRect().bottom >= content.current.getBoundingClientRect().bottom)) {
                currentElements.push(scrollElements[i]);
            }
        }
        setCurrentElements(currentElements);
    }

    return (
        <div className="flex-row body-height width-100 center g-10">
            <div className="flex-column mt-10">
                <div className="flex-row">
                    <div style={{ width: "3px" }}
                        className={currentElements.includes('testsBackend') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('testsBackend')}>
                        Тест бэка
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('serverMode') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('serverMode')}>
                        Режим работы сервера
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('messageReceptionSettings') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100"
                            onClick={() => scrollIntoView('messageReceptionSettings')}>
                        Приём сообщений
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('incomingMessageFilter') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('incomingMessageFilter')}>
                        Фильтр дублированных сообщений
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{ width: "3px" }}
                        className={currentElements.includes('userFilters') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('userFilters')}>
                        Фильтры событий
                    </button>
                </div>
                {license.gprs &&
                    <div className="flex-row">
                        <div style={{width: "3px"}}
                             className={currentElements.includes('surGardMessages') && 'menu-btn-active'}></div>
                        <button className="menu-btn width-100" onClick={() => scrollIntoView('surGardMessages')}>
                            Сообщения SurGard
                        </button>
                    </div>}
                {license.radio &&
                    <div className="flex-row">
                        <div style={{width: "3px"}}
                             className={currentElements.includes('pcn') && 'menu-btn-active'}></div>
                        <button className="menu-btn width-100" onClick={() => scrollIntoView('pcn')}>ПЦН</button>
                    </div>}
                {license.gsm &&
                    <div className="flex-row">
                        <div style={{width: "3px"}}
                             className={currentElements.includes('gsmTerminals') && 'menu-btn-active'}></div>
                        <button className="menu-btn width-100" onClick={() => scrollIntoView('gsmTerminals')}>
                            GSM терминалы
                        </button>
                    </div>}
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('remoteArm') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('remoteArm')}>
                        Удалённый АРМ
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('universalArm') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('universalArm')}>
                        Универсальный АРМ
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('protonServers') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('protonServers')}>
                        Серверы Протон
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('emailServerSettings') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('emailServerSettings')}>
                        Оповещения по электронной почте
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('emailEvents') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('emailEvents')}>
                        Фильтр электронной почты
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('archive') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('archive')}>
                        Архив сообщений
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('settingAlarmsOnTheServer') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100"
                            onClick={() => scrollIntoView('settingAlarmsOnTheServer')}>
                        Действия по тревоге
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('settingColorClassMesseg') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100"
                            onClick={() => scrollIntoView('settingColorClassMesseg')}>
                        Настройка цвета классов событий 
                    </button>
                </div>
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('alarmProcessingLogRemoving') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100"
                            onClick={() => scrollIntoView('alarmProcessingLogRemoving')}>
                        Журнал отработки тревог
                    </button>
                </div>                
                <div className="flex-row">
                    <div style={{width: "3px"}}
                         className={currentElements.includes('shutdown') && 'menu-btn-active'}></div>
                    <button className="menu-btn width-100" onClick={() => scrollIntoView('shutdown')}>
                        Выключение сервера
                    </button>
                </div>
            </div>

            <div ref={content} className="height-100 flex-column width-980-px overflow-y-auto overflow-x-hidden g-2 mt-2 pr-2"
                 style={{height: "calc(100% - 20px)"}}>
                <div id="testsBackend"><TestsBackend /></div>
                <div id="serverMode"><ServerMode /></div>
                <div id="messageReceptionSettings"><MessageReceptionSettings/></div>
                <div id="incomingMessageFilter"><IncomingMessageFilter /></div>
                <div id="userFilters"><UserFilters /></div>
                {license.gprs && <div id="surGardMessages"><SurGardMessages/></div>}
                {license.radio && <div id="pcn"><Pcn/></div>}
                {license.gsm && <div id="gsmTerminals"><GsmTerminals/></div>}
                <div id="remoteArm"><RemoteArm/></div>
                <div id="universalArm"><UniversalArm/></div>
                <div id="protonServers"><ProtonServers/></div>
                <div id="emailServerSettings"><EmailServerSettings/></div>
                <div id="emailEvents"><EmailEvents/></div>
                <div id="archive"><Archiving/></div>
                <div id="settingAlarmsOnTheServer"><SettingAlarmsOnTheServer/></div>
                <div id="settingColorClassMesseg"><SettingColorClassMesseg/></div>
                <div id="alarmProcessingLogRemoving"><AlarmProcessingLogRemoving/></div>
                <div id="shutdown"><ShutdownForm/></div>
            </div>
        </div>
    );
}

export default SettingsContent;