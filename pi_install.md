#Installing on Raspberry Pi:

1. Run the following commands

```bash
sudo apt-get install libbluetooth-dev python-dev libglib2.0-dev libboost-python-dev libboost-thread-dev

pip3 download gattlib
tar xvzf ./gattlib-0.20150805.tar.gz
cd gattlib-0.20150805/
sed -ie 's/boost_python-py34/boost_python-py35/' setup.py
pip3 install .

sudo python3 -m pip install pybluez pybluez\[ble\]
```

2. Edit /etc/systemd/system/dbus-org.bluez.service
Add a "-C" to the line that says ExecStart=...

3. Run the command for enabling serial ports `sudo sdptool add SP`

4. Run the commands
```bash
sudo systemctl daemon-reload
sudo service bluetooth restart
sudo hciconfig hci0 piscan
```
5. restart the pi


6. The pi needs to be discoverable. in order to accomplish this, you will need to run `sudo bluetoothctl discoverable yes`.  This is done in startup currently because it needs to be run every time the pi is restarted before running the script 

7. Run the server script (we only tested with python 3)