
import bluetooth

server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

shota_phone = "4C:DD:31:C9:92:05"
HOST_MAC = "B4:69:21:BE:FA:A9"
UUID = "b5c65192-1d67-471f-8147-0d0e8904efaa"
server_sock.bind(("",bluetooth.PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()
print("listening on port {}".format(port))
print("SERIAL_PORT_CLASS: {}\nSERIAL_PORT_PROFILE: {}".format(bluetooth.SERIAL_PORT_CLASS, bluetooth.SERIAL_PORT_PROFILE))

bluetooth.advertise_service(
    server_sock,
    "Shota Bluetooth Server",
    UUID,
    service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
    profiles=[bluetooth.SERIAL_PORT_PROFILE])
client_sock,address = server_sock.accept()
print("Accepted connection from {}".format(address))

msg_max = 3
counter = 0
Running = True
while Running and counter < msg_max:
    data = client_sock.recv(1024)
    print("received [{}]".format(data))
    if data == b'quit':
        Running = False
    counter += 1
    client_sock.send(b"received: " + data)

client_sock.close()
server_sock.close()