package Java;



import java.util.List;
import java.util.UUID;

import org.slf4j.LoggerFactory;

public class EventParsing {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EventParsing.class);
    private boolean calculateFlag;
    private boolean flag_lars = false;
    private Integer codeevent_lars = 0;
    private String fio = " ";
    
    

    public EventParsing(
            Integer codeevent, Integer object_addr, Integer system_addr,
            Integer numberLoopback, Integer channel, String name_operator,
            String event_text) {
        // Временно ставим по умолчанию тип охранного АРМ
        uuid_template_color = UUID.fromString(new ParameterAppPostgreSql().getColorEventTypesStart());
        int type_arm = ParameterAppPostgreSql.current_type_arm;
        switch (type_arm) {
            case ParameterAppPostgreSql.TYPE_ARM_FIRE: // Пожарный АРМ
                EventParsingFire(codeevent, object_addr, system_addr, numberLoopback, channel, name_operator,
                        event_text);
                break;
            case ParameterAppPostgreSql.TYPE_ARM_SECURITY: // Охранный АРМ
                EventParsingSecurity(codeevent, object_addr, system_addr, numberLoopback, channel, name_operator,
                        event_text);
                break;
        }
    }

    public EventParsing(
            Integer codeevent_lars, Integer codeevent, Integer object_addr,
            Integer system_addr, Integer numberLoopback, Integer channel,
            String name_operator, String event_text, boolean flag_lars) {
        // Временно ставим по умолчанию тип охранного АРМ
        Integer type_arm = ParameterAppPostgreSql.current_type_arm;
        this.flag_lars = flag_lars;
        this.codeevent_lars = codeevent_lars;
        switch (type_arm) {
            case ParameterAppPostgreSql.TYPE_ARM_SECURITY: // Охранный АРМ
                EventParsingSecurity(codeevent, object_addr, system_addr, numberLoopback, channel, name_operator,
                        event_text);
                break;
        }
    }

    public void EventParsingSecurity(
            Integer codeevent, Integer object_addr, Integer system_addr,
            Integer numberLoopback, Integer channel, String name_operator,
            String event_text) {
        try {
            // Собственные события старой ПЦН

           

            // Определяется зона
            Zone zone;
            int flag_event = 0;// Флаг сообщения
            int fire_object = 1;// Флаг пожарного мониторинга
            int flag_journal_alarm = 0;// Флаг тревог или неисправностей
            boolean flag_update_state = true;// Флаг на изменение state object
            // Создаётся информационное сообщение для отправки на фронт
            InfoOperatorWeb infoOperatorWeb;
            // Объект для отправки
            InfoOperatorQueue infoOperatorQueue;
            // Ответственное лицо
            ResponsiblePersoneWeb responsiblePersoneWeb;
            calculateFlag = true;
            
            // Событие отправляемое на фронт
            JournalEventsAndAlarm journalEvents = new JournalEventsAndAlarm();
            journalEvents.setNum_syst(system_addr);
            // Настройки фильтрации сообщений для объекта
            ObjectDescriptionTemp objectDescriptionTemp = new ParameterAppPostgreSql()
                    .selectObject_description_temp_object_addr_system_addr(object_addr, system_addr);
            if (objectDescriptionTemp == null) {
                return;
            }
            String numobj = objectDescriptionTemp.getNumobj();
            journalEvents.setNum_obj(numobj);
            String nameobj = new ParameterAppPostgreSql().selectObject_s_temp_numobj_nameobj(numobj);
            journalEvents.setName_obj(nameobj);
            journalEvents.setNum_channel(channel);
            long id_template = objectDescriptionTemp.getId_template();
            
            Templatecodeevent templatecodeevent = new ParameterAppPostgreSql().selectTemplatecodeevent(id_template, codeevent);
            //Получаем класс события для вывода цвета в ленте событий пожарного мониторинга
            Integer get_class_event = templatecodeevent.getId_class_event();
            String selectCodeEventColor_code_mess = new ParameterAppPostgreSql()
                    .selectCodeEventColor(uuid_template_color, get_class_event);
            journalEvents.setEvent_class_color(selectCodeEventColor_code_mess);
            if (flag_lars) {
                templatecodeevent.setUser_event_text(event_text);
                event_text = "";
            }
            Integer serch_codeevent = templatecodeevent.getUser_codeevent();
            journalEvents.setClass_mess(templatecodeevent.getId_class_event());
            journalEvents.setType_mess(templatecodeevent.getId_type_events());

            
            journalEvents.setCode_mess(serch_codeevent);
            journalEvents.setTime_sys(OwnTimestamp.getNowTimestamp());

            // Можно определить в конструкторе
            journalEvents = new WriteJournalEvents().writeJournalEvents(journalEvents);
            Integer new_codeevent;
            // ищем пользовательский код, класс, тип
            if (templatecodeevent.getCodeevent() == null) {
                new_codeevent = codeevent;
            } else {
                new_codeevent = templatecodeevent.getCodeevent();
            }

            // Рассылка пользователям по email
            emailSender(new_codeevent, numobj);

            GeneralWeb generalWeb = new GeneralWeb();
            generalWeb.setNumobj(numobj);
            generalWeb = new ParameterAppPostgreSql().selectAddress_numobj(generalWeb);
            String address_object = generalWeb.getTown_name() + " " + generalWeb.getRegion_name() + " "
                    + generalWeb.getStreet_name() + " " + generalWeb.getNumber_building() + ", "
                    + generalWeb.getNumber_flat();
            journalEvents.setAddress_obj(address_object);
            
            if (flag_journal_alarm == 0 && codeevent < 3000) {
                // Снимаем тревогу в таблице journal_alarm
                int codeevent_cancel = codeevent;
                switch (codeevent_cancel) {
                    case 1326:
                    case 1335:
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj, 302,
                                numberLoopback);
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj, 311,
                                numberLoopback);
                        break;
                    default:
                        if (codeevent > 1024) {
                            codeevent_cancel = codeevent - 1024;
                        } else {
                            codeevent_cancel = codeevent + 1024;
                        }
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj,
                                codeevent_cancel, numberLoopback);
                        break;
                }
            }
            if (flag_journal_alarm == 0 && codeevent > 4000) {
                // Снимаем тревогу в таблице journal_alarm
                Integer codeevent_cancel = codeevent - 4000;
                new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj, codeevent_cancel,
                        numberLoopback);
            }
            if (calculateFlag) {
                if (flag_journal_alarm == 1) {
                    // Если присутствует шлейф то смотрим его id и url
                    if (journalEvents.getIltered() != 0 && journalEvents.getIltered() != null) {
                        Zone zone_temp = new ParameterAppPostgreSql().selectZone(journalEvents.getIltered(),
                                journalEvents.getNum_obj());
                        if (zone_temp.getVideo_camera_id() != 0) {
                            journalEvents.setVideo_camera_id(zone_temp.getVideo_camera_id());
                            journalEvents.setVideo_camera_url(zone_temp.getVideo_camera_url());
                        }
                    }

                    journalEvents.setState_object(flag_event);
                    journalEvents.setEvent_handling(0);
                    if (journalEvents.getState_devise() == null) {
                        journalEvents.setState_devise(0);
                    }

                    // В некоторых событиях необходимо сохранить параметр
                    if (new_codeevent == 143) {
                        journalEvents.setIltered(numberLoopback);// адрес устройства
                    }
                    journalEvents = new ParameterAppPostgreSql().insertJournal_alarm(journalEvents);

                    // Отправляем тревогу на frontend
                    infoOperatorWeb = new InfoOperatorWeb();
                    infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateJournalEvents);
                    infoOperatorWeb.setDestination("/topic/updateJournalEvents");
                    infoOperatorWeb.setJournalAlarm(new InfoJournalAlarm(numobj, journalEvents));

                    infoOperatorQueue = new InfoOperatorQueue();
                    infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
                    infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);

                    ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

                    if (journalEvents.getState_object() == 3 || journalEvents.getState_object() == 1) {
                        AlarmsEvent alarmsEvent = new ParameterAppPostgreSql()
                                .setEventClassAndType(new AlarmsEvent(journalEvents));
                        new ParameterAppPostgreSql().addAlarmInAlarmProcessingLogAlarms(alarmsEvent);
                        AlarmProcessingLogQueue.addAndNotifyQueue(alarmsEvent);
                    }
                    // Отправка тревоги в журнал отработки тревог
                    // Если для данного объекта включена отработка тревог
                    // Преобразование объекта JournalEventsAndAlarm в объект журнала отработки
                    // тревог

                }

               

                // Пересчет состояния объекта
                List<StatusZone> listStatusZone = new ParameterAppPostgreSql().selectStatus_zone(numobj);
                StatusDevice statusDevice = new ParameterAppPostgreSql().selectStatus_device(numobj);
                // -----------------------------------------
                // Если объект полностью снят то инициализируем статусы объекта

                int count_zone = listStatusZone.size();
                int count_set = 0;
                int flag_set_unset = 0;// снят
                for (int i = 0; i < listStatusZone.size(); i++) {
                    StatusZone statusZone = listStatusZone.get(i);
                    if (statusZone.getStatus_zone() == 1 && statusZone.getDay_night() != 1) {
                        count_set++;
                    }
                    if (statusZone.getDay_night() == 1) {
                        count_zone--;
                    }
                }
                if (count_set == 0) {
                    flag_set_unset = 0;
                } else if (count_set == count_zone) {
                    flag_set_unset = 1;
                    new ParameterAppPostgreSql().updateStatus_object_set_status_object(numobj, flag_set_unset);
                } else {
                    flag_set_unset = 2;
                    new ParameterAppPostgreSql().updateStatus_object_set_status_object(numobj, flag_set_unset);
                }
                if (flag_set_unset == 0) {
                    new ParameterAppPostgreSql().updateStatus_object_unset_full(numobj);
                    if (flag_update_state) {
                        new ParameterAppPostgreSql().updateStatus_object_state_object(0, numobj);
                    }
                }

                // -------------------------------------------------

                StatusObject statusObject = new ParameterAppPostgreSql().selectStatus_object(numobj);

                CalculateObjectSecurity calculateObjectnew = new CalculateObjectSecurity();
                statusObject = calculateObjectnew.getCalculateObject(statusObject, statusDevice, listStatusZone);
                new ParameterAppPostgreSql().updateStatus_object_flag(statusObject, numobj);

                
                // Отправка изменения по объекту на frontend (flag)
                Integer general_flag_object = statusObject.getGeneral_flag_object();

                infoOperatorWeb = new InfoOperatorWeb();
                infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateeStateObject);
                infoOperatorWeb.setDestination("/topic/updateStateObject");
                infoOperatorWeb.setStateObject(new InfoStatusObject(numobj, general_flag_object, flag_set_unset,
                        statusObject.getTimestamp_status_object()));


                infoOperatorQueue = new InfoOperatorQueue();
                infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
                infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);
                ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);


            }

            if (journalEvents.getText_event() != null) {
                journalEvents.setText_event(journalEvents.getText_event() + event_text);
            } else if (event_text != null && !event_text.isEmpty()) {
                journalEvents.setText_event(event_text);
            }

            long id = new ParameterAppPostgreSql().insertJournalEvents(journalEvents);
            journalEvents.setId_journal_events(id);
            FilterServiceQueue.addAndNotifyQueue(journalEvents.getEvent());

        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString());
        }
    }

    

    public void EventParsingFire(Integer codeevent, Integer object_addr, Integer system_addr, Integer numberLoopback,
            Integer channel, String name_operator, String event_text) {
        try {

            if (channel == ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_3 && object_addr == 65536) {
                EventParsingOldPcn(codeevent, object_addr, system_addr, numberLoopback, channel, name_operator, 1);
                Command_out Command_out = new Command_out();
                if (codeevent == 977) {
                    FireIndication fireIndication = new ParameterAppPostgreSql().selectFire_indication(ParameterAppPostgreSql.INDICATOR_STATE);
                    if (fireIndication.getState() == ParameterAppPostgreSql.INDICATOR_OFF) {
                        new ParameterAppPostgreSql().updateFireIndication(ParameterAppPostgreSql.INDICATOR_STATE, ParameterAppPostgreSql.INDICATOR_ON);
                        Command_out.sendCommandOn("100000", ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_3, 4);
                    }
                }
                if (codeevent == 2001) {
                    FireIndication fireIndication = new ParameterAppPostgreSql().selectFire_indication(ParameterAppPostgreSql.INDICATOR_STATE);
                    if (fireIndication.getState() == ParameterAppPostgreSql.INDICATOR_ON) {
                        new ParameterAppPostgreSql().updateFireIndication(ParameterAppPostgreSql.INDICATOR_STATE, ParameterAppPostgreSql.INDICATOR_OFF);
                    }
                    Command_out.sendCommandOff("100000", ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_3, 4);
                }
                return;
            }

            Zone zone;
            int flag_event = 0;// Флаг сообщения
            int fire_object = 1;// Флаг пожарного мониторинга
            int flag_journal_alarm = 0;// Флаг тревог или неисправностей
            boolean flag_update_state = true;// Флаг на изменение state object
            InfoOperatorWeb infoOperatorWeb;
            InfoOperatorQueue infoOperatorQueue;
            calculateFlag = true;
            JournalEventsAndAlarm journalEvents = new JournalEventsAndAlarm();
            journalEvents.setNum_syst(system_addr);
            ObjectDescriptionTemp objectDescriptionTemp = new ParameterAppPostgreSql()
                    .selectObject_description_temp_object_addr_system_addr(object_addr, system_addr);
            if (objectDescriptionTemp == null) {
                return;
            }
            String numobj = objectDescriptionTemp.getNumobj();
            journalEvents.setNum_obj(numobj);
            String nameobj = new ParameterAppPostgreSql().selectObject_s_temp_numobj_nameobj(numobj);
            journalEvents.setName_obj(nameobj);
            journalEvents.setNum_channel(channel);
            long id_template = objectDescriptionTemp.getId_template();
            Templatecodeevent templatecodeevent = new ParameterAppPostgreSql().selectTemplatecodeevent(id_template,
                    codeevent);
            Integer serch_codeevent = templatecodeevent.getUser_codeevent();
            journalEvents.setClass_mess(templatecodeevent.getId_class_event());
            journalEvents.setType_mess(templatecodeevent.getId_type_events());

            if (!name_operator.isEmpty()) {
                journalEvents
                        .setText_event(templatecodeevent.getUser_event_text() + " (Оператор: " + name_operator + ")");
            } else {
                journalEvents.setText_event(templatecodeevent.getUser_event_text());
            }
            journalEvents.setCode_mess(serch_codeevent);
            journalEvents.setTime_sys(OwnTimestamp.getNowTimestamp());

            journalEvents = new WriteJournalEvents().writeJournalEvents(journalEvents);
            Integer new_codeevent;
            // ищем пользовательский код, класс, тип
            if (templatecodeevent.getCodeevent() == null) {
                new_codeevent = codeevent;
            } else {
                new_codeevent = templatecodeevent.getCodeevent();
            }

            // Рассылка пользователям по email
            emailSender(new_codeevent, numobj);

            GeneralWeb generalWeb = new GeneralWeb();
            generalWeb.setNumobj(numobj);
            generalWeb = new ParameterAppPostgreSql().selectAddress_numobj(generalWeb);
            String address_object = generalWeb.getTown_name() + " " + generalWeb.getRegion_name() + " "
                    + generalWeb.getStreet_name() + " " + generalWeb.getNumber_building() + ", "
                    + generalWeb.getNumber_flat();
            journalEvents.setAddress_obj(address_object);

            
            if (flag_journal_alarm == 0 && codeevent < 3000) {
                // Снимаем тревогу в таблице journal_alarm
                int codeevent_cancel = codeevent;
                switch (codeevent_cancel) {
                    case 1326:
                    case 1335:
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj, 302,
                                numberLoopback);
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj, 311,
                                numberLoopback);
                        break;
                    default:
                        if (codeevent > 1024) {
                            codeevent_cancel = codeevent - 1024;
                        } else {
                            codeevent_cancel = codeevent + 1024;
                        }
                        new ParameterAppPostgreSql().updateJournal_alarm_event_handling_codevent(numobj,
                                codeevent_cancel, numberLoopback);
                        break;
                }
            }
            if (calculateFlag) {
                if (flag_journal_alarm == 1) {
                    journalEvents.setState_object(flag_event);
                    journalEvents.setEvent_handling(0);
                    if (journalEvents.getState_devise() == null) {
                        journalEvents.setState_devise(0);
                    }

                    // В некоторых событиях необходимо сохранить параметр
                    if (new_codeevent == 143) {
                        journalEvents.setIltered(numberLoopback);// адрес устройства
                    }
                    journalEvents = new ParameterAppPostgreSql().insertJournal_alarm(journalEvents);

                    // Отправляем тревогу на frontend

                    infoOperatorWeb = new InfoOperatorWeb();
                    infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateJournalAlarm);
                    infoOperatorWeb.setDestination("/topic/updateJournalAlarm");
                    infoOperatorWeb.setJournalAlarm(new InfoJournalAlarm(numobj, journalEvents));

                    infoOperatorQueue = new InfoOperatorQueue();
                    infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
                    infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);

                    ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

                    if (journalEvents.getState_object() == 3 || journalEvents.getState_object() == 1) {
                        AlarmsEvent alarmsEvent = new ParameterAppPostgreSql()
                                .setEventClassAndType(new AlarmsEvent(journalEvents));
                        new ParameterAppPostgreSql().addAlarmInAlarmProcessingLogAlarms(alarmsEvent);
                        AlarmProcessingLogQueue.addAndNotifyQueue(alarmsEvent);
                    }
                }
                
                // Направляем состояния пожарных индикаторов
                StateIndicatorFire stateIndicatorFire = new ParameterAppPostgreSql().selectStateIndicatorFire("");
                Command_out Command_out = new Command_out();
                if (stateIndicatorFire.getFault() == 1) {
                    FireIndication fireIndication = new ParameterAppPostgreSql()
                            .selectFire_indication(ParameterAppPostgreSql.INDICATOR_FAULT);
                    if (fireIndication.getState() == ParameterAppPostgreSql.INDICATOR_OFF) {
                        new ParameterAppPostgreSql().updateFireIndication(ParameterAppPostgreSql.INDICATOR_FAULT,
                                ParameterAppPostgreSql.INDICATOR_ON);
                        Command_out.sendCommandOn("100000", ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_3, 2);
                    }
                } else {
                    FireIndication fireIndication = new ParameterAppPostgreSql()
                            .selectFire_indication(ParameterAppPostgreSql.INDICATOR_FAULT);
                    if (fireIndication.getState() == ParameterAppPostgreSql.INDICATOR_ON) {
                        new ParameterAppPostgreSql().updateFireIndication(ParameterAppPostgreSql.INDICATOR_FAULT,
                                ParameterAppPostgreSql.INDICATOR_OFF);
                    }
                    Command_out.sendCommandOff("100000", ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_3, 2);
                }

                infoOperatorWeb = new InfoOperatorWeb();
                infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateStateIndicatorFire);
                infoOperatorWeb.setDestination("/topic/updateStateIndicatorFire");
                infoOperatorWeb.setStateIndicatorFire(new InfoStateIndicatorFire(numobj, stateIndicatorFire));

                infoOperatorQueue = new InfoOperatorQueue();
                infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
                infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);

                ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);
                // }

                // Обновляем состояние флага flag_state_test_dialup
                StateIndicatorFire stateIndicatorFire_numobj = new ParameterAppPostgreSql()
                        .selectStateIndicatorFire(numobj);
                numberLoopback = 0;
                if (stateIndicatorFire_numobj.getStart() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(1, numobj, numberLoopback,
                            new_codeevent);
                } else if (stateIndicatorFire_numobj.getStart_delay() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(2, numobj, numberLoopback,
                            new_codeevent);
                } else if (stateIndicatorFire_numobj.getStop() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(3, numobj, numberLoopback,
                            new_codeevent);
                } else if (stateIndicatorFire_numobj.getStart_lock() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(4, numobj, numberLoopback,
                            new_codeevent);
                } else if (stateIndicatorFire_numobj.getAutomatics_disabled() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(5, numobj, numberLoopback,
                            new_codeevent);
                } else if (stateIndicatorFire_numobj.getFault() == 1) {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(7, numobj, numberLoopback,
                            new_codeevent);
                } else {
                    new ParameterAppPostgreSql().updateStatus_device_flag_state_test_dialup(0, numobj, numberLoopback,
                            new_codeevent);
                }
                
                // Пересчет состояния объекта
                List<StatusZone> listStatusZone = new ParameterAppPostgreSql().selectStatus_zone(numobj);
                StatusDevice statusDevice = new ParameterAppPostgreSql().selectStatus_device(numobj);
                // Если объект полностью снят то инициализируем статусы объекта
                int count_zone = listStatusZone.size();
                int count_set = 0;
                int flag_set_unset = 0;// снят
                for (int i = 0; i < listStatusZone.size(); i++) {
                    StatusZone statusZone = listStatusZone.get(i);
                    if (statusZone.getStatus_zone() == 1 && statusZone.getDay_night() != 1) {
                        count_set++;
                    }
                    if (statusZone.getDay_night() == 1) {
                        count_zone--;
                    }
                }
                if (count_set == 0) {
                    flag_set_unset = 0;
                } else if (count_set == count_zone) {
                    flag_set_unset = 1;
                    new ParameterAppPostgreSql().updateStatus_object_set_status_object(numobj, flag_set_unset);
                } else {
                    flag_set_unset = 2;
                    new ParameterAppPostgreSql().updateStatus_object_set_status_object(numobj, flag_set_unset);
                }
                if (flag_set_unset == 0) {
                    new ParameterAppPostgreSql().updateStatus_object_unset_full(numobj);
                    if (flag_update_state) {
                        new ParameterAppPostgreSql().updateStatus_object_state_object(0, numobj);
                    }
                }
                // -------------------------------------------------

                StatusObject statusObject = new ParameterAppPostgreSql().selectStatus_object(numobj);

                CalculateObjectFire calculateObjectnew = new CalculateObjectFire();
                statusObject = calculateObjectnew.getCalculateObject(statusObject, statusDevice, listStatusZone);
                new ParameterAppPostgreSql().updateStatus_object_flag(statusObject, numobj);

                // Отправка изменения по объекту на frontend (flag)
                Integer general_flag_object = statusObject.getGeneral_flag_object();

                infoOperatorWeb = new InfoOperatorWeb();
                infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateeStateObject);
                infoOperatorWeb.setDestination("/topic/updateStateObject");
                infoOperatorWeb.setStateObject(new InfoStatusObject(numobj, general_flag_object, flag_set_unset,
                        statusObject.getTimestamp_status_object()));

                infoOperatorQueue = new InfoOperatorQueue();
                infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
                infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);
                ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

            }

            if (journalEvents.getText_event() != null) {
                journalEvents.setText_event(journalEvents.getText_event() + event_text);
            } else if (event_text != null && !event_text.isEmpty()) {
                journalEvents.setText_event(event_text);
            }
            new ParameterAppPostgreSql().insertJournal_events(journalEvents);
            
            Integer get_class_event = templatecodeevent.getId_class_event();
            String selectCodeEventColor_code_mess = new ParameterAppPostgreSql()
                    .selectCodeEventColor(uuid_template_color, get_class_event);
            journalEvents.setEvent_class_color(selectCodeEventColor_code_mess);
            FilterServiceQueue.addAndNotifyQueue(journalEvents.getEvent());
        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString());
        }
    }

    private void setName_obj(Integer numberLoopback, String numobj, JournalEventsAndAlarm journalEvents) {
        Zone zone = new ParameterAppPostgreSql().selectZone(numberLoopback, numobj);
        if (zone.getName_zone() != null) {
            journalEvents.setText_event(
                    journalEvents.getText_event() + " №" + numberLoopback + " (" + zone.getName_zone() + ")");
        } else {
        }
    }

    // Рассылка пользователям по email
    private void emailSender(Integer new_codeevent, String numobj) {
        // Находим текст соответствующий указанному коду событий и проверяем, что он
        // включен на отправку
        Integer active = 1;
        TemplatecodeeventEmail templatecodeeventEmail = new ParameterAppPostgreSql()
                .selectTemplatecodeevent_email_user_codeevent(new_codeevent, active);
        if (templatecodeeventEmail != null) {
            // Находим ответственных лиц привязанных к объекту у которых установленн флаг
            // отправки по mail
            List<ResponsiblePersoneWeb> listResponsiblePersoneWeb = new ParameterAppPostgreSql()
                    .selectResponsiblePersoneWeb_numobj(numobj);
            listResponsiblePersoneWeb.stream().filter(item -> item.getEmail_send() == 1).forEach(item -> {
                String timeDate = new OwnTimestamp().getUTCTimestampDATE_TIME_FORMATTER_3String();
                String emailSubject = "Сообщение от объекта (Время сервера " + timeDate + ")";
                String emailMsg = templatecodeeventEmail.getUser_event_text();
                new EmailSender().emailSender(item.getEmail(), emailSubject, emailMsg);
            });
            // Производим отправку
        }
    }

    // Собственные события
    public EventParsing(int codeevent, Long parameter, StateIndicatorMonitoring stateIndicatorMonitoring) {
        try {

            InfoOperatorWeb infoOperatorWeb;
            InfoOperatorQueue infoOperatorQueue;
            //992
            long id_template = 2;// Для охранного по умолчанию 2 шаблон
            Templatecodeevent templatecodeevent = new ParameterAppPostgreSql().selectTemplatecodeevent(id_template, codeevent);
            Integer serch_codeevent = templatecodeevent.getUser_codeevent();

            JournalEventsAndAlarm journalEvents = new JournalEventsAndAlarm();
            journalEvents.setNum_obj("WebServer");
            journalEvents.setNum_syst(0);
            journalEvents.setName_obj("WebServer");
            journalEvents.setNum_channel(ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_4);
            journalEvents.setClass_mess(templatecodeevent.getId_class_event());
            journalEvents.setType_mess(templatecodeevent.getId_type_events());
            journalEvents.setText_event(templatecodeevent.getUser_event_text());
            journalEvents.setCode_mess(serch_codeevent);
            journalEvents.setTime_sys(OwnTimestamp.getNowTimestamp());
            journalEvents = new WriteJournalEvents().writeJournalEvents(journalEvents);

            Integer new_codeevent;
            // ищем пользовательский код, класс, тип
            if (templatecodeevent.getCodeevent() == null) {
                new_codeevent = codeevent;
            } else {
                new_codeevent = templatecodeevent.getCodeevent();
            }
            // Направляем состояние индикатора мониторинга каналов PCN GSM терминалов
            infoOperatorWeb = new InfoOperatorWeb();
            infoOperatorWeb.setNameDestination(InfoOperatorWeb.updateStateIndicatorMonitoring);
            infoOperatorWeb.setDestination("/topic/updateStateIndicatorMonitoring");
            infoOperatorWeb.setStateIndicatorMonitoring(stateIndicatorMonitoring);
            infoOperatorQueue = new InfoOperatorQueue();
            infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
            infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);
            ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);

            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListOperatorsAndAdmins(),
                    new String[]{"pageOperator", "pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_PCN, WebSocketTopic.UPDATE_GSM_TERMINALS});

            new ParameterAppPostgreSql().insertJournal_events(journalEvents);
            FilterServiceQueue.addAndNotifyQueue(journalEvents.getEvent());
            infoOperatorQueue = new InfoOperatorQueue();
            infoOperatorQueue.setNameClassStorageQueue(infoOperatorQueue.NameInfoOperatorWeb);
            infoOperatorQueue.setInfoOperatorWeb(infoOperatorWeb);
            ManagerOperatorQueue.addAndNotifyQueue(infoOperatorQueue);
        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString());
        }
    }

    // Собственные события Старой ПЦН
    public void EventParsingOldPcn(Integer codeevent, Integer object_addr, Integer system_addr, Integer numberLoopback,
            Integer channel, String name_operator, long id_template) {
        try {

            InfoOperatorWeb infoOperatorWeb;
            InfoOperatorQueue infoOperatorQueue;

            Templatecodeevent templatecodeevent = new ParameterAppPostgreSql().selectTemplatecodeevent(id_template,
                    codeevent);
            Integer serch_codeevent = templatecodeevent.getUser_codeevent();

            JournalEventsAndAlarm journalEvents = new JournalEventsAndAlarm();
            journalEvents.setNum_obj("PCN");
            journalEvents.setNum_syst(system_addr);
            journalEvents.setName_obj("PCN");
            journalEvents.setNum_channel(ParamVariables.JOURNAL_MESSAGE_CHANNEL_SEND_4);
            journalEvents.setClass_mess(templatecodeevent.getId_class_event());
            journalEvents.setType_mess(templatecodeevent.getId_type_events());
            journalEvents.setText_event(templatecodeevent.getUser_event_text());
            journalEvents.setCode_mess(serch_codeevent);
            journalEvents.setTime_sys(OwnTimestamp.getNowTimestamp());
            journalEvents = new WriteJournalEvents().writeJournalEvents(journalEvents);

            Integer new_codeevent;
            // ищем пользовательский код, класс, тип
            if (templatecodeevent.getCodeevent() == null) {
                new_codeevent = codeevent;
            } else {
                new_codeevent = templatecodeevent.getCodeevent();
            }

            new ParameterAppPostgreSql().insertJournal_events(journalEvents);
            FilterServiceQueue.addAndNotifyQueue(journalEvents.getEvent());
        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString());
        }
    }

    // Статус выходов
    String getStatusOutput(int status) {
        // При этом: 00 – отсутствует, 01 – включен(выполняется программа), 02 –
        // выключен, 03 – выход неисправен
        switch (status) {
            case 0:
                return "отсутствует";
            case 1:
                return "включен(выполняется программа";
            case 2:
                return "выключен";
            case 3:
                return "выход неисправен";
        }
        return "";
    }
}
