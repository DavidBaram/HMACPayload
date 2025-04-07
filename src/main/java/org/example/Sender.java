/** Project: Solo Lab HMAC Payload
 * Purpose Details: Professor Oakes wants us to send a message via HMAC and recieve said message
 * Course: IST 242
 * Author: David Baram
 * Date Developed: 4/7/2025
 * Last Date Changed: 4/7/2025
 * Rev: 1.0
 */
package org.example;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
public class Sender {
    private static final String QUEUE_NAME = "secure_queue";
    private static final String SECRET_KEY = "secret";
    public static void main(String[] args) throws Exception {
        String message = "The quick brown fox jumps over the lazy dog";
        String hmac = generateHmacSHA256(message, SECRET_KEY);
        String fullPayload = message + "|" + hmac;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, fullPayload.getBytes());
            System.out.println("Sent Message: " + message);
            System.out.println("Sent HMAC:    " + hmac);
        }
    }
    private static String generateHmacSHA256(String message, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secret);
        byte[] hmacBytes = mac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
