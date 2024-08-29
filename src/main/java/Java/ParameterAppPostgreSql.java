package Java;



import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
//@Service
public class ParameterAppPostgreSql {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterAppPostgreSql.class);
    private Connection connectionDB;
    private static BasicDataSource basicDS;
    private static UUID uuid_server;
    private static UUID uuid_app;
    private UUID uuid_template_color;
    private static String lan_ip;


    /**
     * Для генерации отчетов *
     */
    public static QueueHandlerReport queueHandlerReport;

    /**
     * Для оператора *
     */
    public static QueueHandlerOperator queueHandlerOperator;

    /**
     * Тип арма
     */
    public final static int TYPE_ARM_SECURITY = 0; // Тип АРМ - охранный
    public final static int TYPE_ARM_FIRE = 1; // Тип АРМ - пожарный
    public static int current_type_arm; // Текущий тип АРМ
    /**
     * Индикация пожарного АРМ
     */
    public final static int INDICATOR_FAULT = 1; // Индикатор неисправности
    public final static int INDICATOR_STATE = 2; // Индикатор аварии
    public final static int INDICATOR_ON = 1; // Индикатор включен
    public final static int INDICATOR_OFF = 0; // Индикатор выключен
   

    /**
     * Для инициализации 10 шаблонов template_translate
     */
    private final String[] template_translate = {
            "", "ProtonToSurgard_1", "ProtonToSurgard_2", "ProtonToSurgard_3", "ProtonToSurgard_4",
            "ProtonToSurgard_5", "ProtonToSurgard_6", "ProtonToSurgard_7", "ProtonToSurgard_8",
            "ProtonToSurgard_9", "ProtonToSurgard_10"
    };

    /**
     * Для инициализации 10 шаблонов template_uarm
     */
    private final String[] template_uarm = {"", "ProtonToUarm_1", "ProtonToUarm_2", "ProtonToUarm_3", "ProtonToUarm_4",
            "ProtonToUarm_5",
            "ProtonToUarm_6", "ProtonToUarm_7", "ProtonToUarm_8", "ProtonToUarm_9", "ProtonToUarm_10"
    };

    /**
     * Результат сохранения и обновления таблицы object_s_temp *
     */
    public final static int RESULT_OBJECT_S_1 = 1; // Указанный пультовой номер объекта уже есть в базе
    public final static int RESULT_OBJECT_S_2 = 2; // Указанный адрес и номер системы объекта уже есть в базе
    public final static int RESULT_OBJECT_S_3 = 3; // Указанный идентификатор объекта уже есть в базе
    public final static int RESULT_OBJECT_S_4 = 4; // Сохранение или обновление произведено
    public final static int RESULT_OBJECT_S_5 = 5; // Ошибка
    public final static int RESULT_OBJECT_S_6 = 6; // Допустимое число объектов по текущей лицензии =

    /**
     * Результат авторизации при отсутствии информации в базе, заносим логин как
     * not_authorized *
     */
    public final static String RESULT_NOT_AUTHORIZED = "not_authorized"; // Ошибка

    /**
     * Token для удаленного арма *
     */
    public static byte[] TOKEN_UARM;

    public static JavaMailSender javaMailSender;
    public static String from_email;
    public ParameterAppPostgreSql() {
    }
    /**
     *
     */
    public void connectInit() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            this.uuid_server = Parameters.uuid_server;// UUID текущего сервера
            this.uuid_app = Parameters.uuid_app;// UUID текущего экземпляра приложения
            this.queueHandlerReport = new QueueHandlerReport();
            this.queueHandlerOperator = new QueueHandlerOperator();
            connectionInitDB();
            this.current_type_arm = selectServer_passport_mode_arm(); // Текущий тип АРМ
        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
        }
    }

    public void connectionInitDB() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            if (basicDS != null) {
                basicDS.close();
            }
            basicDS = new BasicDataSource();
            basicDS.setDriverClassName("org.postgresql.Driver");
            basicDS.setUrl(Parameters.url);
            basicDS.setUsername(Parameters.username);
            basicDS.setPassword(Parameters.password);

            basicDS.setMinIdle(10);
            basicDS.setInitialSize(10);
            basicDS.setMaxIdle(35);
            basicDS.setMaxWaitMillis(5000); // wait 5 seconds to get new connection
            basicDS.setMaxTotal(30);
            basicDS.setTestOnBorrow(true);
            basicDS.setValidationQuery("select 1");
            basicDS.setValidationQueryTimeout(10); // The value is in seconds

            basicDS.setTimeBetweenEvictionRunsMillis(600000); // 10 minutes wait to run evictor process
            basicDS.setSoftMinEvictableIdleTimeMillis(600000); // 10 minutes wait to run evictor process
            basicDS.setMinEvictableIdleTimeMillis(60000); // 60 seconds to wait before idle connection is evicted
            basicDS.setMaxConnLifetimeMillis(600000); // 10 minutes is max life time
            basicDS.setNumTestsPerEvictionRun(10);
        } catch (Exception ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
        }
    }
    /**
     * Select из таблиц user_object
     *
     * @return
     */
    public List<String> selectObjectUserToListString(String login) {
        List<String> result = new ArrayList<String>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();

            preparedStatement = connection.prepareStatement(
                    "SELECT numobj FROM user_object WHERE login=? and uuid_server_passport=? ");
            preparedStatement.setString(1, login);
            preparedStatement.setObject(2, uuid_server);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> numobjList = new ArrayList<>();
            while (resultSet != null && resultSet.next()) {
                numobjList.add(resultSet.getString("numobj"));
            }

            for (int i = 0; i < numobjList.size(); i++) {
                result.add(selectObject_description_temp_numobj_login_obj(numobjList.get(i)));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	/**
     * Select из таблицу journal_events
     *
     * @return
     */
    public List<JournalEventsAndAlarm> selectJournalEvents() {
        List<JournalEventsAndAlarm> result = new ArrayList<JournalEventsAndAlarm>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();

            preparedStatement = connection.prepareStatement(
                    "SELECT je.uuid_server_passport ,je.id_journal_events ,je.time_sys ,"
                            + "je.time_event ,je.protokol ,je.num_syst ,je.num_repeat ,"
                            + "je.num_obj ,je.num_channel ,je.code_mess ,je.transmit_level_signal ,"
                            + "je.repeat_level_signal ,je.class_mess ,je.type_mess ,je.text_event ,"
                            + "je.operator_name ,je.on_close ,je.extension ,je.value_ext ,je.name_obj ,"
                            + "je.address_obj ,je.type_extension ,je.iltered ,je.code_distr ,je.deily_report ,"
                            + "je.num_slot ,je.fl_gbr_processing , apec.class_color " +
                    "FROM journal_events AS je "+
                    "JOIN alarm_processing_event_color AS apec ON je.class_mess = apec.class_mess ");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_journal_events"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getString("event_class_color")));
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Select из таблицу journal_events
     *
     * @return
     */
    public List<JournalEventsAndAlarm> selectJournalEventsNumobj(String num_obj, Integer limit) {
        List<JournalEventsAndAlarm> result = new ArrayList<JournalEventsAndAlarm>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT je.uuid_server_passport ,je.id_journal_events ,"
                            + "je.time_sys ,je.time_event ,je.protokol ,"
                            + "je.num_syst ,je.num_repeat ,je.num_obj ,"
                            + "je.num_channel ,je.code_mess ,je.transmit_level_signal ,"
                            + "je.repeat_level_signal ,je.class_mess ,je.type_mess ,"
                            + "je.text_event ,je.operator_name ,je.on_close ,"
                            + "je.extension ,je.value_ext ,je.name_obj ,"
                            + "je.address_obj ,je.type_extension ,je.iltered ,"
                            + "je.code_distr ,je.deily_report ,je.num_slot ,je.fl_gbr_processing , apec.class_color " +
                    "JOIN alarm_processing_event_color AS apec ON je.class_mess = apec.class_mess "+
                    "FROM journal_events AS je "+
                    "where num_obj=? and uuid_server_passport= ? "+
                    "order by id_journal_events desc"
                    // ?"
            );
            preparedStatement.setString(1, num_obj);
            preparedStatement.setObject(2, uuid_server);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_journal_events"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getString("event_class_color")));
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Список событий за последние сутки с цветом (Отфильтровать через UserService)
     */
    public List<ShortEvent> getEventsLastDay(String login) {
        long start = new OwnTimestamp().beginningOfTheCurrentDay();
        List<ShortEvent> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        uuid_template_color = UUID.fromString(getColorEventTypesStart());
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT je.id_journal_events, je.time_sys, je.num_obj, je.name_obj, je.address_obj, je.num_channel, je.code_mess, je.text_event, je.class_mess, je.type_mess, apec.class_color " +
                            "FROM journal_events AS je " +
                            "JOIN alarm_processing_event_color AS apec ON je.class_mess = apec.class_mess "+
                            "WHERE time_sys > ? " +
                            "AND je.uuid_server_passport = ? " +
                            "AND apec.uuid_template_color = ? " +
                            "AND je.num_obj IN (SELECT numobj FROM user_object WHERE login = ?) " +
                            "ORDER BY id_journal_events DESC;");
            preparedStatement.setLong(1, start);
            preparedStatement.setObject(2, uuid_server);
            preparedStatement.setObject(3, uuid_template_color);
            preparedStatement.setString(4, login);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new ShortEvent(
                        resultSet.getInt("id_journal_events"),
                        -1,
                        null,
                        null,
                        resultSet.getLong("time_sys"),
                        resultSet.getString("num_obj"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("class_color")));
            }

            preparedStatement = connection.prepareStatement(
                    "SELECT jea.id_journal_events, jea.time_sys, jea.num_obj, jea.name_obj, jea.address_obj, jea.num_channel, jea.code_mess, jea.text_event, jea.class_mess, jea.type_mess, apec.class_color " +
                            "FROM journal_events_archive AS jea "+
                            "JOIN alarm_processing_event_color AS apec ON jea.class_mess = apec.class_mess "+
                            "WHERE time_sys > ? " +
                            "AND jea.uuid_server_passport = ? " +                            
                            "AND apec.uuid_template_color = ? " +
                            "AND jea.num_obj IN (SELECT numobj FROM user_object WHERE login = ?) " +
                            "ORDER BY id_journal_events DESC;");
            preparedStatement.setLong(1, start);
            preparedStatement.setObject(2, uuid_server);
            preparedStatement.setObject(3, uuid_template_color);
            preparedStatement.setString(4, login);
            resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new ShortEvent(
                        resultSet.getInt("id_journal_events"),
                        -1,
                        null,
                        null,
                        resultSet.getLong("time_sys"),
                        resultSet.getString("num_obj"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("class_color")));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Select из таблицу journal_events
     *
     * @return
     */
    public List<JournalEventsAndAlarm> selectJournalEventsArchiveLogin(String login, Integer limit,
                                                                       List<JournalEventsAndAlarm> journalEventsAndAlarmList) {
        List<JournalEventsAndAlarm> result = journalEventsAndAlarmList;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM journal_events_archive where  num_obj IN (select numobj from user_object where login=? ) OR num_obj='WebServer'  order by id_journal_events desc limit ?");
          
            preparedStatement.setString(1, login);
            preparedStatement.setInt(2, limit);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_journal_events"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getString("event_class_color")));
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Список тревог
     */
    public List<JournalEventsAndAlarm> selectJournalAlarmAlarmLogin(String login) {
        List<JournalEventsAndAlarm> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            int event_handling = 2;// Отмена
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM journal_alarm " +
                            "WHERE uuid_server_passport = ? " +
                            "AND event_handling <> ? " +
                            "AND num_obj IN (SELECT numobj FROM user_object WHERE login = ?) " +
                            "AND state_object <> 5 " +
                            "ORDER BY id_alarm DESC;");
            preparedStatement.setObject(1, uuid_server);
            preparedStatement.setInt(2, event_handling);
            preparedStatement.setString(3, login);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                JournalEventsAndAlarm journalEventsAndAlarm = new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_alarm"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getInt("event_handling"),
                        resultSet.getLong("time_operator"),
                        resultSet.getInt("state_object"),
                        resultSet.getLong("addrdev"),
                        resultSet.getInt("state_devise"),
                        resultSet.getString("event_class_color"));

                result.add(journalEventsAndAlarm);
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Отработка тревог. Классы и типы событий. Действия по тревоге.
     * Наполнение списков действий и отмен тревог.
     */
    public List<AlarmsEventClass> getEventClasses() {
        List<AlarmsEventClass> classes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM alarm_processing_event_classes ORDER BY event_class_id;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                classes.add(new AlarmsEventClass(
                        resultSet.getInt("event_class_id"),
                        resultSet.getString("event_class_name"),
                        resultSet.getString("event_class_color")));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return classes;
    }
	
	/**
     * Отработка тревог. Классы и типы событий. 
     * Наполнение списков цветов.
     */
    public List<AlarmsEventColor> getColorEventClasses() {
        List<AlarmsEventColor> classes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM alarm_processing_event_color ORDER BY class_mess;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                classes.add(new AlarmsEventColor(
                        resultSet.getInt("class_mess"),
                        resultSet.getString("name_class_mess"),
                        resultSet.getString("class_color")));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return classes;
    }
	
	public String getColorEventTypes(int classId) {
        String types = "";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT  name_class_mess, class_color  FROM alarm_processing_event_color WHERE class_mess=? ORDER BY class_mess;");
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {                
                        types = resultSet.getString("class_color");
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return types;
    }
	
	//Выбор шаблона цветов для отоброжения в ленте событий
    public String getColorEventTypesStart() {
        String types = "";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT uuid_template_color  FROM alarm_processing_event_color_name WHERE status_template_color = 1;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {                
                        types = resultSet.getString("uuid_template_color");
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return types;
    }
	
	public List<AlarmsEventColorSelect> getColorEventTypesClass(int classId) {
        List<AlarmsEventColorSelect> action = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT  class_mess, name_class_mess, class_color  FROM alarm_processing_event_color WHERE id_template_color=? ORDER BY class_mess;");
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                action.add(new AlarmsEventColorSelect(
                        resultSet.getInt("class_mess"),
                        resultSet.getString("name_class_mess"),
                        resultSet.getString("class_color")));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return action;
    }
    
    
    
    public ArrayList<AlarmsEventColorSelect> updateColorEventTypes(int class_mess, int id_template_color, String class_color) {
        ArrayList<AlarmsEventColorSelect> types = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "UPDATE alarm_processing_event_color SET class_color=? WHERE id_template_color = ? AND class_mess = ?;");
            preparedStatement.setString(1, class_color);
            preparedStatement.setInt(2, id_template_color);
            preparedStatement.setInt(3, class_mess);
            preparedStatement.executeUpdate();
            PreparedStatement preparedStatementSel = null;
            preparedStatementSel = connection.prepareStatement(
                    "SELECT  class_mess, name_class_mess, class_color  FROM alarm_processing_event_color WHERE id_template_color=? ORDER BY class_mess;");
            preparedStatementSel.setInt(1, id_template_color);
            ResultSet resultSet = preparedStatementSel.executeQuery();
            while (resultSet != null && resultSet.next()) {
                types.add(new AlarmsEventColorSelect(
                        resultSet.getInt("class_mess"),
                        resultSet.getString("name_class_mess"),
                        resultSet.getString("class_color")));
            }            

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return types;
    }

    //Обновление статуса при выборе шаблона цвета событий
    public ArrayList<AlarmsEventColorSelect>  updateStatusColorEventTypes(int id_template_color) {
        ArrayList<AlarmsEventColorSelect> types = new ArrayList<>();        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            PreparedStatement preparedStatementUpdate = null;
            preparedStatementUpdate = connection.prepareStatement(
                    "UPDATE alarm_processing_event_color_name SET status_template_color=0;");
            preparedStatementUpdate.executeUpdate();
            
            preparedStatement = connection.prepareStatement(
                    "UPDATE alarm_processing_event_color_name SET status_template_color=1 WHERE id_template_color = ?;");
            preparedStatement.setInt(1, id_template_color);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return types;
    }
	
	public AlarmsEvent setEventClassAndType(AlarmsEvent event) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT e.event_type_id, c.event_class_id, c.event_class_color FROM alarm_processing_events AS e " +
                            "JOIN alarm_processing_event_types AS t ON e.event_type_id = t.event_type_id " +
                            "JOIN alarm_processing_event_classes AS c ON t.event_class_id = c.event_class_id " +
                            "WHERE e.event_code = ? ;");
            preparedStatement.setInt(1, event.getEventCode());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null && resultSet.next()) {
                event.setEventClassId(resultSet.getInt("event_class_id"));
                event.setEventTypeId(resultSet.getInt("event_type_id"));
                event.setColor(resultSet.getString("event_class_color"));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return event;
    }
	
	/**
     * Список неисправностей
     */
    public List<JournalEventsAndAlarm> selectJournalAlarmFailLogin(String login) {
        List<JournalEventsAndAlarm> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            int event_handling = 2;// Отмена
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM journal_alarm " +
                            "WHERE uuid_server_passport = ? " +
                            "AND event_handling <> ? " +
                            "AND num_obj IN (SELECT numobj FROM user_object WHERE login = ?) " +
                            "AND state_object = 5 " +
                            "ORDER BY id_alarm DESC;");
            preparedStatement.setObject(1, uuid_server);
            preparedStatement.setInt(2, event_handling);
            preparedStatement.setString(3, login);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                result.add(new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_alarm"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getInt("event_handling"),
                        resultSet.getLong("time_operator"),
                        resultSet.getInt("state_object"),
                        resultSet.getLong("addrdev"),
                        resultSet.getInt("state_devise"),
                        resultSet.getString("event_class_color")));
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	/**
     * Select из таблицу journal_alarm
     *
     * @return
     */
    public JournalEventsAndAlarm selectJournalAlarmId_alarm(long id_alarm) {
        JournalEventsAndAlarm result = new JournalEventsAndAlarm();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT je.uuid_server_passport ,je.id_journal_events ,je.time_sys ,je.time_event ,"
                            + "je.protokol ,je.num_syst ,je.num_repeat ,je.num_obj ,je.num_channel ,"
                            + "je.code_mess ,je.transmit_level_signal ,je.repeat_level_signal ,je.class_mess ,"
                            + "je.type_mess ,je.text_event ,je.operator_name ,je.on_close ,je.extension ,je.value_ext ,"
                            + "je.name_obj ,je.address_obj ,je.type_extension ,je.iltered ,je.code_distr ,je.deily_report ,"
                            + "je.num_slot ,je.fl_gbr_processing , apec.class_color " +
                    "FROM journal_events AS je "+
                    "JOIN alarm_processing_event_color AS apec ON je.class_mess = apec.class_mess "+
                    "where uuid_server_passport=? " +
                    "and id_alarm=? ");

            preparedStatement.setObject(1, uuid_server);
            preparedStatement.setLong(2, id_alarm);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null && resultSet.next()) {
                result = new JournalEventsAndAlarm(
                        (UUID) resultSet.getObject("uuid_server_passport"),
                        resultSet.getInt("id_alarm"),
                        resultSet.getLong("time_sys"),
                        resultSet.getLong("time_event"),
                        resultSet.getInt("protokol"),
                        resultSet.getInt("num_syst"),
                        resultSet.getInt("num_repeat"),
                        resultSet.getString("num_obj"),
                        resultSet.getInt("num_channel"),
                        resultSet.getInt("code_mess"),
                        resultSet.getInt("transmit_level_signal"),
                        resultSet.getInt("repeat_level_signal"),
                        resultSet.getInt("class_mess"),
                        resultSet.getInt("type_mess"),
                        resultSet.getString("text_event"),
                        resultSet.getString("operator_name"),
                        resultSet.getInt("on_close"),
                        resultSet.getString("extension"),
                        resultSet.getInt("value_ext"),
                        resultSet.getString("name_obj"),
                        resultSet.getString("address_obj"),
                        resultSet.getInt("type_extension"),
                        resultSet.getInt("iltered"),
                        resultSet.getString("code_distr"),
                        resultSet.getInt("deily_report"),
                        resultSet.getInt("num_slot"),
                        resultSet.getInt("fl_gbr_processing"),
                        resultSet.getInt("event_handling"),
                        resultSet.getLong("time_operator"),
                        resultSet.getInt("state_object"),
                        resultSet.getLong("addrdev"),
                        resultSet.getInt("state_devise"),
                        resultSet.getString("event_class_color"));
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return result;
    }
	
	public String selectCodeEventColor(Object uuid_template_color, Integer class_mess) {       
        String event_class_color = "";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = basicDS.getConnection();
            preparedStatement = connection.prepareStatement(
                    "SELECT class_color "+
                    "FROM alarm_processing_event_color "+
                    "where uuid_template_color=? " +
                    "and class_mess=? ");
            preparedStatement.setObject(1, uuid_template_color);
            preparedStatement.setInt(2, class_mess);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null && resultSet.next()) {                                                  
                event_class_color = resultSet.getString("class_color");
            }

        } catch (SQLException ex) {
            LOGGER.error(ex.getStackTrace()[0].toString() + ":" + ex.getMessage());
            isConnection();
        } finally {
            closePreparedStatementAndConnection(preparedStatement, connection);
        }
        return event_class_color;
    }		
}
