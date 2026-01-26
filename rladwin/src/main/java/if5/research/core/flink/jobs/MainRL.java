package if5.research.core.flink.jobs;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import if5.research.core.flink.operators.SocketRLMapper;

public class MainRL {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); // IMPORTANT: 1 seul thread pour 1 seul socket Python

        // Remplace par le chemin absolu de ton dataset Elec ou Synthétique
        String datasetPath = "C:\\Users\\antag\\Downloads\\elec.csv";

        System.out.println("--- DÉMARRAGE ORCHESTRATEUR RL ---");

        // 1. Lire le fichier
// On configure la nouvelle source de fichier
        FileSource<String> source = FileSource.forRecordStreamFormat(
                new TextLineInputFormat(),
                new Path(datasetPath)
        ).build();

        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "File Source");
        // 2. Passer par le Socket (Python)
        stream
                .map(new SocketRLMapper())
                .print(); // Afficher ce que l'IA a décidé

        env.execute("Flink RL Job");
    }
}
