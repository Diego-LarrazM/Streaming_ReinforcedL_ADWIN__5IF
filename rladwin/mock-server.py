import socket
import time
import csv

HOST = 'localhost'
PORT = 9999
CSV_FILE = './source/elec.csv' 

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
            with open (CSV_FILE, 'r') as file:
                csv_reader = csv.reader(file)
                header = next(csv_reader)  # Lire l'en-tête (si présent)
                print(f"PYTHON: En-tête du fichier CSV: {header}")
                for row in csv_reader:
                    event = ','.join(row) + '\n'
                    conn.sendall(event.encode('utf-8'))
                    print(f"PYTHON: Envoyé: {event.strip()}")
                    time.sleep(1)
    except Exception as e:
        print(f"Erreur: {e}")
    finally:
        conn.close()
        server_socket.close()

if __name__ == "__main__":
    start_server()