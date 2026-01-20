import socket
import sys
import time
import random
import string
from datetime import datetime

def generate_random_string(minC,maxC):
    """Generate a random word of 5-10 characters"""
    return ''.join(random.choices(string.ascii_lowercase, k=random.randint(minC, maxC)))

def generate_random_number(minC,maxC):
    return random.randint(minC, maxC)


def main(port):
    # Create a socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    # Bind to the port
    server_socket.bind(('localhost', port))
    server_socket.listen(1)
    print(f"Server listening on port {port}...")
    
    try:
        while True:
            # Accept a connection
            connection, client_address = server_socket.accept()
            print(f"Client connected from {client_address}")
            
            try:
                while True:
                    # Generate random string and timestamp
                    src = generate_random_number(1,9)
                    trgt = generate_random_number(1,9)
                    label = generate_random_string(1,3)
                    timestamp = int(datetime.now().timestamp())
                    message = f"{src};{trgt};{label};{timestamp}\n"
                    
                    # Send data
                    connection.sendall(message.encode('utf-8'))
                    print(f"Sent: {message.strip()}")
                    
                    # Wait before sending next message
                    time.sleep(1)
            finally:
                connection.close()
    except KeyboardInterrupt:
        print("\nShutting down...")
    finally:
        server_socket.close()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python mysocket.py <port>")
        sys.exit(1)
    
    port = int(sys.argv[1])
    main(port)