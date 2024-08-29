package Java;



import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class AlarmProcessingLogRestController {
    /**
     * Отработка тревог. Классы и типы событий. Действия по тревоге.
     * Наполнение списков действий и отмен тревог.
     */

    @RequestMapping(value = "/alarm_processing_log/", method = RequestMethod.POST)
    public ResponseEntity<List<AlarmsEvent>> getAlarmProcessingLog(@RequestBody ObjectNumberDto dto, Principal principal) {
        List<AlarmsEvent> alarmEvents = new ParameterAppPostgreSql().selectAlarmsInAlarmProcessingLogAlarms(dto.getObjectNumber(), principal.getName());
        return new ResponseEntity<>(alarmEvents, HttpStatus.OK);
    }


    @RequestMapping(value = "/alarms_event_classes/", method = RequestMethod.GET)
    public ResponseEntity<List<AlarmsEventClass>> getEventClasses() {
        List<AlarmsEventClass> classes = new ParameterAppPostgreSql().getEventClasses();
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    @RequestMapping(value = "/alarms_event_types/", method = RequestMethod.POST)
    public ResponseEntity<List<AlarmsEventType>> getEventTypesByClassId(@RequestBody ClassIdDto dto) {
        List<AlarmsEventType> types = new ParameterAppPostgreSql().getEventTypes(dto.getClassId());
        return new ResponseEntity<>(types, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/alarms_color_event_classes/", method = RequestMethod.GET)
    public ResponseEntity<List<AlarmsEventColor>> getColorEventClasses() {
        List<AlarmsEventColor> classes = new ParameterAppPostgreSql().getColorEventClasses();
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/alarms_color_event_name_classes/", method = RequestMethod.GET)
    public ResponseEntity<List<AlarmsEventNameColor>> getColorEventNameClasses() {
        List<AlarmsEventNameColor> classes = new ParameterAppPostgreSql().getColorEventNameClasses();
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/select_color_event_name_classes/", method = RequestMethod.GET)
    public ResponseEntity<Boolean> getSelectColorEventNameClasses() {
        Integer types = new ParameterAppPostgreSql().getSelectColorEventNameClasses();
        return new ResponseEntity(types, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/template_class_color/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> getColorEventTypes(@RequestBody ClassIdDto dto) {
         String types = new ParameterAppPostgreSql().getColorEventTypes(dto.getClassId());
        return new ResponseEntity(types, HttpStatus.OK);
    }
        
    
    @RequestMapping(value = "/template_color_name/", method = RequestMethod.POST)
    public ResponseEntity<List<AlarmsEventColorSelect>> getColorEventTypesClass(@RequestBody ClassIdDto dto) {
        List<AlarmsEventColorSelect> types = new ParameterAppPostgreSql().getColorEventTypesClass(dto.getClassId());
        return new ResponseEntity(types, HttpStatus.OK);
    }
    
   
    @RequestMapping(value = "/alarms_actions_by_type_id/", method = RequestMethod.POST)
    public ResponseEntity<AlarmActionsAndCancels> getActionsByTypeId(@RequestBody TypeIdDto dto) {
        AlarmActionsAndCancels actions = new ParameterAppPostgreSql().getActionsAndCancelsByTypeId(dto.getTypeId());
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @RequestMapping(value = "/alarms_actions_and_cancels_by_object_number_and_event_type_id/", method = RequestMethod.POST)
    public ResponseEntity<ObjectActions> getActionsByObjectNumber(@RequestBody GetObjectActionsDto dto) {
        ObjectActions actions = new ParameterAppPostgreSql().getActionsAndCancelsByTypeIdAndObjectNumber(dto.getObjectNumber(), dto.getTypeId());
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @RequestMapping(value = "/get_list_actions_and_cancels_by_type_id_and_object_number/", method = RequestMethod.POST)
    public ResponseEntity<AlarmActionsAndCancels> getListActionsAndCancelsByTypeIdAndObjectNumber(@RequestBody GetObjectActionsDto dto) {
        AlarmActionsAndCancels actions = new ParameterAppPostgreSql().getListActionsAndCancelsByTypeIdAndObjectNumber(dto.getObjectNumber(), dto.getTypeId());
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }


    @RequestMapping(value = "/alarms_add_action/", method = RequestMethod.POST)
    public ResponseEntity<AlarmsAction> newAction(@RequestBody AddActionDto dto, Authentication authResult) {
        AlarmsAction action = new ParameterAppPostgreSql().addAction(dto.getTypeId(), dto.getName(), dto.getPosition());
        String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
        new ParameterAppPostgreSql().writeToSystemLog(
                "Пользователь " + user + " для типа тревоги ID: " + dto.getTypeId() + " добавил действие ID: " + action.getId() + " - " + action.getName(),
                authResult.getName());
        WebSocketService.sendUpdate(
                new ParameterAppPostgreSql().getListAdmins(),
                new String[]{"pageGateway"},
                new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
        return new ResponseEntity<>(action, HttpStatus.OK);
    }
    
    //Для добовления шаблона цвета
    @RequestMapping(value = "/add_template/", method = RequestMethod.POST)
    public ResponseEntity<AddColorTemplate> newTemplate(@RequestBody AddActionDto dto, Authentication authResult) {
        AddColorTemplate action = new ParameterAppPostgreSql().addTemplate( dto.getTypeId(), dto.getName(), dto.getPosition());
        String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
        new ParameterAppPostgreSql().writeToSystemLog(
                "Пользователь " + user + " для типа тревоги ID: " + dto.getTypeId() + " добавил действие ID: " + action.getId() + " - " + action.getName(),
                authResult.getName());
        WebSocketService.sendUpdate(
                new ParameterAppPostgreSql().getListAdmins(),
                new String[]{"pageGateway"},
                new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
        return new ResponseEntity<>(action, HttpStatus.OK);
    }
    

    @RequestMapping(value = "/alarms_add_cancel/", method = RequestMethod.POST)
    public ResponseEntity<AlarmsAction> newCancel(@RequestBody AddActionDto dto, Authentication authResult) {
        AlarmsAction action = new ParameterAppPostgreSql().addCancel(dto.getTypeId(), dto.getName(), dto.getPosition());
        String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
        new ParameterAppPostgreSql().writeToSystemLog(
                "Пользователь " + user + " для типа тревоги ID: " + dto.getTypeId() + " добавил отмену ID: " + action.getId() + " - " + action.getName(),
                authResult.getName());
        WebSocketService.sendUpdate(
                new ParameterAppPostgreSql().getListAdmins(),
                new String[]{"pageGateway"},
                new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
        return new ResponseEntity<>(action, HttpStatus.OK);
    }

    @RequestMapping(value = "/alarms_rename_action/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> renameAction(@RequestBody RenameActionDto dto, Authentication authResult) {
        if (new ParameterAppPostgreSql().renameAction(dto.getId(), dto.getName())) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + user + " переименовал действие ID: " + dto.getId() + " - " + dto.getName(),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
    
    @RequestMapping(value = "/alarms_rename_cancel/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> renameCancel(@RequestBody RenameActionDto dto, Authentication authResult) {
        if (new ParameterAppPostgreSql().renameCancel(dto.getId(), dto.getName())) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + user + " переименовал отмену ID: " + dto.getId() + " - " + dto.getName(),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/rename_color_template/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> renameColorTemplate(@RequestBody RenameActionDto dto, Authentication authResult) {
        if (new ParameterAppPostgreSql().renameColorTemplate(dto.getId(), dto.getName())) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + user + " переименовал отмену ID: " + dto.getId() + " - " + dto.getName(),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }   

   
    @RequestMapping(value = "/delete_color_template/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> deleteColorTemplate(@RequestBody ActionIdDto dto, Authentication authResult) {
        new ParameterAppPostgreSql().deleteColorTemplate(dto.getActionId());
        if (new ParameterAppPostgreSql().deleteColorTemplate(dto.getActionId())) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + user + " удалил действие ID: " + dto.getActionId(),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/alarms_delete_cancel/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> deleteCancel(@RequestBody ActionIdDto dto, Authentication authResult) {
        if (new ParameterAppPostgreSql().deleteCancel(dto.getActionId())) {
            String user = new ParameterAppPostgreSql().getUserDescriptionByLogin(authResult.getName());
            new ParameterAppPostgreSql().writeToSystemLog(
                    "Пользователь " + user + " удалил отмену ID: " + dto.getActionId(),
                    authResult.getName());
            WebSocketService.sendUpdate(
                    new ParameterAppPostgreSql().getListAdmins(),
                    new String[]{"pageGateway"},
                    new WebSocketTopic[]{WebSocketTopic.UPDATE_ALARM_PROCESSING_LOG});
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Настройка - Отработка тревог
     */

    @RequestMapping(value = "/alarm_processing_log_setting/", method = RequestMethod.POST)
    public ResponseEntity<Boolean> getAlarmProcessingLogSetting(@RequestBody ObjectNumberDto dto) {
        Boolean setting = new ParameterAppPostgreSql().getAlarmProcessingSetting(dto.getObjectNumber());
        return new ResponseEntity<>(setting, HttpStatus.OK);
    }

    @RequestMapping(value = "/alarm_processing_log_settings/", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> getAlarmProcessingLogSettings() {
        Map<String, Boolean> settings = new ParameterAppPostgreSql().getAlarmProcessingSettings();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @RequestMapping(value = "/set_alarm_processing_log_setting/", method = RequestMethod.POST)
    public ResponseEntity<?> updateAlarmProcessingLogSetting(@RequestBody UpdateAlarmProcessingLogSettingDto dto) {
        new ParameterAppPostgreSql().updateAlarmProcessingSetting(dto.getObjectNumber(), dto.isAlarmProcessingLog());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/update_template_color_name/", method = RequestMethod.POST)
    public ResponseEntity<List<AlarmsEventColorSelect>> updateColorEventTypes(@RequestBody AlarmsEventColorUpdate dto) {
        ArrayList<AlarmsEventColorSelect> types = new ParameterAppPostgreSql().updateColorEventTypes(dto.getId(), dto.getId_template_color(), dto.getColor());
        return new ResponseEntity<>(types, HttpStatus.OK);
    }
    
    
     @RequestMapping(value = "/update_status_template_color_name/", method = RequestMethod.POST)
    public ResponseEntity<List<AlarmsEventColorSelect>> updateStatusColorEventTypes(@RequestBody AlarmsEventColorUpdate dto) {
        ArrayList<AlarmsEventColorSelect> types = new ParameterAppPostgreSql().updateStatusColorEventTypes(dto.getId_template_color());
        return new ResponseEntity<>(types, HttpStatus.OK);
    }
       

    @RequestMapping(value = "/get_alarm_processing_log_list/", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<AlarmProcessingLogYear>> getAlarmProcessingLogList() {
        ArrayList<AlarmProcessingLogYear> list = new ParameterAppPostgreSql().getAlarmProcessingLogSegmentsList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }


    /**
     * Выбор действий по отработке тревог для объекта
     */
    @RequestMapping(value = "/change_object_action_list_value/", method = RequestMethod.POST)
    public ResponseEntity<?> addActionInObjectActionList(@RequestBody ObjectActionDto dto) {
        if (!dto.isActive()) {
            new ParameterAppPostgreSql().addActionInObjectActionList(dto.getObjectNumber(), dto.getActionId(), dto.getEventTypeId());
        } else {
            new ParameterAppPostgreSql().removeActionInObjectActionList(dto.getObjectNumber(), dto.getActionId());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    
}
