package if5.research.core.flink.operators;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Cette classe fait le pont entre Flink et Python.
 * Entrée : String (une ligne du CSV)
 * Sortie : String (la réponse de l'IA)
 */
public class SocketRLMapper extends RichMapFunction<String, String> {

    private transient Socket socket;
    private transient PrintWriter out;
    private transient BufferedReader in;

    // S'exécute UNE FOIS au démarrage
    @Override
    public void open(OpenContext openContext) throws Exception {
        System.out.println("JAVA: Tentative de connexion au Python...");
        socket = new Socket("localhost", 9999);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("JAVA: Connecté !");
    }

    // S'exécute POUR CHAQUE LIGNE du dataset
    @Override
    public String map(String value) throws Exception {

        // 1. Envoyer la donnée au Python
        // (Pour l'instant on envoie la ligne brute, plus tard on enverra les stats du Bucket)
        out.println(value);

        // 2. Attendre la réponse de l'IA
        String response = in.readLine();

        // 3. Retourner le résultat pour l'afficher ou l'utiliser
        return "Donnée: " + value + " | Action IA: " + response;
    }

    // S'exécute à la fin pour fermer proprement
    @Override
    public void close() throws Exception {
        if (socket != null) socket.close();
    }
}