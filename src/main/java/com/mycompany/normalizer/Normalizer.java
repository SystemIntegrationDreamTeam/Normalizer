/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.normalizer;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import java.io.IOException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Buhrkall
 */
public class Normalizer {

    private final static String QUEUE_NAME = "NormalizerQueue";
    private final static String SENDING_QUEUE_NAME = "AggregatorExchange";
    private static boolean schoolBank;

    static Gson gson = new Gson();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        final Channel sendingChannel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        sendingChannel.exchangeDeclare(SENDING_QUEUE_NAME, "fanout");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                //HANDLE HERE
                if (message.startsWith("{")) {
                    // Transform with GSON

                    schoolBank = message.contains("-");

                    if (!schoolBank) {
                        message = message.replace("-", "");
                        JResponse jresponse = gson.fromJson(message, JResponse.class);
                        jresponse.setBank("cphbusiness.bankJSON");
                        message = gson.toJson(jresponse);

                    } else {
                        message = message.replace("-", "");
                        JResponse jresponse = gson.fromJson(message, JResponse.class);
                        jresponse.setBank("DreamTeamBankJSON");
                        message = gson.toJson(jresponse);
                    }

                    sendingChannel.basicPublish(SENDING_QUEUE_NAME, "", null, message.getBytes());

                } else {

                    schoolBank = message.contains("-");
                    String result = "";

                    if (!schoolBank) {
                        message = message.replace("-", "");
                        JSONObject soapDatainJsonObject = XML.toJSONObject(message);

                        result = gson.toJson(soapDatainJsonObject);
                        result = result.replace("{\"map\":{\"LoanResponse\":{\"map\":", "");
                        result = result.replace("}}}", "");

                        JResponse jresponse = gson.fromJson(result, JResponse.class);
                        jresponse.setBank("cphbusiness.bankXML");
                        result = gson.toJson(jresponse);

                    } else {
                        message = message.replace("-", "");
                        JSONObject soapDatainJsonObject = XML.toJSONObject(message);

                        result = gson.toJson(soapDatainJsonObject);
                        result = result.replace("{\"map\":{\"LoanResponse\":{\"map\":", "");
                        result = result.replace("}}}", "");

                        JResponse jresponse = gson.fromJson(result, JResponse.class);
                        jresponse.setBank("DreamTeamBankXML");

                        result = gson.toJson(jresponse);
                    }

                    //  XResponse response = gson.fromJson(soapDatainJsonObject, XResponse.class);
                    sendingChannel.basicPublish(SENDING_QUEUE_NAME, "", null, result.getBytes());

                }

            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

}
