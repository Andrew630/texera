import socket

from core.models.tdb import Tdb


class TDBClient:
    def __init__(self):
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket.connect((Tdb.DEFAULT_ADDR, Tdb.DEFAULT_PORT))

    def send(self, command: str):
        self._socket.send(command.encode("utf-8"))
