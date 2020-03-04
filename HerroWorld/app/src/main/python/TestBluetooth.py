
import bluetooth

target_name = "My Phone"
target_address = None
shota_phone = "4C:DD:31:C9:92:05"

def discover():
    nearby_devices = bluetooth.discover_devices()
    message = "Addresses: \n"
    for bdaddr in nearby_devices:
        found_name = bluetooth.lookup_name( bdaddr )
        # print("{}, {}".format(found_name, bdaddr))
        # if target_name == found_name:
        #     target_address = bdaddr
        #     break
        message = message + bdaddr "\n"
    
    return message

    # if target_address is not None:
    #     print("found target bluetooth device with address ", target_address)
    # else:
    #     print("could not find target bluetooth device nearby")