package Java;



import java.nio.ByteBuffer;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OperatorRestController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user_name/", method = RequestMethod.GET)
    public ResponseEntity<UserName> getUserNameByLogin(Principal principal) {
        String userName = new ParameterAppPostgreSql().getUserDescriptionByLogin(principal.getName());
        return new ResponseEntity<>(new UserName(userName), HttpStatus.OK);
    }

    @RequestMapping(value = "/stateIndicatorMonitoringPcn/", method = RequestMethod.GET)
    public ResponseEntity<List<StateIndicatorMonitoring>> infoStateIndicatorMonitoringtPcn() {
        List<StateIndicatorMonitoring> listStateIndicatorMonitoring = new ParameterAppPostgreSql().selectControl_pcn();
        return new ResponseEntity<>(listStateIndicatorMonitoring, HttpStatus.OK);
    }

    @RequestMapping(value = "/stateIndicatorMonitoringGsmTerminal/", method = RequestMethod.GET)
    public ResponseEntity<List<StateIndicatorMonitoring>> infoStateIndicatorMonitoringtGsmTerminal() {
        List<StateIndicatorMonitoring> listStateIndicatorMonitoring = new ParameterAppPostgreSql().selectControl_gsm_terminal();
        return new ResponseEntity<>(listStateIndicatorMonitoring, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/status_object/", method = RequestMethod.GET)
    public ResponseEntity<List<InfoStatusObjectName>> infoStatusObjectName(Principal principal) {
        String login = principal.getName();
        List<InfoStatusObjectName> listInfoStatusObjectName = new ParameterAppPostgreSql()
                .selectStatus_objectInfoStatusObjectName(login);
        if (!listInfoStatusObjectName.isEmpty()) {
            return new ResponseEntity<>(listInfoStatusObjectName, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/journal_events/", method = RequestMethod.GET)
    public ResponseEntity<List<JournalEventsAndAlarm>> infoJournalEvents(Principal principal) {
        String login = principal.getName();
        Integer limit = 50;
        List<JournalEventsAndAlarm> listJournalEvents = new ParameterAppPostgreSql().selectJournalEventsLogin(login,
                limit);
        return new ResponseEntity<>(listJournalEvents, HttpStatus.OK);
    }

    
    @RequestMapping(value = "/journal_events_last_day/", method = RequestMethod.GET)
    public ResponseEntity<List<ShortEvent>> eventsLastDay(Principal principal) {
        List<ShortEvent> listJournalEvents = userService.getEventsLastDay(principal.getName());
        return new ResponseEntity<>(listJournalEvents, HttpStatus.OK);
    }

    @RequestMapping(value = "/journal_alarm_alarm/", method = RequestMethod.GET)
    public ResponseEntity<List<JournalEventsAndAlarm>> infoJournalAlarmAlarm(Principal principal) {
        String login = principal.getName();
        List<JournalEventsAndAlarm> listJournalEvents = new ParameterAppPostgreSql()
                .selectJournalAlarmAlarmLogin(login);
        return new ResponseEntity<>(listJournalEvents, HttpStatus.OK);
    }

    @RequestMapping(value = "/journal_alarm_fail/", method = RequestMethod.GET)
    public ResponseEntity<List<JournalEventsAndAlarm>> infoJournalAlarmFail(Principal principal) {
        String login = principal.getName();
        List<JournalEventsAndAlarm> listJournalEvents = new ParameterAppPostgreSql().selectJournalAlarmFailLogin(login);
        return new ResponseEntity<>(listJournalEvents, HttpStatus.OK);
    }

    @RequestMapping(value = "/journal_alarm/", method = RequestMethod.PUT)
    public ResponseEntity<?> updateManagerObject(@RequestBody JournalEventsAndAlarm journalEventsAndAlarm,
                                                 Principal principal) {
        User result = new ParameterAppPostgreSql().selectUsers_login(principal.getName());
        String name;
        if (result.getDescriptions() != null) {
            if (!result.getDescriptions().isEmpty()) {
                name = result.getDescriptions();
            } else {
                name = principal.getName();
            }
        } else {
            name = principal.getName();
        }
        if (new ParameterAppPostgreSql().updateJournal_alarm_event_handling(journalEventsAndAlarm)) {
            
            ObjectDescriptionTemp objectDescriptionTemp = new ParameterAppPostgreSql()
                    .selectObject_description_numobj(journalEventsAndAlarm.getNum_obj());
            Integer number_object = objectDescriptionTemp.getObject_addr();
            int number_system = objectDescriptionTemp.getSystem_addr();
            if (journalEventsAndAlarm.getEvent_handling() == 2) {
                Integer code_mess = journalEventsAndAlarm.getCode_mess();
                switch (code_mess) {
                    case 121:
                        code_mess = code_mess + 4000;
                        break;
                    case 421:
                        code_mess = code_mess + 4000;
                        break;
                    case 1245:
                        code_mess = code_mess - 1024;
                        break;
                    default:
                        code_mess = code_mess + 1024;
                        break;
                }
                new EventParsing(code_mess, number_object, number_system, journalEventsAndAlarm.getIltered(),
                        ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_4, name, "");
            } 

            InfoOperatorWeb infoOperatorWeb = new InfoOperatorWeb();
            infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateJournalEvents);
            infoOperatorWeb.setDestination("/topic/updateJournalEvents");
            infoOperatorWeb
                    .setJournalEvents(new InfoJournalEvents(journalEventsAndAlarm.getNum_obj(), journalEventsAndAlarm.getEvent()));
            InfoOperatorQueue infoOperatorQueue = new InfoOperatorQueue();
            infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
            infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);
            ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String[] userCommands = new String[]{
            "",
            "Включить выход прибора",
            "Отключить выход прибора",
            "Запросить тест канала связи",
            "Взять прибор под охрану по типу",
            "Снять прибор с охраны по типу",
            "Взять шлейф под охрану",
            "Снять шлейф с охраны",
            "Уровень сигнала в радиоканале",
            "Состояние питания",
            "Состояние шлейфа",
            "Состояние выхода прибора",
            "Качество сигнала в радиоканале",
            "Уровень шума в радиоканале",
            "Мощность передатчика",
            "Уровень сигнала GSM",
            "Емкость аккумулятора",
            "Состояние объекта",
            "Состояние баланса SIM"
    };

    private String commandParameters(Integer commandNumber, Integer parameter) {
        switch (commandNumber) {
            case 1:
            case 2:
            case 4:
            case 5:
            case 6:
            case 7:
            case 10:
            case 11:
                return " с параметром " + parameter;
            default:
                return "";
        }
    }

    

    private long insertJournal_message_send(Integer id, String identifier, ByteBuffer send_package, int flag,
                                            int channel_send, Integer codecommand, Integer object_addr, Integer system_addr, Long timelive) {
        long current_timestamp_control = OwnTimestamp.getNowTimestamp() + 3600000;// Смещение на один час вперед
        return new ParameterAppPostgreSql().insertJournal_message_send_id_identifier_package_message_flag(
                id, identifier, send_package.array(), flag, channel_send, codecommand, object_addr, system_addr,
                current_timestamp_control);
    }

    @RequestMapping(value = "/indicator_fire/", method = RequestMethod.GET)
    public ResponseEntity<StateIndicatorFire> infoJournalIndicatorFire(Authentication authentication) {
        StateIndicatorFire stateIndicatorFire = new ParameterAppPostgreSql().selectStateIndicatorFire(authentication.getName());
        if (stateIndicatorFire != null) {
            return new ResponseEntity<>(stateIndicatorFire, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/object_info/{numobj}", method = RequestMethod.GET)
    public ResponseEntity<CardObject> objectInfo(@PathVariable("numobj") String numobj, Principal principal) {
        CardObject cardObject = new CardObject();
        String nameobj = new ParameterAppPostgreSql().selectObject_s_temp_numobj_nameobj(numobj);
        cardObject.setNumobj(numobj);
        cardObject.setNameobj(nameobj);
        StatusObject statusObject = new ParameterAppPostgreSql().selectStatus_object(numobj);
        cardObject.setStatus_object(statusObject.getStatus_object());
        cardObject.setTimestamp_status_object(statusObject.getTimestamp_status_object());
        StatusDevice statusDevice = new ParameterAppPostgreSql().selectStatus_device(numobj);
        cardObject.setFlag_state_object(statusDevice.getFlag_state_object()); // Прибор
        cardObject.setFlag_state_tamper(statusDevice.getFlag_state_tamper()); // Корпус
        cardObject.setFlag_state_network(statusDevice.getFlag_state_network()); // Сеть
        cardObject.setFlag_state_akb(statusDevice.getFlag_state_akb());// АКБ
        cardObject.setFlag_state_test_radio(statusDevice.getFlag_state_test_radio());// Тест радиоканала
        cardObject.setFlag_state_test_gprs(statusDevice.getFlag_state_test_gprs());// Тест Ethernet
        cardObject.setFlag_indicator(statusDevice.getFlag_state_test_dialup());// Флаг индикаторов
        OwnerWeb ownerWeb = new OwnerWeb();
        ownerWeb = new ParameterAppPostgreSql().selectOwner_numobj(ownerWeb, numobj);
        ownerWeb = new ParameterAppPostgreSql().selectAddressOwner_numobj(ownerWeb);
        cardObject.setFio(ownerWeb.getFio());// Фио собственника
        cardObject.setMobule_phone(ownerWeb.getMobile_phone());// Телефон собственника
        cardObject.setMobule_phone2(ownerWeb.getMobile_phone2());// Телефон собственника
        String address_owner = ownerWeb.getTown_name() + " " + ownerWeb.getRegion_name() + " "
                + ownerWeb.getStreet_name() + " " + ownerWeb.getNumber_building() + ", " + ownerWeb.getNumber_flat();
        cardObject.setAddress_owner(address_owner);// Адрес собственника

        GeneralWeb generalWeb = new GeneralWeb();
        generalWeb.setNumobj(numobj);
        generalWeb = new ParameterAppPostgreSql().selectAddress_numobj(generalWeb);
        String address_object = generalWeb.getTown_name() + " " + generalWeb.getRegion_name() + " "
                + generalWeb.getStreet_name() + " " + generalWeb.getNumber_building() + ", "
                + generalWeb.getNumber_flat();
        cardObject.setAddress_object(address_object);

        List<StatusZone> listStatusZone = new ParameterAppPostgreSql().selectStatus_zone(numobj);
        List<WebZone> list_zone = new ArrayList<>();
        for (int i = 0; i < listStatusZone.size(); i++) {
            WebZone webZone = new WebZone();
            webZone.setNum_zone(listStatusZone.get(i).getNumzone());
            Zone zone = new ParameterAppPostgreSql().selectZone(listStatusZone.get(i).getNumzone(), numobj);
            webZone.setName_zone(zone.getName_zone());
            webZone.setDescription_zone(zone.getDescription_zone());
            webZone.setState_zone(listStatusZone.get(i).getState_zone());
            webZone.setStatus_zone(listStatusZone.get(i).getStatus_zone());
            list_zone.add(webZone);
        }
        cardObject.setList_zone(list_zone);
        return new ResponseEntity<>(cardObject, HttpStatus.OK);
    }

    // Удалить
    @RequestMapping(value = "/select_cardobject/{numobj}", method = RequestMethod.GET)
    public ResponseEntity<CardObject> cardObject(@PathVariable("numobj") String numobj, Principal principal) {
        String name = principal.getName();
        InfoOperatorWeb infoOperatorWeb = new InfoOperatorWeb();
        infoOperatorWeb.setNameDestination(InfoOperatorWeb.selectСardObject);
        infoOperatorWeb.setNameUser(name);
        infoOperatorWeb.setDestination("/topic/selectСardObject");
        ParameterAppPostgreSql.queueHandlerOperator.addTaskToProcess(infoOperatorWeb, numobj);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private final String[] objectStates = {"Присутствует", "Отсутствует", "Неисправен"};
    private final String[] tamperStates = {"В норме", "Вскрыт"};
    private final String[] akbStates = {"В норме", "Разряжен", "Авария или отсутствует"};
    private final String[] connectionStates = {"В норме", "Отсутствует"};

    private String getChangedParameter(String user, String objectNumber, CardObject currentState, CardObject newState) {
        String message = "";
        if (!Objects.equals(currentState.getFlag_state_object(), newState.getFlag_state_object())) {
            message = "Пользователь " + user + " изменил состояние прибора на объекте №" + objectNumber +
                    " c " + objectStates[currentState.getFlag_state_object()] + " на "
                    + objectStates[newState.getFlag_state_object()];
        } else if (!Objects.equals(currentState.getFlag_state_tamper(), newState.getFlag_state_tamper())) {
            message = "Пользователь " + user + " изменил состояние корпуса прибора на объекте №" + objectNumber +
                    " c " + tamperStates[currentState.getFlag_state_tamper()] + " на "
                    + tamperStates[newState.getFlag_state_tamper()];
        } else if (!Objects.equals(currentState.getFlag_state_akb(), newState.getFlag_state_akb())) {
            message = "Пользователь " + user + " изменил состояние АКБ прибора на объекте №" + objectNumber +
                    " c " + akbStates[currentState.getFlag_state_akb()] + " на "
                    + akbStates[newState.getFlag_state_akb()];
        } else if (!Objects.equals(currentState.getFlag_state_network(), newState.getFlag_state_network())) {
            message = "Пользователь " + user + " изменил состояние сети на объекте №" + objectNumber +
                    " c " + connectionStates[currentState.getFlag_state_network()] + " на "
                    + connectionStates[newState.getFlag_state_network()];
        } else if (!Objects.equals(currentState.getFlag_state_test_radio(), newState.getFlag_state_test_radio())) {
            message = "Пользователь " + user + " изменил состояние радиоканала на объекте №" + objectNumber +
                    " c " + connectionStates[currentState.getFlag_state_test_radio()] + " на "
                    + connectionStates[newState.getFlag_state_test_radio()];
        } else if (!Objects.equals(currentState.getFlag_state_test_gprs(), newState.getFlag_state_test_gprs())) {
            message = "Пользователь " + user + " изменил состояние GPRS/Ethernet на объекте №" + objectNumber +
                    " c " + connectionStates[currentState.getFlag_state_test_gprs()] + " на "
                    + connectionStates[newState.getFlag_state_test_gprs()];
        }
        return message;
    }

    @RequestMapping(value = "/select_cardobject/", method = RequestMethod.PUT)
    public ResponseEntity<?> updatCardobject(@RequestBody CardObject cardObject, Authentication authResult) {
        String objectNumber = cardObject.getNumobj();
        CardObject currentCardObject = new ParameterAppPostgreSql().getCurrentStateObject(objectNumber);
        if (new ParameterAppPostgreSql().updateCardobject(cardObject)) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    getChangedParameter(user, objectNumber, currentCardObject, cardObject),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListOperators(),
                    new String[]{"pageOperator"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_OBJECT_STATE});
            // Пересчет состояний объекта

            List<StatusZone> listStatusZone = new ParameterAppPostgreSql().selectStatus_zone(objectNumber);
            StatusDevice statusDevice = new ParameterAppPostgreSql().selectStatus_device(objectNumber);
            StatusObject statusObject = new ParameterAppPostgreSql().selectStatus_object(objectNumber);

            CalculateObjectFire calculateObjectnew = new CalculateObjectFire();
            statusObject = calculateObjectnew.getCalculateObject(statusObject, statusDevice, listStatusZone);
            new ParameterAppPostgreSql().updateStatus_object_flag(statusObject, objectNumber);

            // Отправка изменения по объекту на frontend (flag)
            Integer general_flag_object = statusObject.getGeneral_flag_object();
            Integer status_object = statusObject.getStatus_object();
            Long timestamp_status_object = statusObject.getTimestamp_status_object();
            InfoOperatorWeb infoOperatorWeb = new InfoOperatorWeb();
            infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateeStateObject);
            infoOperatorWeb.setDestination("/topic/updateStateObject");
            infoOperatorWeb.setStateObject(
                    new InfoStatusObject(objectNumber, general_flag_object, status_object, timestamp_status_object));

            InfoOperatorQueue infoOperatorQueue = new InfoOperatorQueue();
            infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
            infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);

            ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/select_cardobject_status_object/", method = RequestMethod.PUT)
    public ResponseEntity<?> updatCardobjectStatusObject(@RequestBody CardObject cardObject, Principal principal) {
        String user = principal.getName();
        User result = new ParameterAppPostgreSql().selectUsers_login(user);
        String name;
        if (!result.getDescriptions().isEmpty()) {
            name = result.getDescriptions();
        } else {
            name = user;
        }
        // Пересчет состояний объекта
        String numobj = cardObject.getNumobj();
        Integer status_object = cardObject.getStatus_object();
        ObjectDescriptionTemp objectDescriptionTemp = new ParameterAppPostgreSql()
                .selectObject_description_numobj(numobj);
        Integer object_addr = objectDescriptionTemp.getObject_addr();
        Integer system_addr = objectDescriptionTemp.getSystem_addr();
        if (status_object == 0) {// Снят
            new EventParsing(482, object_addr, system_addr, 1, ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_4, name, "");
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + name + " снял с охраны объект №" + numobj, principal.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageOperator"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_OBJECT_STATE});
        }
        if (status_object == 1 || status_object == 2) {// Взят
            new EventParsing(1506, object_addr, system_addr, 1, ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_4, name,
                    "");
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + name + " взял под охрану объект №" + numobj, principal.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageOperator"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_OBJECT_STATE});
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
