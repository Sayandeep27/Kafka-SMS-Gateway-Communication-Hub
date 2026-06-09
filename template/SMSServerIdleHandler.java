package com.icici.smsgateway.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import com.icici.smsgateway.common.GlobalFunc;
import com.icici.smsgateway.common.KafkaConfig;
import com.icici.smsgateway.common.UniqueIdGenerator;
import com.icici.smsgateway.dao.KafkaResponseData;
import com.icici.smsgateway.dao.DBPool;
import com.icici.smsgateway.dao.KafkaDataSend;
import com.icici.smsgateway.mail.JSendMailCode;
import org.json.JSONObject;

public class SMSServerIdleHandler extends ChannelDuplexHandler {

    /**
     * private static final Logger logger =
     * Logger.getLogger(SMSServerIdleHandler.class.getName());
     */
    private static final Logger logger = LogManager.getLogger(SMSServerIdleHandler.class.getName());

    private final DBPool dbpool;
    private final List lstIM;
    private final List lstVD;
    private final List lpsd; // For getting Priority
    private static String SeperatorString = "^`!";
    private String messageType = "";
    private String errorMsg = "";
    private String nPipeId = "";
    private String nPipePnId = "";
    private String sourceIP = "";
    private String sourcePort = "";
    String localPort = "";
    String localIP = "";
    int messagePriority = 0; // store message priority
    Map<String, String> pnCheck;
    Properties prop;
    Collection clients;

    SMSServerIdleHandler(Properties prop, Collection<String> clientips, DBPool mdbpool, List<String> lstIM,
            List<String> lstVD, List<String> lpsd, Map<String, String> pnCheck) {

        /* System.out.println("TES7: " ); */
        this.prop = prop;
        this.clients = clientips;
        this.dbpool = mdbpool;
        this.lstIM = lstIM;
        this.lstVD = lstVD;
        this.lpsd = lpsd;
        this.pnCheck = pnCheck;
    }

    private String replaceUnWantedChar(String inStr) {
        // inStr = inStr.replace("\\", "");
        inStr = inStr.replace("\r", "");
        inStr = inStr.replace("\n", "");
        // inStr = inStr.replace("\r\n", "");
        inStr = inStr.trim();
        return inStr;
    }

    private String replaceUnWantedCharXML(String inStr) {
        /*
         * inStr=inStr.toString().replace("\\","");
         * inStr=inStr.toString().replace("\\r","");
         * inStr=inStr.toString().replace("\\n","");
         * inStr=inStr.toString().replace("\\r\\n","");
         * inStr=inStr.toString().replace("&","&amp;");
         * inStr=inStr.toString().replace("<","&lt;");
         * inStr=inStr.toString().replace(">","&gt;");
         * inStr=inStr.toString().replace("\"","&quot;");
         */
        // inStr = inStr.replace("'", "\"");
        /*
         * inStr=inStr.toString().replace("&"," and ");
         * inStr=inStr.toString().replace(" & "," and ");
         * inStr=inStr.toString().replace("& "," and ");
         * inStr=inStr.toString().replace(" &"," and ");
         */
        inStr = inStr.replace("<MESSAGE>", "<MESSAGE><![CDATA[");
        inStr = inStr.replace("</MESSAGE>", "]]></MESSAGE>");
        inStr = inStr.trim();
        return inStr;
    }

    private String replaceInMobile(String inStr) {
        // inStr = inStr.replace("/", "");
        inStr = inStr.replace(" ", "");
        inStr = inStr.replace("-", "");
        inStr = inStr.replace("\\r", "");
        inStr = inStr.replace("\\n", "");
        // inStr = inStr.replace("\\r\\n", "");
        inStr = inStr.trim();
        return inStr;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // SMSIdleServer._nClientCount++;
        SMSIdleServer._nClientCount.getAndIncrement();
        // TODO
        // As per request from support team and sarfaraz khan, we are uncomment
        // this line after 2 round testing with commented line
        System.out.println("Client Connected ::" + SMSIdleServer._nClientCount + " at " + SMSIdleServer._connString); // added by sonu punia
        logger.info("Client Connected ::" + SMSIdleServer._nClientCount + " at " + SMSIdleServer._connString); // added by sonu punia

        String localAddress = ctx.channel().localAddress().toString();
        localPort = localAddress.substring(localAddress.indexOf(":") + 1);
        localIP = localAddress.substring(1, localAddress.indexOf(":"));

        String sourceAdd = ctx.channel().remoteAddress().toString();
        sourceIP = sourceAdd.substring((sourceAdd.indexOf("/") + 1), (sourceAdd.indexOf(":")));
        sourcePort = sourceAdd.substring((sourceAdd.indexOf(":") + 1));
        // TODO
        // checkMaxConnection();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SMSIdleServer._nClientCount.getAndDecrement();
        // As per request from support team and sarfaraz khan, we are uncomment
        // this line after 2 round testing with commented line
        System.out.println("Channel disconnected");
        logger.info("Channel disconnected");
    }

    private void checkMaxConnection() {
        if (SMSIdleServer._nClientCount.get() > Integer.parseInt(prop.getProperty("MAXCONN"))) {
            // sendAlert(); // added by sonu punia
        }
    }

    private boolean checkMaxClient() {
        return false;
        // if (Integer.parseInt(Base64Coder
        //         .decodeString(prop.getProperty("MAXCL"))) >
        //         SMSIdleServer._nClientCount) {
        //
        //     return false;
        // } else {
        //     return true;
        // }
    }

    // added by sonu punia
    private void sendAlert() {

        Connection con = null;
        CallableStatement st = null, stmt = null;
        ResultSet rs = null;
        InetAddress localhost = null;
        List<String> lstRec = null;
        JSendMailCode mail = null;

        try {
            localhost = (InetAddress.getLocalHost());
        } catch (UnknownHostException e1) {
            logger.error("Error :=> ", e1);
        }

        try {
            con = dbpool.getConnection();
            st = con.prepareCall("{call SMS_ALERT_NUMOF_CONNECTIONS(?,?,?,?,?)}");
            st.setString(1, "Number of connections on Server " + localIP + " " + "and Listener : " + localPort
                    + " Exceeded " + prop.getProperty("MAXCONN") + ".Please check."); // message
            st.setString(2, sourceIP);   // sourceip
            st.setString(3, sourcePort); // sourceport
            st.setString(4, localIP);    // localip
            st.setString(5, localPort);  // localport
            rs = st.executeQuery();
            logger.info("----------------------------------------------------------------------------->>>>>>>>>>>>>>>>>>Crossed Max conn");

            // stmt = con.prepareCall("{call MAIL_ALERT_NUMOF_CONNECTIONS(?,?,?,?)}");
            // stmt.setString(1, sourceIP);   // sourceip
            // stmt.setString(2, sourcePort); // sourceport
            // stmt.setString(3, localIP);    // localip
            // stmt.setString(4, localPort);  // localport
            // stmt.executeQuery();

            mail = new JSendMailCode();
            lstRec = new ArrayList<String>();
            int i = 0;
            while (rs.next()) {
                lstRec.add(i, rs.getString("EmailID"));
                i++;
            }
            if (!lstRec.isEmpty() && lstRec != null) {
                mail.SendMailSMTP(lstRec,
                        "Number of connections on Server " // message
                                + localIP + " " + "Port:" + localPort + " Exceeded " + prop.getProperty("MAXCONN")
                                + ".Please check.",
                        "No.of connections on IP:" + localIP + " Port:" + localPort + " exceeded "
                                + prop.getProperty("MAXCONN")
                                + ". Check open connections and close unused connections."
                                + "Coordinate with SMS team(022-24906001).",
                        prop);
            }

        } catch (Exception ex) {
            logger.error("Error in channelConnected method SMSServerIdleHandler ", ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (stmt != null)
                    stmt.close();
                if (con != null)
                    dbpool.releaseConnection(con);
            } catch (Exception e2) {
                logger.error("Error while closing connection in method SMSServerIdleHandler ", e2);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // String localAddress = ctx.channel().getLocalAddress().toString();
        // localPort = localAddress.substring(localAddress.indexOf(":") + 1);
        // localIP = localAddress.substring(1, localAddress.indexOf(":"));

        String sourceAdd = ctx.channel().remoteAddress().toString();
        // sourceIP = sourceAdd.substring((sourceAdd.indexOf("/") + 1),
        //         (sourceAdd.indexOf(":")));
        // sourcePort = sourceAdd.substring((sourceAdd.indexOf(":") + 1));

        ByteBuf cbReq = (ByteBuf) msg;
        StringBuffer sbReq = new StringBuffer();
        sbReq.append(cbReq.toString(Charset.defaultCharset()).trim());

        if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
            logger.info("Request from " + sourceIP + ":" + sbReq.toString());

        try {
            String strXMLOrg = replaceUnWantedChar(sbReq.toString());
            logger.info("XML STRING :" + strXMLOrg);
            List<String> lstVal = new ArrayList<String>();
            lstVal = validXML(strXMLOrg, ctx.channel());
            if (lstVal.get(0).equalsIgnoreCase("SUCCESS")) {
                strXMLOrg = lstVal.get(1);
                // if (validClient(e.getRemoteAddress().toString())) {
                do {
                    String strXML = "";
                    if (strXMLOrg.length() > strXMLOrg.indexOf("#END#") + 5) {
                        strXML = strXMLOrg.substring(0, strXMLOrg.indexOf("#END#") + 5);
                        strXMLOrg = strXMLOrg.substring(strXMLOrg.indexOf("#END#") + 5);
                    } else if (strXMLOrg.length() == strXMLOrg.indexOf("#END#") + 5) {
                        strXML = strXMLOrg.substring(0, strXMLOrg.indexOf("#END#") + 5);
                        strXMLOrg = "";
                    }

                    String[] tokens = strXML.split("\\" + SeperatorString);
                    tokens[0] = replaceUnWantedChar(tokens[0].toString());

                    if (tokens[0].equals("CON")) {
                        try {
                            this.messageType = tokens[1].toString().replace("\\r\\n", "");
                            if (tokens.length > 2) {
                                String threadName = tokens[2].toString().replace("\\r\\n", "");
                            }
                            String strRes = "";
                            if (checkMaxClient()) {
                                strRes = "CON!-1" + SeperatorString;
                            } else {
                                strRes = "CON!0" + SeperatorString;
                            }
                            writeInChannel(strRes, ctx.channel());
                        } catch (Exception econ) {
                            writeInChannel("CON!111" + SeperatorString, ctx.channel());
                            logger.error("Error in connection", econ);
                            errorDump("", "", "", sbReq.toString(), GlobalFunc.getDBDateTime(),
                                    "Error in CON::" + econ.getMessage(), getLnrPort(ctx.channel()),
                                    getLnrIP(ctx.channel()), sourceIP, sourcePort);
                        }

                    } else if (tokens[0].equals("MSG")) {
                        try {
                            SMSIdleServer._nMSGCount++;
                            logger.info("Message Received :: " + SMSIdleServer._nMSGCount + " On "
                                    + GlobalFunc.getDBDateTime() + " at " + SMSIdleServer._connString);
                            String strReq = "";
                            strReq = tokens[1].replace("#END#", "");
                            // *****START****** Code change added for FILE UPLOAD
                            // if (this.messageType.equalsIgnoreCase("XML#END#"))
                            //     this.messageType = "XML";
                            // *****END******
                            if (this.messageType.equalsIgnoreCase("XML")) {
                                strReq = "<XML>" + strReq + "</XML>";
                                if (validClient(ctx.channel().remoteAddress().toString())) {
                                    String strRes = getResponse(strReq, ctx.channel().localAddress().toString(),
                                            ctx.channel());
                                    // logger.info(" RAW XML Response from SMS service ::" + strRes);
                                    writeInChannel(strRes, ctx.channel());
                                    // if (SMSIdleServer._nMSGCount == 10)
                                    //     System.exit(0);
                                } else {
                                    if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                                        logger.info("Request received from unknown client!!!" + sourceAdd);
                                    /*
                                     * Sushmita changes for minimizing logs in file....commenting following
                                     * writeInChannel line 9 March 2022
                                     */
                                    writeInChannel(
                                            "Request received from unknown client!!! " + ctx.channel().remoteAddress(),
                                            ctx.channel());
                                    // TODO 26102015
                                    // errorDump(
                                    //         "Unknown",
                                    //         "Unknown",
                                    //         "",
                                    //         strReq,
                                    //         GlobalFunc.getDBDateTime(),
                                    //         "Request received from unknown client!!!",
                                    //         getLnrPort(e.getChannel()),
                                    //         getLnrIP(e.getChannel()), sourceIP,
                                    //         sourcePort);
                                    // invalid ip entry in table here//
                                }
                            } // else {
                              //     buf.clear();
                              // }
                        } catch (Exception emsg) {
                            writeInChannel("ACK!111" + SeperatorString, ctx.channel());
                            logger.error("Error in connection", emsg);
                            errorDump("", "", "", sbReq.toString(), GlobalFunc.getDBDateTime(),
                                    "Error in MSG::" + emsg.getMessage(), getLnrPort(ctx.channel()),
                                    getLnrIP(ctx.channel()), sourceIP, sourcePort);
                        }

                    } else if (tokens[0].equals("EXT")) {
                        // logger.info("MESSAGE RCVD FROM CLIENT EXT. Exiting thread");
                        try {
                            // SMSIdleServer._nClientCount--;
                            String strRes = "EXT!Client Requested Exit" + SeperatorString;
                            // logger.info(" RAW XML Response from SMS service ::" + strRes);
                            writeInChannel(strRes, ctx.channel()); // Need to disable this line for ICORE
                            // e.getChannel().close();
                        } catch (Exception ex) {
                            writeInChannel("EXT!111" + SeperatorString, ctx.channel()); // Need to disable this line for ICORE
                            logger.error("Error in connection", ex);
                            errorDump("", "", "", sbReq.toString(), GlobalFunc.getDBDateTime(),
                                    "Error in MSG::" + ex.getMessage(), getLnrPort(ctx.channel()),
                                    getLnrIP(ctx.channel()), sourceIP, sourcePort);
                        } finally {
                            try {
                                // Thread.sleep(3000);
                                ctx.channel().close();
                            } catch (Exception e2) {
                                // TODO: handle exception
                                logger.error("Error in EXT client close", e2);
                            }
                        }

                    } else if (tokens[0].equals("KEEPALIVE")) {
                        logger.info("MESSAGE RCVD FROM CLIENT KEEPALIVE.");
                    }

                } while (strXMLOrg.indexOf("#END#") != -1);
                // }
                // else {
                //     if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                //         logger.info("Request received from unknown client!!!" + sourceAdd);
                //     writeInChannel("Request received from unknown client!!! "
                //             + e.getChannel().getRemoteAddress(), e.getChannel());
                //     errorDump("", "", "", sbReq.toString(), GlobalFunc.getDBDateTime(),
                //             "Request received from unknown client!!!",
                //             getLnrPort(e.getChannel()),
                //             getLnrIP(e.getChannel()), sourceIP, sourcePort);
                //     // invalid ip entry in table here//
                // }
            }
        } catch (Exception em) {
            errorDump("", "", "", sbReq.toString(), GlobalFunc.getDBDateTime(),
                    "Error in messageReceived::" + em.getMessage(), getLnrPort(ctx.channel()),
                    getLnrIP(ctx.channel()), sourceIP, sourcePort);
            logger.error("Error in messageReceived ", em);
        }
        /*
         * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
         *     logger.info("messageReceived End:" + System.currentTimeMillis());
         */
    }

    private void dbalert(String localIP2, String localPort2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("Child Channel Idle, closing connection"); // Need to disable this line for file upload client
        ctx.channel().close(); // Need to disable this line for file upload client
    }

    /*
     * @Override
     * public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
     *     System.out.println("Child Channel Idle, closing connection"); // Need to disable this line for file upload client
     *     ctx.channel().close(); // Need to disable this line for file upload client
     * }
     */

    private boolean validClient(String strClientIP) {
        String strIPAuth = this.prop.getProperty("ipauth");
        logger.info("strIPAuth" + strIPAuth);
        if (strIPAuth != null && strIPAuth.equalsIgnoreCase("N")) {
            return true;
        } else {
            strClientIP = strClientIP.substring(1, strClientIP.indexOf(":"));
            if (this.clients.contains((String) strClientIP)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Commented by sushmita for removal of extra character
     * Old Code 20 June 2022 Valid Xml
     */

    /*
     * private List<String> validXML(String strXML, Channel cnl) {
     *     List<String> lstVal = new ArrayList<String>();
     *     lstVal.add(0, "FAILED");
     *     lstVal.add(1, strXML);
     *     // logger.info("START::" + strXML.substring(0, 6));
     *
     *     int idxcon = strXML.indexOf("CON^`!");
     *     int idxmsg = strXML.indexOf("MSG^`!");
     *     int idxext = strXML.indexOf("EXT^`!");
     *     int idxka  = strXML.indexOf("KEEPALIVE^`!");
     *
     *     if (strXML.substring(0, 6).equalsIgnoreCase("CON^`!")
     *             || strXML.substring(0, 6).equalsIgnoreCase("MSG^`!")
     *             || strXML.substring(0, 6).equalsIgnoreCase("EXT^`!")
     *             || strXML.substring(0, 12).equalsIgnoreCase("KEEPALIVE^`!")) {
     *         // logger.info("END::" + strXML.substring(strXML.length() - 5));
     *         if (strXML.substring(strXML.length() - 5).equalsIgnoreCase("#END#")) {
     *             lstVal.add(0, "SUCCESS");
     *             lstVal.add(1, strXML);
     *             return lstVal;
     *         } else if (strXML.lastIndexOf("#END#") != -1) {
     *             lstVal.add(0, "SUCCESS");
     *             strXML = strXML.substring(0, strXML.lastIndexOf("#END#") + 5);
     *             lstVal.add(1, strXML);
     *             return lstVal;
     *         } else {
     *             logger.error("Error in end part of message");
     *             writeInChannel("ACK!111", cnl);
     *         }
     *     } else {
     *         logger.error("Error in start part of message");
     *         logger.error(strXML);
     *         writeInChannel("ACK!111", cnl);
     *         String localAddress = cnl.localAddress().toString();
     *         errorDump("", "", "", strXML, GlobalFunc.getDBDateTime(),
     *                 "Error in start part of message",
     *                 localAddress.substring(localAddress.indexOf(":") + 1),
     *                 localAddress.substring(1, localAddress.indexOf(":")),
     *                 sourceIP, sourcePort);
     *     }
     *     return lstVal;
     * }
     */

    /* New code Added by Sushmita (extra character removal) 20 june 2022 */
}

private List validXML(String strXML, Channel cnl) {

    List<String> lstVal = new ArrayList<String>();
    lstVal.add(0, "FAILED");
    lstVal.add(1, strXML);
    // logger.info("START::"+strXML.substring(0, 6));

    /*
     * int idxcon = strXML.indexOf("CON^`!") ;
     * int idxmsg = strXML.indexOf("MSG^`!") ;
     * int idxext = strXML.indexOf("EXT^`!") ;
     * int idxka  = strXML.indexOf("KEEPALIVE^`!") ;
     */

    /******************* code for unwanted character removal 26 April 2022 ***************************************/

    if (strXML.contains("CON^`!") || strXML.contains("MSG^`!") || strXML.contains("EXT^`!")
            || strXML.contains("KEEPALIVE^`!")) {

        if (strXML.contains("CON^`!")) {
            int b = strXML.indexOf("CON");
            strXML = strXML.substring(b);
            /* System.out.println("Substring of con ::"+strXML); */
            logger.info("SUBSTRING OF CON ::" + strXML);

        } else if (strXML.contains("MSG^`!")) {
            int c = strXML.indexOf("MSG");
            strXML = strXML.substring(c);
            /* System.out.println("Substring of msg ::"+strXML); */
            logger.info("SUBSTRING OF MSG ::" + strXML);

        } else if (strXML.contains("EXT^`!")) {
            int d = strXML.indexOf("EXT");
            strXML = strXML.substring(d);
            /* System.out.println("Substring of ext ::"+s); */
            logger.info("SUBSTRING OF EXT ::" + strXML);

        } else if (strXML.contains("KEEPALIVE^`!")) {
            int e = strXML.indexOf("KEEPALIVE");
            strXML = strXML.substring(e);
            /* System.out.println("Substring of KEEPALIVE ::"+s); */
            logger.info("SUBSTRING OF KEEPALIVE ::" + strXML);

        } else {
            logger.info("IN ELSE PART OF CHEKING SEPARATE STRING ::" + strXML);
        }

        /* ********************************************************************************/

        logger.info("BEFORE IF ::" + strXML);

        if (strXML.substring(0, 6).equalsIgnoreCase("CON^`!")
                || strXML.substring(0, 6).equalsIgnoreCase("MSG^`!")
                || strXML.substring(0, 6).equalsIgnoreCase("EXT^`!")
                || strXML.substring(0, 12).equalsIgnoreCase("KEEPALIVE^`!")) {

            if (strXML.substring(strXML.length() - 5).equalsIgnoreCase("#END#")) {
                lstVal.add(0, "SUCCESS");
                lstVal.add(1, strXML);
                return lstVal;
            }

            if (strXML.lastIndexOf("#END#") != -1) {
                lstVal.add(0, "SUCCESS");
                strXML = strXML.substring(0, strXML.lastIndexOf("#END#") + 5);
                lstVal.add(1, strXML);
                return lstVal;
            }

        } else {
            logger.info("XML PACKET IN ELSE " + strXML);
            logger.error("Error in start part of message");
            logger.error(strXML);
            writeInChannel("ACK!111", cnl);
            String localAddress = cnl.localAddress().toString();
            errorDump("", "", "", strXML, GlobalFunc.getDBDateTime(), "Error in start part of message",
                    localAddress.substring(localAddress.indexOf(":") + 1),
                    localAddress.substring(1, localAddress.indexOf(":")), this.sourceIP, this.sourcePort);
        }

        return lstVal;

        /* ********************************************************************************/

    } else {
        logger.info("STRING STARTING WITH MSG,EXT,KEEPALIVE,CON NOT FOUND ");
    }

    return lstVal;
}

private boolean writeInChannel(String strVal, Channel channel) {

    try {
        // GlobalFunc.smsReqResWrite("Response:"+strVal.toString());
        if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
            logger.info("Response:" + strVal.toString());

        if (channel != null && channel.isWritable()) {
            ByteBuf time = Unpooled.buffer(strVal.length());
            time.writeBytes(strVal.getBytes());
            channel.write(time);
            channel.flush();
        }
    } catch (Exception e) {
        logger.error("Channel writting exception::" + e);
    }
    /*
     * finally {
     *     buf.clear();
     * }
     */

    return true;
}

@Override
public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
    // Close the connection when an exception is raised.
    // buf.clear();
    /*
     * logger.log(Priority.WARN, "Unexpected exception from downstream.",
     * e.getCause());
     */
    logger.log(Level.WARN, "Unexpected exception from downstream.", e.getCause());
    writeInChannel("ACK!111", ctx.channel());
    ctx.channel().close();
}

private String checkGetValue(Node ndVal) {
    // long s1 = System.currentTimeMillis();
    // String ss = (ndVal == null ? "" : ndVal.getTextContent().trim());
    /*
     * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
     *     logger.info("checkGetValue:" + (System.currentTimeMillis() - s1));
     */
    return (ndVal == null ? "" : ndVal.getTextContent().trim());
}

private String getFirstTagValue(Document doc, String... tagNames) {
    for (String tagName : tagNames) {
        String value = checkGetValue(doc.getElementsByTagName(tagName).item(0));
        if (!value.equals("")) {
            return value;
        }
    }
    return "";
}

private String getTemplateMessage(String templateId, String templateValues) throws Exception {
    Connection con = null;
    CallableStatement st = null;
    ResultSet rs = null;

    try {
        con = this.dbpool.getConnection();
        st = con.prepareCall("{call USP_GET_SMS_TEMPLATE_DETAILS(?,?)}");
        st.setInt(1, Integer.parseInt(templateId));
        st.setString(2, templateValues == null ? "" : templateValues);

        rs = st.executeQuery();
        if (rs.next()) {
            return rs.getString("Message") == null ? "" : rs.getString("Message").trim();
        }
        return "";
    } finally {
        try {
            if (rs != null)
                rs.close();
            if (st != null)
                st.close();
            if (con != null)
                this.dbpool.releaseConnection(con);
        } catch (Exception e) {
            logger.error("Error while closing template procedure resources", e);
        }
    }
}

private String getLnrIP(Channel channel) {
    try {
        String localAddress = channel.localAddress().toString();
        return localAddress.substring(1, localAddress.indexOf(":"));
    } catch (Exception e) {
        logger.error("Error in getIP", e);
    }
    return null;
}

private String getLnrPort(Channel channel) {
    try {
        String localAddress = channel.localAddress().toString();
        return localAddress.substring(localAddress.indexOf(":") + 1);
    } catch (Exception e) {
        logger.error("Error in getIP", e);
    }
    return null;
}

private String getPipeID() {

    int pipeCount;

    if (messagePriority == 1) {
        SMSIdleServer._nPipeCount.getAndIncrement();
        if (SMSIdleServer._nPipeCount.get() < SMSIdleServer._nPriorityMinPipe
                || SMSIdleServer._nPipeCount.get() > SMSIdleServer._nPriorityMaxPipe) {
            SMSIdleServer._nPipeCount.set(SMSIdleServer._nPriorityMinPipe);
        }
        pipeCount = SMSIdleServer._nPipeCount.get();

    } else {
        SMSIdleServer._nNonPrioPipeCount.getAndIncrement();
        if (SMSIdleServer._nNonPrioPipeCount.get() < SMSIdleServer._nNonPriorityMinPipe
                || SMSIdleServer._nNonPrioPipeCount.get() > SMSIdleServer._nNonPriorityMaxPipe) {
            SMSIdleServer._nNonPrioPipeCount.set(SMSIdleServer._nNonPriorityMinPipe);
        }
        pipeCount = SMSIdleServer._nNonPrioPipeCount.get();
    }

    return String.valueOf(pipeCount);
}

private String getPnPipeID(String appId, String deptId) {

    logger.info("inside the getPnPipeID");
    logger.info("category value is::::" + pnCheck.get(deptId + "-" + appId));

    int pipeCount = 0;

    if (pnCheck.get(deptId + "-" + appId) != null && pnCheck.get(deptId + "-" + appId).equalsIgnoreCase("Y")) {
        logger.info("======push notification match for APP_ID:" + appId + " DEPT_ID:" + deptId
                + " Flag values is:" + pnCheck.get(deptId + "" + appId));
        SMSIdleServer._nPipePnCount.getAndIncrement();
        if (SMSIdleServer._nPipePnCount.get() < SMSIdleServer._nPriorityMinPipePn
                || SMSIdleServer._nPipePnCount.get() > SMSIdleServer._nPriorityMaxPipePn) {
            SMSIdleServer._nPipePnCount.set(SMSIdleServer._nPriorityMinPipePn);
        }
        pipeCount = SMSIdleServer._nPipePnCount.get();
    }

    return String.valueOf(pipeCount);
}

private String getResponse(String strReq, String localAddress, Channel cnl) {

    Document doc = null;
    DocumentBuilderFactory docBuilderFactory = null;
    DocumentBuilder docBuilder = null;
    Connection con = null;
    CallableStatement st = null;
    StringReader strReader = null;
    StringBuffer sbRes = new StringBuffer();
    sbRes.append("ACK!");

    try {
        strReq = replaceUnWantedCharXML(strReq);
        // logger.info("strReq:"+strReq);
        /*
         * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
         *     logger.info("XML DOM Start:" + System.currentTimeMillis());
         */

        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilder = docBuilderFactory.newDocumentBuilder();
        strReader = new StringReader(strReq);
        doc = docBuilder.parse(new InputSource(strReader));

        String DEPT                  = checkGetValue(doc.getElementsByTagName("DEPT").item(0));
        String APPID                 = checkGetValue(doc.getElementsByTagName("APPID").item(0));
        String DEPTMSGID             = checkGetValue(doc.getElementsByTagName("DEPTMSGID").item(0));
        String MOBILE                = checkGetValue(doc.getElementsByTagName("MOBILE").item(0));
        String MESSAGE               = checkGetValue(doc.getElementsByTagName("MESSAGE").item(0));
        String TEMPLATE_ID           = getFirstTagValue(doc, "TEMPLATE_ID", "TEMPLATEID");
        String TEMPLATE_VALUES       = getFirstTagValue(doc, "TEMPLATE_VALUES", "TEMPLATEVALUE", "VALUES");
        String FROMDATETIME          = checkGetValue(doc.getElementsByTagName("FROMDATETIME").item(0));
        String TODATETIME            = checkGetValue(doc.getElementsByTagName("TODATETIME").item(0));
        String NODELIVERYTIMEFROM    = checkGetValue(doc.getElementsByTagName("NODELIVERYTIMEFROM").item(0));
        String NODELIVERYTIMETO      = checkGetValue(doc.getElementsByTagName("NODELIVERYTIMETO").item(0));
        String HTTPMODE              = checkGetValue(doc.getElementsByTagName("HTTPMODE").item(0));
        String TRN_GENERATE_TIMESTAMP = checkGetValue(doc.getElementsByTagName("TRN_GENERATE_TIMESTAMP").item(0));
        String info1                 = checkGetValue(doc.getElementsByTagName("REMARKS").item(0));
        String info2                 = checkGetValue(doc.getElementsByTagName("REMARKS1").item(0));
        String info3                 = checkGetValue(doc.getElementsByTagName("REMARKS2").item(0));
        String info4                 = checkGetValue(doc.getElementsByTagName("REMARKS3").item(0));
        String dupchk                = checkGetValue(doc.getElementsByTagName("DUPLICATE_CHECK").item(0));
        // changes by rahul on date -09-09-2019
        String alt_channel           = checkGetValue(doc.getElementsByTagName("ALT_CHANNEL").item(0));
        String popSenderAdd          = checkGetValue(doc.getElementsByTagName("POP_SENDER_ADDR").item(0));

        /*
         * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
         *     logger.info("XML DOM End:" + System.currentTimeMillis());
         */

        if (info4.trim().equals(""))
            info4 = "JAVA";
        else
            info4 += " JAVA";

        // long start = 0;

        try {
            String strError = "ACK!MSGSTATUS=" + "FALSE" + ";FATAL=" + "TRUE" + ";DEPT=" + DEPT + ";APPID=" + APPID
                    + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2 + ";INFO3=" + info3
                    + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP=" + TRN_GENERATE_TIMESTAMP + ";MOBILE=" + MOBILE
                    + ";ISGMSGID=;";

            if (!TEMPLATE_ID.equals("")) {
                try {
                    MESSAGE = getTemplateMessage(TEMPLATE_ID, TEMPLATE_VALUES);
                    logger.info("Resolved MESSAGE from template id " + TEMPLATE_ID + " for source_IP " + sourceIP);
                } catch (NumberFormatException e) {
                    logger.info("ERROR=Template Id is not numeric for source_IP " + sourceIP);
                    return strError + "ERROR=Template Id is not numeric" + SeperatorString;
                } catch (Exception e) {
                    logger.error("Error while resolving template message", e);
                    return strError + "ERROR=Unable to resolve template message" + SeperatorString;
                }

                if (MESSAGE.equalsIgnoreCase("Template not found")
                        || MESSAGE.equalsIgnoreCase("Template mismatched")) {
                    logger.info("ERROR=" + MESSAGE + " for source_IP " + sourceIP);
                    return strError + "ERROR=" + MESSAGE + SeperatorString;
                }
            }

            // TODO 26102015
            if (DEPT.equals("")) {
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Department Id is blank",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Department Id is blank for source_IP " + sourceIP);
                return strError + "ERROR=Department Id is blank" + SeperatorString;

            } else if (APPID.equals("")) {
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Application is blank",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Application Id is blank for source_IP " + sourceIP);
                return strError + "ERROR=Application Id is blank" + SeperatorString;

            } else if (!lstVD.contains((DEPT + "-" + APPID))) {
                // if DEPT and APPID is not present in lstVD, add error dump
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Department id is disabled",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Department id is disabled for source_IP " + sourceIP);
                return strError + "ERROR=Department id is disabled" + SeperatorString;

            } else if (MESSAGE.equals("")) {
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Message Text is blank",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Message Text is blank for source_IP " + sourceIP);
                return strError + "ERROR=Message Text is blank" + SeperatorString;

            } else if (MESSAGE.length() > 500) {
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Message length cannot be greater than 500 characters",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Message length cannot be greater than 500 characters for source_IP " + sourceIP);
                return strError + "ERROR=Message length cannot be greater than 500 characters" + SeperatorString;

            } else if (MOBILE.equals("")) {
                // errorDump(
                //         DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                //         "ERROR=Mobile number is blank",
                //         localAddress.substring(localAddress.indexOf(":") + 1),
                //         localAddress.substring(1, localAddress.indexOf(":")),
                //         sourceIP, sourcePort);
                logger.info("ERROR=Mobile number is blank for source_IP " + sourceIP);
                return strError + "ERROR=Mobile number is blank" + SeperatorString;
            }

            String[] strArrMob = null;
            String strMob = MOBILE;

            if (strMob.indexOf(",") != -1) {
                strArrMob = strMob.split("\\,");
            } else {
                strArrMob = new String[1];
                strArrMob[0] = strMob;
            }

            for (int i = 0; i < strArrMob.length; i++) {
                strArrMob[i] = replaceInMobile(strArrMob[i]);

                if (strArrMob[i].equals("")) {
                    // errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                    //         "ERROR=Mobile number is blank",
                    //         localAddress.substring(localAddress.indexOf(":") + 1),
                    //         localAddress.substring(1, localAddress.indexOf(":")),
                    //         sourceIP, sourcePort);
                    logger.info("ERROR=Mobile number is blank for source_IP " + sourceIP);
                    return strError + "ERROR=Mobile number is blank" + SeperatorString;

                } else if (strArrMob[i].length() < 10 || strArrMob[i].length() > 15) {
                    // errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                    //         "ERROR=Mobile number is not valid",
                    //         localAddress.substring(localAddress.indexOf(":") + 1),
                    //         localAddress.substring(1, localAddress.indexOf(":")),
                    //         sourceIP, sourcePort);
                    logger.info("ERROR=Mobile number is not valid for source_IP " + sourceIP);
                    return strError + "ERROR=Mobile number is not valid" + SeperatorString;

                } else if (lstIM.contains(strArrMob[i])) {
                    // errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                    //         "ERROR=Mobile number is not valid",
                    //         localAddress.substring(localAddress.indexOf(":") + 1),
                    //         localAddress.substring(1, localAddress.indexOf(":")),
                    //         sourceIP, sourcePort);
                    logger.info("ERROR=Mobile number is invalid for source_IP " + sourceIP);
                    return strError + "ERROR=Mobile number is invalid" + SeperatorString;

                } else {
                    Scanner sc = new Scanner(strArrMob[i]);
                    if (!sc.hasNextBigInteger()) {
                        // errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                        //         "ERROR=Mobile number is not numeric",
                        //         localAddress.substring(localAddress.indexOf(":") + 1),
                        //         localAddress.substring(1, localAddress.indexOf(":")),
                        //         sourceIP, sourcePort);
                        logger.info("ERROR=Mobile number is not numeric for source_IP " + sourceIP);
                        return strError + "ERROR=Mobile number is not numeric" + SeperatorString;
                    }
                }
            }

            // MESSAGE = RemoveSpecialChars(MESSAGE);
            /*
             * else if (FN_CheckSpecialChars(MESSAGE)) {
             *     return strError + "ERROR=Message text contain special characters" + SeperatorString;
             * }
             */

            // TODO Changes for getting priority
            String reglstStr[];
            for (String reglst : lpsd) {
                reglstStr = reglst.split("\\|");
                if (reglstStr[0].contains(DEPT + "-" + APPID)) {
                    messagePriority = Integer.valueOf(reglstStr[1]);
                    break;
                }
            }

            // As discussed with Manesh Sir on 20 April 2026 for uniquely generate Message ID on java
            // Develop BY Shubham Salunkhe By comment down the Procedure call
            con = this.dbpool.getConnection();
            st = con.prepareCall(
                    "{call USP_INSERTONLY_PUSH_SMS_JAVA_DUPCHK(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

            // start = System.currentTimeMillis();

            for (int i = 0; i < strArrMob.length; i++) {
                /*
                 * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                 *     logger.info("Pipe ID True Start:" + System.currentTimeMillis());
                 */

                nPipeId   = getPipeID();
                nPipePnId = getPnPipeID(APPID, DEPT);

                /* logger.info("======pipe id for push notification is==="+nPipePnId); */
                /*
                 * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                 *     logger.info("Pipe ID True End:" + System.currentTimeMillis());
                 */

                try {
                    /*
                     * if ((checkGetValue(DEPTMSGID) + "-" + replaceInMobile(strArrMob[i]))
                     *         .equals(SMSIdleServer.preMsgIDMob)) {
                     *     sbRes.append("MSGSTATUS=" + "false".toString().toUpperCase() + ";FATAL="
                     *             + "true".toString().toUpperCase() + ";DEPT=" + checkGetValue(DEPT)
                     *             + ";APPID=" + checkGetValue(APPID) + ";DEPTMSGID=" + checkGetValue(DEPTMSGID)
                     *             + ";INFO1=" + checkGetValue(info1) + ";INFO2=" + checkGetValue(info2)
                     *             + ";INFO3=" + checkGetValue(info3) + ";INFO4=" + strInfo4
                     *             + ";TRN_GENERATE_TIMESTAMP=" + checkGetValue(TRN_GENERATE_TIMESTAMP)
                     *             + ";MOBILE=" + replaceInMobile(strArrMob[i]) + ";ISGMSGID=;ERROR=Duplicate Message");
                     *     sbRes.append(SeperatorString);
                     *     errorDump(checkGetValue(DEPT), checkGetValue(APPID), checkGetValue(DEPTMSGID),
                     *             strReq, GlobalFunc.getDBDateTime(), "Duplicate Messages",
                     *             localAddress.substring(localAddress.indexOf(":") + 1),
                     *             localAddress.substring(1, localAddress.indexOf(":")));
                     * } else {
                     *     SMSIdleServer.preMsgIDMob = checkGetValue(DEPTMSGID) + "-" + replaceInMobile(strArrMob[i]);
                     */

//                  st.setString(1,  DEPT);
//                  st.setString(2,  APPID);
//                  st.setString(3,  null);          // added by sonu punia for source ip
//                  st.setString(4,  popSenderAdd);  // added by sonu punia for source modify by rahul
//                  // port
//                  st.setString(5,  DEPTMSGID);
//                  st.setString(6,  null);
//                  st.setString(7,  strArrMob[i]);
//                  st.setString(8,  MESSAGE);
//                  st.setString(9,  FROMDATETIME);
//                  st.setString(10, TODATETIME);
//                  st.setString(11, null);
//                  st.setString(12, alt_channel);
//                  st.setString(13, null);
//                  st.setString(14, null);
//                  st.setString(15, null);
//                  st.setString(16, NODELIVERYTIMEFROM);
//                  st.setString(17, NODELIVERYTIMETO);
//                  st.setString(18, null);
//                  st.setString(19, HTTPMODE);
//                  st.setString(20, info1);
//                  st.setString(21, info2);
//                  st.setString(22, info3);
//                  st.setString(23, info4);
//                  st.setString(24, TRN_GENERATE_TIMESTAMP);
//                  st.setString(25, null);
//                  st.setString(26, null);
//                  st.setString(27, null);
//                  st.setString(28, null);
//                  st.setString(29, null);
//                  st.setString(30, null);
//                  st.setString(31, null);
//                  st.setString(32, null);
//                  st.setString(33, localPort);
//                  st.setString(34, localIP);
//                  st.setString(35, strReq);
//                  st.setString(36, nPipeId);
//                  st.setString(37, dupchk);
//                  st.setString(38, sourceIP); // added by sonu punia for source ip
//                  st.setString(39, null);     // added by sonu punia for source port
//                  st.setString(40, nPipePnId); // added by rahul patel for push notification
//                  st.registerOutParameter(41, Types.NUMERIC);
//                  st.registerOutParameter(42, Types.VARCHAR);
//                  st.executeUpdate();

                    long pmsgid = 0;

                    // As discussed with Manesh Sir on 20 April 2026 for uniquely generate Message ID on java
                    // Develop BY Shubham Salunkhe
//                  pmsgid = st.getLong(41);
                    pmsgid = (long) generate9DigitKey(localPort);
//                  UniqueIdGenerator gen = new UniqueIdGenerator();
//                  pmsgid = (Long.parseLong(localPort) * 1000000000000L) + gen.generateId();
//                  pmsgid = Long.parseLong(localPort + String.valueOf(gen.generateId()).substring(4));
//                  logger.info("pmsgid 22==" + pmsgid);
                    // sbRes.append(String.valueOf(pmsgid));

                    // AS discussed with manesh on 23-May-2026 doing development for Time Based Message Processing
                    logger.info("FROMDATETIME=" + FROMDATETIME + "=TODATETIME=" + TODATETIME);

                    if (!FROMDATETIME.equals("") && !TODATETIME.equals("")) {
                        logger.info("Inside the time based Functionality");
                        st.setString(1,  DEPT);
                        st.setString(2,  APPID);
                        st.setString(3,  null);          // added by sonu punia for source ip
                        st.setString(4,  popSenderAdd);  // added by sonu punia for source modify by rahul
                        // port
                        st.setString(5,  DEPTMSGID);
                        st.setString(6,  null);
                        st.setString(7,  strArrMob[i]);
                        st.setString(8,  MESSAGE);
                        st.setString(9,  FROMDATETIME);
                        st.setString(10, TODATETIME);
                        st.setString(11, null);
                        st.setString(12, alt_channel);
                        st.setString(13, null);
                        st.setString(14, null);
                        st.setString(15, null);
                        st.setString(16, NODELIVERYTIMEFROM);
                        st.setString(17, NODELIVERYTIMETO);
                        st.setString(18, null);
                        st.setString(19, HTTPMODE);
                        st.setString(20, info1);
                        st.setString(21, info2);
                        st.setString(22, info3);
                        st.setString(23, info4);
                        st.setString(24, TRN_GENERATE_TIMESTAMP);
                        st.setString(25, null);
                        st.setString(26, null);
                        st.setString(27, null);
                        st.setString(28, null);
                        st.setString(29, null);
                        st.setString(30, null);
                        st.setString(31, null);
                        st.setString(32, null);
                        st.setString(33, localPort);
                        st.setString(34, localIP);
                        st.setString(35, strReq);
//                      st.setString(36, nPipeId);
                        st.setString(36, "11");
                        st.setString(37, dupchk);
                        st.setString(38, sourceIP); // added by sonu punia for source ip
//
                        st.setString(39, null);     // added by sonu punia for source port
                        st.setString(40, nPipePnId); // added by rahul patel for push notification
                        st.registerOutParameter(41, Types.NUMERIC);
                        st.registerOutParameter(42, Types.VARCHAR);
                        logger.info("Procedure call=" + st.executeUpdate());
//                      st.executeUpdate();
                        logger.info("st.getLong(41)=" + st.getLong(41));
                        logger.info("st.getLong(42)=" + st.getString(42));
                    }

                    if (pmsgid > 0) {
                        String SendPNmessageflag = this.prop.getProperty("SendPNmessageFlag");
                        logger.info("SendPNmessageflag :: " + SendPNmessageflag);

                        // Adding New Development for SMS Data Send on Kafka
                        // Discussed with Manesh Sir Change Done By Shubham SALUNKHE 01-04-2026
                        String categoryK = pnCheck.get(DEPT + "-" + APPID);
                        logger.info("category==" + categoryK);
                        // logger.info("inside th if 232323311111111==");

                        if (!(!FROMDATETIME.equals("") && !TODATETIME.equals(""))) {
                            logger.info("inside the if condition 111");

                            if (categoryK.toUpperCase().equals("K")) {
                                // logger.info("inside the if condition111");
                                try {
//                                  KafkaResponseData kafkaResponseData = new KafkaResponseData();
//                                  kafkaResponseData.setMSGID(String.valueOf(pmsgid));
//                                  kafkaResponseData.setDEPT(DEPT);
//                                  kafkaResponseData.setAPPID(APPID);
//                                  kafkaResponseData.setMOBILE(MOBILE);
//                                  kafkaResponseData.setDEPTMSGID(DEPTMSGID);
//                                  kafkaResponseData.setMESSAGE(MESSAGE);
//                                  kafkaResponseData.setFROMDATETIME(FROMDATETIME);
//                                  kafkaResponseData.setTODATETIME(TODATETIME);
//                                  kafkaResponseData.setNODELIVERYTIMEFROM(NODELIVERYTIMEFROM);
//                                  kafkaResponseData.setNODELIVERYTTIMETO(NODELIVERYTIMETO);
//                                  kafkaResponseData.setHTTPMODE(HTTPMODE);
//                                  kafkaResponseData.setREMARK(info1);
//                                  kafkaResponseData.setTRN_GENERATE_TIMESTAMP(TRN_GENERATE_TIMESTAMP);
//                                  kafkaResponseData.setDUPLICATE_CHECK(dupchk);
//                                  kafkaResponseData.setREMARKS1(info2);
//                                  kafkaResponseData.setREMARKS2(info3);
//                                  kafkaResponseData.setTOIPC("smsgw.request");

                                    KafkaDataSend kafkaResponseData = new KafkaDataSend();
                                    kafkaResponseData.setMsg_id(String.valueOf(pmsgid));
                                    kafkaResponseData.setDept(DEPT);
                                    kafkaResponseData.setAppid(APPID);
                                    kafkaResponseData.setMobile(MOBILE);
                                    kafkaResponseData.setDeptmsgid(DEPTMSGID);
                                    kafkaResponseData.setMessage(MESSAGE);
                                    kafkaResponseData.setFromdatetime(FROMDATETIME);
                                    kafkaResponseData.setTodatetime(TODATETIME);
                                    kafkaResponseData.setNodeliverytimefrom(NODELIVERYTIMEFROM);
                                    kafkaResponseData.setNodeliverytimeto(NODELIVERYTIMETO);
                                    kafkaResponseData.setHttpmode(HTTPMODE);
                                    kafkaResponseData.setRemarks(info1);
                                    kafkaResponseData.setTrn__generate_timestamp(TRN_GENERATE_TIMESTAMP);
                                    kafkaResponseData.setDuplicate_check(dupchk);
                                    kafkaResponseData.setRemarks1(info2);
                                    kafkaResponseData.setRemarks2(info3);
                                    kafkaResponseData.setTopic_name("smsgw.request");

//                                  kafkaResponseData.setMsg_id(popSenderAdd);
//                                  kafkaResponseData.setDept(DEPT);
//                                  kafkaResponseData.setAppid(APPID);
//                                  kafkaResponseData.setMobile(MOBILE);
//                                  kafkaResponseData.setDeptmsgid(DEPTMSGID);
//                                  kafkaResponseData.setMessage(MESSAGE);
//                                  kafkaResponseData.setFromdatetime(FROMDATETIME);
//                                  kafkaResponseData.setTodatetime(TODATETIME);
//                                  kafkaResponseData.setNodeliverytimefrom(NODELIVERYTIMEFROM);
//                                  kafkaResponseData.setNodeliverytimeto(NODELIVERYTIMETO);
//                                  kafkaResponseData.setHttpmode(HTTPMODE);
//                                  kafkaResponseData.setRemarks(info1);
//                                  kafkaResponseData.setTrn_generate_timestamp(TRN_GENERATE_TIMESTAMP);
//                                  kafkaResponseData.setDuplicate_check(dupchk);
//                                  kafkaResponseData.setRemarks1(info2);
//                                  kafkaResponseData.setRemarks2(info3);
//                                  JSONObject kafkaRequestBody = new JSONObject();
//                                  kafkaRequestBody.put("msg_id",                 pmsgid);
//                                  kafkaRequestBody.put("dept",                   DEPT);
//                                  kafkaRequestBody.put("appid",                  APPID);
//                                  kafkaRequestBody.put("mobile",                 MOBILE);
//                                  kafkaRequestBody.put("deptmsgid",              DEPTMSGID);
//                                  kafkaRequestBody.put("message",                MESSAGE);
//                                  kafkaRequestBody.put("fromdatetime",           FROMDATETIME);
//                                  kafkaRequestBody.put("todatetime",             TODATETIME);
//                                  kafkaRequestBody.put("nodeliverytimefrom",     NODELIVERYTIMEFROM);
//                                  kafkaRequestBody.put("nodeliverytimeto",       NODELIVERYTIMETO);
//                                  kafkaRequestBody.put("httpmode",               HTTPMODE);
//                                  kafkaRequestBody.put("remarks",                info1);
//                                  kafkaRequestBody.put("trn__generate_timestamp", TRN_GENERATE_TIMESTAMP);
//                                  kafkaRequestBody.put("duplicate_check",        dupchk);
//                                  kafkaRequestBody.put("remarks1",               info2);
//                                  kafkaRequestBody.put("remarks2",               info3);
//                                  kafkaRequestBody.put("topic_name",             "SMSGE.REQUEST");
//                                  kafkaResponseData.setTopic_name("SMSGW.REQUEST");

                                    logger.info("Before the kafkaPreoducer.kafkaProcuder");
                                    KafkaConfig kafkaPreoducer = new KafkaConfig();
                                    kafkaPreoducer.kafkaProcuder(kafkaResponseData);
                                    logger.info("After the kafkaPreoducer.kafkaProcuder");

                                } catch (Exception ex) {
                                    logger.error("Error Occurred During Kafka Implementation=", ex);
                                }
                            }
                        }

                        if ("true".equals(SendPNmessageflag)) {
                            try {
                                String category = pnCheck.get(DEPT + "-" + APPID);
                                logger.info("category ::" + category + " mobile no::" + MOBILE);

                                if ("Y".equalsIgnoreCase(category)) {
                                    String PNMessageURL = this.prop.getProperty("PNMessageURL");
                                    logger.info("PNMessageURL::" + PNMessageURL);

                                    URL url = new URL(PNMessageURL);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Content-Type", "application/json");
                                    conn.setConnectTimeout(6000);  // set connection time out
                                    conn.setReadTimeout(10000);    // set read time out
                                    conn.setDoOutput(true);

                                    JSONObject requestBody = new JSONObject();
                                    requestBody.put("MSG_ID",                pmsgid);
                                    requestBody.put("DEPTID",                DEPT);
                                    requestBody.put("APPID",                 APPID);
                                    requestBody.put("MOBILE_NO",             MOBILE);
                                    requestBody.put("DEPTMSGID",             DEPTMSGID);
                                    requestBody.put("MESSAGE",               MESSAGE);
                                    requestBody.put("FROMDATETIME",          FROMDATETIME);
                                    requestBody.put("TODATETIME",            TODATETIME);
                                    requestBody.put("NODELIVERYTIMEFROM",    NODELIVERYTIMEFROM);
                                    requestBody.put("NODELIVERYTIMETO",      NODELIVERYTIMETO);
                                    requestBody.put("HTTPMODE",              HTTPMODE);
                                    requestBody.put("REMARKS",               info1);
                                    requestBody.put("DUPLICATE_CHECK",       dupchk);
                                    requestBody.put("ALT_CHANNEL",           alt_channel);
                                    requestBody.put("TRN_GENERATE_TIMESTAMP", TRN_GENERATE_TIMESTAMP);
                                    requestBody.put("REMARKS1",              info2);
                                    requestBody.put("REMARKS2",              info3);

                                    String jsonString = requestBody.toString().replaceAll("\\\\", "");
                                    logger.info("jsonString request ::" + jsonString);

                                    DataOutputStream wr = null;
                                    // try (OutputStream os = conn.getOutputStream()) {
                                    wr = new DataOutputStream(conn.getOutputStream());
                                    wr.writeBytes(jsonString);
                                    wr.flush();

                                    int responseCode = conn.getResponseCode();
                                    logger.info("responseCode ::" + responseCode);

                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        BufferedReader in = new BufferedReader(
                                                new InputStreamReader(conn.getInputStream()));
                                        String inputLine;
                                        StringBuilder response = new StringBuilder();
                                        while ((inputLine = in.readLine()) != null) {
                                            response.append(inputLine);
                                        }
                                        in.close();
                                        logger.info("response msg deatils :: " + response.toString());
                                    } else {
                                        logger.info("request failed with http status code: " + responseCode);
                                    }

                                    wr.close();
                                    conn.disconnect();
                                }

                            } catch (Exception e) {
                                logger.error("exception details :: " + e.getMessage());
                            }
                        }

                        // logger.info("request after if condition");
                        sbRes.append("MSGSTATUS=" + "TRUE" + ";FATAL=" + "FALSE" + ";DEPT=" + DEPT + ";APPID="
                                + APPID + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2
                                + ";INFO3=" + info3 + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP="
                                + TRN_GENERATE_TIMESTAMP + ";MOBILE=" + strArrMob[i] + ";ISGMSGID=" + pmsgid
                                + ";ERROR=" + this.errorMsg);
                        sbRes.append(SeperatorString);

                    } else {
                        /*
                         * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                         *     logger.info("Pipe ID False Start:" + System.currentTimeMillis());
                         */
                        // getPipeID(false);
                        /*
                         * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                         *     logger.info("Pipe ID False End:" + System.currentTimeMillis());
                         */

                        String errMsg = st.getString(42);
                        if (errMsg != null && errMsg.trim().indexOf("Duplicate Message for") != -1) {
                            sbRes.append("MSGSTATUS=" + "TRUE" + ";FATAL=" + "TRUE" + ";DEPT=" + DEPT + ";APPID="
                                    + APPID + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2
                                    + ";INFO3=" + info3 + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP="
                                    + TRN_GENERATE_TIMESTAMP + ";MOBILE=" + strArrMob[i] + ";ISGMSGID=;ERROR="
                                    + st.getString(42));
                            sbRes.append(SeperatorString);
                        } else {
                            sbRes.append("MSGSTATUS=" + "FALSE" + ";FATAL=" + "TRUE" + ";DEPT=" + DEPT + ";APPID="
                                    + APPID + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2
                                    + ";INFO3=" + info3 + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP="
                                    + TRN_GENERATE_TIMESTAMP + ";MOBILE=" + strArrMob[i] + ";ISGMSGID=;ERROR="
                                    + st.getString(42));
                            sbRes.append(SeperatorString);
                        }
                    }

                    // }

                } catch (SQLException ex) {
                    /*
                     * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                     *     logger.info("Pipe ID False Start:" + System.currentTimeMillis());
                     */
                    // getPipeID(false);
                    /*
                     * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y"))
                     *     logger.info("Pipe ID False End:" + System.currentTimeMillis());
                     */
                    throw ex;

                } catch (Exception e) {
                    logger.error("Error :=> ", e);
                    this.errorMsg = e.getMessage();
                    sbRes.append("MSGSTATUS=" + "FALSE" + ";FATAL=" + "TRUE" + ";DEPT=" + DEPT + ";APPID=" + APPID
                            + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2 + ";INFO3=" + info3
                            + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP=" + TRN_GENERATE_TIMESTAMP + ";MOBILE="
                            + strArrMob[i] + ";ISGMSGID=;ERROR=" + this.errorMsg);
                    sbRes.append(SeperatorString);
                    errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                            "Error in messageReceived::" + e.getMessage(),
                            localAddress.substring(localAddress.indexOf(":") + 1),
                            localAddress.substring(1, localAddress.indexOf(":")), sourceIP, sourcePort);
                }

                if (i < strArrMob.length - 1) {
                    // logger.info(" RAW XML Response from SMS service " + i + "::" + sbRes.toString());
                    writeInChannel(sbRes.toString(), cnl);
                    sbRes = new StringBuffer();
                    sbRes.append("ACK!");
                }
            }

        } catch (Exception e) {
            /* logger.log(Priority.ERROR, e); */
            logger.log(Level.ERROR, e);
            this.errorMsg = e.getMessage();
            sbRes.append("MSGSTATUS=" + "FALSE" + ";FATAL=" + "TRUE" + ";DEPT=" + DEPT + ";APPID=" + APPID
                    + ";DEPTMSGID=" + DEPTMSGID + ";INFO1=" + info1 + ";INFO2=" + info2 + ";INFO3=" + info3
                    + ";INFO4=" + info4 + ";TRN_GENERATE_TIMESTAMP=" + TRN_GENERATE_TIMESTAMP + ";MOBILE=" + MOBILE
                    + ";ISGMSGID=;ERROR=" + this.errorMsg);
            sbRes.append(SeperatorString);
            errorDump(DEPT, APPID, DEPTMSGID, strReq, GlobalFunc.getDBDateTime(),
                    "Error in messageReceived::" + e.getMessage(),
                    localAddress.substring(localAddress.indexOf(":") + 1),
                    localAddress.substring(1, localAddress.indexOf(":")), sourceIP, sourcePort);

        } finally {
            try {
                if (st != null)
                    st.close();
                if (con != null)
                    this.dbpool.releaseConnection(con);
                // long end = System.currentTimeMillis();
                /*
                 * if (SMSIdleServer._strDebug.equalsIgnoreCase("Y")) {
                 *     logger.info("Conn Hold Start:: " + start);
                 *     logger.info("Conn Hold End:: "   + end);
                 * }
                 */
            } catch (Exception e) {
                logger.error("DB close on getResponse", e);
            }
        }

    } catch (Exception e) {
        sbRes.append("111-Error in parsing the message");
        logger.error(strReq + "\n" + sbRes.toString(), e);
        errorDump("", "", "", strReq, GlobalFunc.getDBDateTime(), "Error in messageReceived::" + e.getMessage(),
                localAddress.substring(localAddress.indexOf(":") + 1),
                localAddress.substring(1, localAddress.indexOf(":")), sourceIP, sourcePort);

    } finally {
        if (strReader != null) {
            strReader.close();
        }
    }

    return sbRes.toString();
}

private boolean FN_CheckSpecialChars(String message) {
    if (message.length() > 0) {
        int asciiNum = 0;
        for (int i = 0; i < message.length(); i++) {
            asciiNum = message.codePointAt(i);
            // !^/<>|
            if (asciiNum == 33
                    || asciiNum == 94
                    /* || asciiNum == 47 */
                    || asciiNum == 60
                    || asciiNum == 62
                    || asciiNum == 124
                    /* || asciiNum == 38 */) {
                return true;
            }
        }
    }
    return false;
}

private String RemoveSpecialChars(String message) {
    String valRet = "";
    if (message.length() > 0) {
        int asciiNum = 0;
        for (int i = 0; i < message.length(); i++) {
            asciiNum = message.codePointAt(i);
            if (asciiNum == 33
                    || asciiNum == 94
                    || asciiNum == 47
                    || asciiNum == 60
                    || asciiNum == 62
                    || asciiNum == 124
                    /* || asciiNum == 38 */) {
                continue;
            } else {
                valRet += message.charAt(i);
            }
        }
    } else {
        valRet = message;
    }
    return valRet;
}

private boolean errorDump(String messageDeptID, String messageAppID, String messageDeptRefMsgID,
        String rawMessageText, String ErrDtTime, String LastError,
        String ServerPort, String ServerIP, String sourceIP, String sourcePort) {

    Connection con = null;
    CallableStatement st = null;
    // StringBuffer sbRes = new StringBuffer();

    try {
        con = this.dbpool.getConnection();
        st = con.prepareCall("{call USP_LIST_SRVR_ERROR_i(?,?,?,?,?,?,?,?,?)}");
        st.setString(1, messageDeptID);
        st.setString(2, messageAppID);
        st.setString(3, messageDeptRefMsgID);
        st.setString(4, rawMessageText);
        st.setString(5, ErrDtTime);
        st.setString(6, LastError);
        st.setString(7, ServerPort);
        st.setString(8, ServerIP);
        st.setString(9, sourceIP);
        // st.setString(10, sourcePort);
        st.executeUpdate();

    } catch (Exception e) {
        // logger.log(Priority.ERROR, e);
        logger.error("Error :=>", e);
    } finally {
        try {
            if (st != null)
                st.close();
            if (con != null)
                this.dbpool.releaseConnection(con);
        } catch (Exception e) {
        }
    }

    return true;
}

public long generate9DigitKey(String localPort) {
    long uniqueKey = 0;
    try {
        // int id = 0, MACHINE_ID = 0;
        // MACHINE_ID = Math.abs(localPort.hashCode());
        long combined = (System.currentTimeMillis() ^ System.nanoTime()) ^ Math.abs(localPort.hashCode());
        // logger.info("combinedcombined=" + combined);
        uniqueKey = Integer.valueOf(localPort) + Math.abs((long) (combined % 1000000000L));
        logger.info("uniqueKey111=" + uniqueKey);
    } catch (Exception ex) {
        logger.error("Error Occuurred at generate9DigitKey ", ex);
    }
    return uniqueKey;
}

}
