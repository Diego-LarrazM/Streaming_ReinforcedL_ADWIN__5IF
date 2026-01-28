import numpy as np
import socket
import sys
import time
import csv

def gaussian_sample(mean, std=1.0):
    """
    Generate one Gaussian sample with given mean and std.
    """
    return np.random.normal(loc=mean, scale=std)

def update_mean(t, mean, mode, params):
    """
    Update the Gaussian mean based on drift mode.

    t      : current timestep
    mean   : current mean
    mode   : 'stable' | 'abrupt' | 'gradual' | 'erratic'
    params : dict with mode-specific parameters
    """

    if mode == "stable":
        return mean

    elif mode == "abrupt":
        # single hard jump at t0
        if t == params["t0"]:
            return mean + params["delta"]
        return mean

    elif mode == "gradual":
        # linear drift over a time window
        if params["t_start"] <= t <= params["t_end"]:
            return mean + params["delta_per_step"]
        return mean

    elif mode == "erratic":
        # noisy fluctuations that SHOULD NOT cause splits
        noise = np.random.normal(0, params["noise_std"])
        return mean + noise

    else:
        raise ValueError(f"Unknown mode: {mode}")
    

def gaussian_stream(total_steps=5000, std=1.0, seed=42):
    np.random.seed(seed)

    mean = 0.0
    stream = []

    for t in range(total_steps):

        # ----- define regimes -----
        if t < 1000:
            mode = "stable"
            params = {}

        elif t < 2000:
            mode = "abrupt"
            params = {"t0": 1200, "delta": 3.0}

        elif t < 3500:
            mode = "gradual"
            params = {
                "t_start": 2000,
                "t_end": 3500,
                "delta_per_step": 0.002
            }

        else:
            mode = "erratic"
            params = {"noise_std": 0.05}

        # ----- update mean -----
        mean = update_mean(t, mean, mode, params)

        # ----- generate value -----
        x = gaussian_sample(mean, std)

        stream.append(x)

    return stream



HOST = 'localhost'

def start_server(port):
    # Création du socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, port))
    server_socket.listen(1)

    print(f"PYTHON: Le cerveau écoute sur {HOST}:{port}...")

    conn, addr = server_socket.accept()
    print(f"PYTHON: Connecté à Flink ({addr})")

    try:
        while True:
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
    if len(sys.argv) < 2:
        print("Usage: python stream_source.py <port>")
        sys.exit(1)
    
    port = int(sys.argv[1])
    start_server(port)