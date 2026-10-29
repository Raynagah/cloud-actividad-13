package com.kafka.consumidor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.kafka.consumidor.Repository.TelemetriaRepository;

import com.kafka.consumidor.Model.TelemetriaCamion;

import tools.jackson.databind.ObjectMapper;

@Component
public class ConsumidorComponent {
    @KafkaListener(topics = "tutorial-particiones", groupId = "prueba-group-1")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        System.out.println("\n" + "=".repeat(100));
        System.out.printf("Mensaje Recibido %n");
        System.out.printf("Particion %d%n", partition);
        System.out.printf("offset: %d%n", offset);
        System.out.printf("key: %s%n", key != null ? key : "sin key");
        System.out.printf("mensaje: %s%n", message);
        System.out.println("=".repeat(100) + "\n");
    }

    @Autowired
    private telemetriaRepository telemetriaRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "telemetria-camion", groupId = "prueba-group-1")
    public void telemetria(
            @Payload String message, // Recibimos el JSON crudo como String
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        try {
            TelemetriaCamion telemetria = objectMapper.readValue(message, TelemetriaCamion.class);

            System.out.println("Camión recibido: " + telemetria.getCamionId());
            System.out.println("Temperatura: " + telemetria.getTemperatura());

            telemetriaRepository.guardar(telemetria);
        } catch (Exception e) {
            System.err.println("Error al deserializar la telemetría del camión: " + e.getMessage());
        }
    }

}
