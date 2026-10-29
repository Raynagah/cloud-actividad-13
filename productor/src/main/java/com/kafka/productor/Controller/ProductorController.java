package com.kafka.productor.Controller;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/kafka")
public class ProductorController {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ProductorController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/send")
    public String getMethodName(
            @RequestParam String message,
            @RequestParam(required = false) String key) {
        String topicName = "tutorial-particiones";
        if (key != null && !key.isEmpty()) {
            kafkaTemplate.send(topicName, key, message);
            return "Mensaje enviado al topic: " + topicName
                    + "con key: " + key
                    + "mensaje: " + message;
        } else {
            kafkaTemplate.send(topicName, message);
            return "Mensaje enviado al topic: " + topicName
                    + "con key: sin key"
                    + "mensaje: " + message;
        }
    }

    @PostMapping("/send")
    public String postMethodName(
            @RequestBody String message,
            @RequestParam(required = false) String key) {
        String topicName = " telemtria-camion ";

        if (key != null && !key.isEmpty()) {
            kafkaTemplate.send(topicName, key, message);
            return "Mensaje JSON enviado al topic: " + topicName
                    + " | con key: " + key
                    + " | mensaje: " + message;
        } else {
            kafkaTemplate.send(topicName, message);
            return "Mensaje JSON enviado al topic: " + topicName
                    + " | con key: sin key"
                    + " | mensaje: " + message;
        }
    }

    @PostMapping("/simular-camiones")
    public String simular50Mensajes() {
        String topicName = "telemtria-camion";
        String[] camiones = { "CAMION-001", "CAMION-002", "CAMION-003" };
        String[] estados = { "EN_RUTA", "DETENIDO", "REFRIGERANDO", "ALERTA_TEMPERATURA" };
        Random random = new Random();

        try {
            for (int i = 1; i <= 50; i++) {
                // Seleccionar un camión de forma rotativa o aleatoria entre los 3
                String camionId = camiones[random.nextInt(camiones.length)];

                // Generar valores aleatorios simulados
                double temperatura = -25.0 + (random.nextDouble() * 15.0); // Entre -25°C y -10°C
                double humedad = 50.0 + (random.nextDouble() * 30.0); // Entre 50% y 80%
                String estado = estados[random.nextInt(estados.length)];
                long timestamp = System.currentTimeMillis() + (i * 1000L); // Timestamps separados por 1 segundo

                TelemetriaCamion telemetria = new TelemetriaCamion();
                String id = UUID.randomUUID().toString().substring(0, 8);
                telemetria.setId(id);
                telemetria.setCamionId(camionId);
                telemetria.setTimestamp(timestamp);
                telemetria.setTemperatura(Math.round(temperatura * 100.0) / 100.0); // Redondear a 2 decimales
                telemetria.setHumedad(Math.round(humedad * 100.0) / 100.0);
                telemetria.setEstado(estado);

                // Convertir el objeto a JSON string
                String jsonMessage = objectMapper.writeValueAsString(telemetria);

                // Enviar a Kafka usando el camionId como Key para mantener el orden por
                // partición
                kafkaTemplate.send(topicName, camionId, jsonMessage);
            }

            return "¡Se han enviado 50 mensajes de simulación para los 3 camiones al tópico '" + topicName
                    + "' con éxito!";
        } catch (Exception e) {
            return "Error al generar o enviar la simulación: " + e.getMessage();
        }
    }

}
