# MotoSafe Motorcycle Security System

## Overview

**MotoSafe** is a capstone thesis project designed to enhance motorcycle safety and security through an Android mobile application and an ESP32-based hardware system. 

The Android app, built with Java in Android Studio, provides a user-friendly interface with map visualization using OSMDroid to display real-time motorcycle data. The ESP32 hardware manages movement detection, GPS tracking, tampering detection via voltage monitoring, and remote control, communicating with the app via SMS using a SIM800L GSM module.

**Key features include** unauthorized movement alerts, remote kill switch control, and a low-power sleep mode.

> 🔗 [MotoSafe Android App Repository](https://github.com/Skca01/MotoSafeAndroidStudio)

*Note: ESP32 firmware will be added soon in a separate `/firmware` directory.*

---

## Features

- **Real-Time Monitoring**: Speed, location, and movement displayed on the app with OSMDroid map using NEO-8M GPS and MPU6050 data.
- **Security Alerts**: Detects unauthorized movement or voltage tampering; sends SMS or initiates a call to the owner.
- **Remote Control**: Control motorcycle power and horn through SMS commands from the app.
- **GPS Tracking**: Sends GPS updates every 10 seconds via SMS.
- **Tampering Detection**: Uses a voltage divider to detect electrical tampering (threshold: analog 1800).
- **Safety Constraints**: Prevents disabling motorcycle if speed > 13 km/h.
- **Low Power Mode**: Sleep mode enabled with MOSFET control, wakeable via SMS.
- **User-Friendly App**: Map-based UI for data visualization and sending commands.

---

## System Architecture

### Android App

- **Language**: Java (Java 11)
- **IDE**: Android Studio
- **Libraries**:
  - OSMDroid 6.1.16
  - androidx.appcompat, activity, constraintlayout
  - com.google.android.material
  - commons-io:2.11.0
- **Testing**: junit, androidx.test.ext.junit, espresso-core
- **SDK**:
  - Compile: 34
  - Minimum: 29 (Android 10)
  - Target: 34

### ESP32 Hardware

- **Microcontroller**: ESP32
- **Modules**:
  - **MPU6050**: Accelerometer (0.3g threshold for motion detection)
  - **NEO-8M**: GPS tracking (location & speed)
  - **SIM800L**: GSM SMS/Call communication
- **Components**:
  - **IRLZ44N MOSFET**: Power control for sleep mode
  - **Relays**: Kill switch (D4) & Horn (D19)
  - **Voltage Divider**: Tampering detection (D33)
- **Communication Pins**:
  - I2C: D21 (SDA), D22 (SCL) for MPU6050
  - UART1: D14 (TX), D13 (RX) for SIM800L
  - UART2: D17 (TX), D16 (RX) for GPS

---

## Installation

### Android App

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/Skca01/MotoSafeAndroidStudio.git
   ```

2. **Open in Android Studio**  
   Import the project and ensure Gradle syncs with dependencies.

3. **Run the App**  
   Connect an Android device (API level 29+) or use an emulator.  
   From Android Studio: **Run > Run 'app'**

---

### ESP32 Firmware (Coming Soon)

1. **Hardware Setup**  
   Connect:
   - MPU6050, NEO-8M, SIM800L
   - IRLZ44N MOSFET
   - Kill switch & horn relays
   - Voltage divider to D33

2. **Upload Firmware**
   - Open the code in Arduino IDE or PlatformIO
   - Install libraries: `TinyGPSPlus`, `Wire`, `Preferences`
   - Upload to ESP32 via USB

3. **Initial Configuration**
   - Send an SMS: `setnumber <your_phone_number>`
   - The ESP32 replies with system status via SMS

---

## Usage

### Android App

- Open the MotoSafe app
- View GPS location, alerts, and status updates (via SMS)
- Send control commands to ESP32

### ESP32 Commands

- `status`: Get system status
- `track on/off`: Enable/disable GPS tracking
- `on/off`: Enable/disable motorcycle (only when speed < 13 km/h)
- `alerts on/off`: Enable/disable motion/tamper alerts
- `acknowledge`: Stop alerts and horn
- `sleep` / `wake`: Toggle low-power mode
- `setnumber <number>`: Set or update owner phone number
- `help`: View available commands

### Map Visualization

OSMDroid displays:
- GPS coordinates
- Real-time updates
- Speed and satellite info (if enabled)

---

## Limitations

- ESP32 firmware is not yet included (coming soon).
- SMS-based communication only (real-time syncing via WiFi/MQTT planned).
- Ensure Android app has SMS permissions.

---

## Project Status

- ✅ Android App: UI, SMS handling, map display completed
- ✅ ESP32 Firmware: Fully functional (to be published)
- 🔄 Integration: SMS-based control and alerts working; real-time sync in progress

### Next Steps

- [ ] Upload ESP32 firmware to `/firmware`
- [ ] Enhance SMS parsing for real-time UI updates
- [ ] Implement MQTT/WiFi integration
- [ ] Add features: alert history, graphical speed display

---

## Contributing

Contributions are welcome! 🚀

```bash
# Fork and clone
git clone https://github.com/your-username/MotoSafeAndroidStudio.git

# Create a branch
git checkout -b feature-branch

# Make changes, then commit and push
git commit -m "Add feature"
git push origin feature-branch
```

Open a pull request and let's improve MotoSafe together!

---

## License

This project is licensed under the **MIT License**.

---

## Contact

For questions or feedback, contact [Kent Carlo B. Amante](https://github.com/Skca01) via GitHub issues or email at carloamante125@gmail.com.

---

## Acknowledgements

- Capstone thesis advisors and teammates
- Libraries: OSMDroid, TinyGPSPlus, Wire, Preferences
- Hardware: ESP32, MPU6050, NEO-8M, SIM800L, IRLZ44N MOSFET
