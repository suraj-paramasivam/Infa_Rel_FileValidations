package com.custom.informatica.fab.credittrading.fix;

import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.Symbol;
import quickfix.fix50.MarketDataRequest;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;


public class FIXParser implements Application {

    private static boolean initiatorStarted = false;
    private static volatile SessionID sessionID;
    private final Initiator initiator = null;

    public static void main(String[] args) throws ConfigError, FileNotFoundException, InterruptedException, SessionNotFound {
        SessionSettings settings = new SessionSettings("res/initiator.config");

        Application application = new FIXParser();
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();
        if (!initiatorStarted) {
            Initiator initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
            initiator.start();
            initiatorStarted = true;
            while (sessionID == null) {
                Thread.sleep(1000);
            }

            System.out.println("the session id is" + sessionID);
            Session session = Session.lookupSession(sessionID);
            userLogon(session);
            //productRequest(session);
            MDRequest(session);

        }
    }


    public static void userLogon(Session session) throws SessionNotFound, InterruptedException {
        Message message = new Message();
        // BeginString
        message.getHeader().setField(new StringField(8, "FIX.4.4"));

        //MsgType
        message.getHeader().setField(new StringField(35, "BE"));

        // SenderCompID
        message.getHeader().setField(new StringField(49, "MD01_FAB_FIXSTG2"));

        // TargetCompID, with enumeration
        message.getHeader().setField(new StringField(56, "FIXCTSBOBGW"));

        //Username
        message.getHeader().setField(new StringField(553, "bbnd_fab_md1"));

        // Password
        message.getHeader().setField(new StringField(554, "testing1"));

        // UserRequestID
        message.getHeader().setField(new StringField(923, "661516808"));

        // UserRequestType
        message.getHeader().setField(new StringField(924, "1"));

        System.out.println(message);
        Session.sendToTarget(message, sessionID);
        Thread.sleep(5000);

    }

    //Product requests
    public static void productRequest(Session session) throws SessionNotFound, InterruptedException {
        Message message = new Message();
        // BeginString
        message.getHeader().setField(new StringField(8, "FIX.5.0"));

        //MsgType
        message.getHeader().setField(new StringField(35, "x"));

        // SenderCompID
        message.getHeader().setField(new StringField(49, "MD02_FAB_FIXSTG5"));

        // TargetCompID, with enumeration
        message.getHeader().setField(new StringField(56, "FIXCTSBOBGW"));
        // SecurityListRequestType
        message.getHeader().setField(new StringField(559, "4"));

        //SecurityReqID
        message.getHeader().setField(new StringField(320, "661516809"));


        // SecurityListRequestType
        message.getHeader().setField(new StringField(263, "1"));

        System.out.println(message);
        Session.sendToTarget(message, sessionID);
        Thread.sleep(5000);
    }

    //Market Data  requests
    public static void MDRequest(Session session) throws SessionNotFound, InterruptedException {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        Message message = new Message();
        MarketDataRequest.NoMDEntryTypes group = new MarketDataRequest.NoMDEntryTypes();
        MarketDataRequest.NoRelatedSym sym = new MarketDataRequest.NoRelatedSym();
        MarketDataRequest marketDataRequest = new MarketDataRequest();

        // BeginString
        message.getHeader().setField(new StringField(8, "FIX.5.0"));

        //MsgType
        message.getHeader().setField(new StringField(35, "V"));

        // SenderCompID
        message.getHeader().setField(new StringField(49, "MD02_FAB_FIXSTG5"));

        // TargetCompID, with enumeration
        message.getHeader().setField(new StringField(56, "FIXCTSBOBGW"));
        // TargetCompID, with enumeration
        message.setField(new StringField(262, randomUUIDString));
        // TargetCompID, with enumeration
        message.setField(new StringField(263, "1"));

        // TargetCompID, with enumeration
        message.setField(new StringField(264, "0"));

        // TargetCompID, with enumeration
        message.setField(new StringField(266, "N"));
        // Subscription Request Type
        message.setField(new StringField(267, "2"));
        // Subscription Request Type
        group.setField(new StringField(269, "0"));
        message.addGroup(group);
        // Subscription Request Type
        group.setField(new StringField(269, "1"));
        message.addGroup(group);


        // Subscription Request Type
        message.setField(new StringField(146, "1"));
        sym.setField(new Symbol("ALL"));

        // Subscription Request Type

        message.addGroup(sym);

        System.out.println(message);
        Session.sendToTarget(message, sessionID);
        Thread.sleep(5000);
    }

    public void decodeMessage() throws ConfigError {
        //MessageFactory messageFactory = new MessageFactory();
        DefaultMessageFactory messageFactory = new DefaultMessageFactory();
        InputStream fix42Input = FIXParser.class.getResourceAsStream("/FIX42_tt.xml");
        DataDictionary dataDictionary = new DataDictionary(fix42Input);

        //Message message = MessageUtils.parse(messageFactory, dataDictionary, messageStr);
    }

    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("OnCreate");
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("OnLogon");
        FIXParser.sessionID = sessionID;


    }

    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("OnLogout");
        FIXParser.sessionID = null;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        System.out.println("ToAdmin");
        try {
            final String msgType = message.getHeader().getString(MsgType.FIELD);
            if (MsgType.LOGON.compareTo(msgType) == 0) {

            }
        } catch (FieldNotFound e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("FromAdmin");
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        System.out.println("ToApp: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("FromApp");
    }

}

