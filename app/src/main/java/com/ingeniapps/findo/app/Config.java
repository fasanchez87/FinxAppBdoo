package com.ingeniapps.findo.app;

/**
 * Created by FABiO on 07/10/2016.
 */

public class Config
{

    public static final String DEVELOPER_KEY = "AIzaSyBb5-XMsykh7Pa4ilY0pDJ52rUa4KH8jUk";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOMINACION = "newVoto";
    public static final String PUSH_NOTIFICATION = "pushFindo";
    public static final String PUSH_NOTIFICATION_DESPACHADO = "pushDespachado";
    public static final String PUSH_NOTIFICATION_CANCELADO = "pushCancelado";
    public static final String PUSH_NOTIFICATION_USO_TICKET = "pushNotificaClienteUso";
    public static final String PUSH_NOTIFICATION_CLIENTE_ANULAR = "pushNotificaClienteAnular";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final String SHARED_PREF = "ah_firebase";


}
