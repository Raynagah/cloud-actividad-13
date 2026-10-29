package com.kafka.consumidor.Repository;

import com.kafka.consumidor.Model.TelemetriaCamion;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TelemetriaRepository {

    private final DynamoDbTable<TelemetriaCamion> table;

    // Inyectamos el cliente mejorado de DynamoDB configurado en Spring
    public TelemetriaRepository(DynamoDbEnhancedClient enhancedClient) {
        // Vincula la clase bean con el nombre de la tabla en AWS DynamoDB
        this.table = enhancedClient.table("TelemetriaCamiones", TableSchema.fromBean(TelemetriaCamion.class));
    }

    // 1. Guardar o actualizar un registro (Ideal para tu Lambda / POST)
    public void guardar(TelemetriaCamion telemetria) {
        table.putItem(telemetria);
    }

    // 2. Consultar todo el historial de un camión usando su Partition Key (GET para
    // el gráfico)
    public List<TelemetriaCamion> obtenerHistorialPorCamion(String camionId) {
        // Condición de búsqueda: buscar por la Clave de Partición (camionId)
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(camionId).build());

        // Ejecuta la consulta y mapea los resultados a una lista de Java
        return table.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}
