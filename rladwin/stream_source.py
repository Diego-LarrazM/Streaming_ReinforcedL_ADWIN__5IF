import socket
import sys
import time
import csv

HOST = 'localhost'
# CSV_FILE = './source/elec.csv' or './source/synthetic_abrupt.csv'

def start_server(port, csv_file):
    # Création du socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, port))
    server_socket.listen(1)

    print(f"PYTHON: Le cerveau écoute sur {HOST}:{port}...")

    conn, addr = server_socket.accept()
    print(f"PYTHON: Connecté à Flink ({addr})")

    try:
        while True:
            with open (csv_file, 'r') as file:
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
    if len(sys.argv) < 3:
        print("Usage: python stream_source.py <port> <csv_file>")
        sys.exit(1)
    
    port = int(sys.argv[1])
    csv_file = sys.argv[2]
    start_server(port, csv_file)