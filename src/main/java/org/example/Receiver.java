/** Project: Solo Lab HMAC Payload
 * Purpose Details: Professor Oakes wants us to send a message via HMAC and recieve said message
 * Course: IST 242
 * Author: David Baram
 * Date Developed: 4/7/2025
 * Last Date Changed: 4/7/2025
 * Rev: 1.0
 */
package org.example;
import com.rabbitmq.client.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public class Receiver {
    private static final String QUEUE_NAME = "secure_queue";
    private static final String SECRET_KEY = "secret";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("Waiting for messages...");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String fullPayload = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String[] parts = fullPayload.split("\\|");
            String message = parts[0];
            String receivedHmac = parts[1];
            String generatedHmac = null;
            try {
                generatedHmac = generateHmacSHA256(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("Received Message: " + message);
            System.out.println("Received HMAC:   " + receivedHmac);
            System.out.println("Generated HMAC:  " + generatedHmac);
            if (generatedHmac.equals(receivedHmac)) {
                System.out.println("Success, the integrity verified");
            } else {
                System.out.println("ERROR, integrity failed!");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
    private static String generateHmacSHA256(String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(Receiver.SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secret);
        byte[] hmacBytes = mac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
