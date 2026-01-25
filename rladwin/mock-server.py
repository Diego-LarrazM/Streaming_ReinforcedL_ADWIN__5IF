import socket
import random
import time

HOST = 'localhost'
PORT = 9999

def start_server():
    # Création du socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, PORT))
    server_socket.listen(1)

    print(f"PYTHON: Le cerveau écoute sur {HOST}:{PORT}...")

    conn, addr = server_socket.accept()
    print(f"PYTHON: Connecté à Flink ({addr})")

    try:
        while True:
            # 1. Recevoir des données de Flink
            data = conn.recv(1024).decode('utf-8')
            if not data:
                break

            # (On nettoie les sauts de ligne)
            message = data.strip()
            if not message:
                continue

            print(f"REÇU: {message}")

            # 2. Simuler une décision d'IA (0 ou 1)
            # Dans le vrai projet, c'est ici que le DeepQ travaillera
            action = random.choice(["SPLIT", "MERGE", "WAIT"])

            # 3. Répondre à Flink (Important: ajouter \n pour la fin de ligne)
            response = f"{action}\n"
            conn.sendall(response.encode('utf-8'))

    except Exception as e:
        print(f"Erreur: {e}")
    finally:
        conn.close()
        server_socket.close()

if __name__ == "__main__":
    start_server()