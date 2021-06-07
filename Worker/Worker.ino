#include <ESP8266WiFi.h>
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define WIFI_SSID "Crack House"
#define WIFI_PASS "michaeliscool"

#define LED_PIN D3
// TESTING PURPOSES DATA
int DOOR_DATA[] = {5,5,1,8,5,15,2,20,30,0,1,0,4,0,0,5,15,25,5,1,0,0,25};
int inputForce = 4;
int inputDelay = 3000;

WiFiClient client;
Adafruit_SSD1306 display(128, 32, &Wire, -1);

void setup() {
  Serial.begin(9600);
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, HIGH);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  client.connect("192.168.0.100", 38368);
  delay(1000);
  
  client.print("amWorker");

  char buffer[20];
  sprintf(buffer, "m%s", WiFi.localIP().toString().c_str());
  client.print(buffer);

  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.cp437(true);

  digitalWrite(LED_PIN, LOW);
  updateDisplayData();
}

void loop() {
  char* dataString = readTCPData();
  if (dataString) {
    char command = dataString[0];
    char* argv = dataString + 1;
    if (command == 'r') {
      handleGraphData();
    }
    if (command == 'a') {
      handleParametersData();
    }
    if (command == 'V') {
      handleParametersUpdate(argv);
    }

    free(dataString);
  }
}

void handleParametersUpdate(char* argv) {
  char force[3];
  char time[3];
  memcpy(force, argv, 2 * sizeof(char));
  memcpy(time, argv + 2, 2 * sizeof(char));
  force[2] = '\0';
  time[2] = '\0';

  inputForce = atoi(force);
  inputDelay = atoi(time) * 1000;

  updateDisplayData();
}

void handleParametersData() {
  char out[6];
  sprintf(out, "A%.2d%.2d", inputForce, inputDelay / 1000);
  client.write(out);
}

void handleGraphData() {
  char out[50];
  out[0] = 'u';
  out[1] = '\0';
  for (int i = 0; i < 24; i++) {
    sprintf(out + strlen(out), "%d,", DOOR_DATA[i]);
  }
  out[strlen(out) - 1] = '\0';
  client.write(out);
}

void updateDisplayData() {
  display.setCursor(0, 0);
  display.clearDisplay();
  display.print("IP: ");
  display.println(WiFi.localIP().toString().c_str());
  
  display.print("Average Force: ");
  display.println(inputForce);
  display.print("Average Delay: ");
  display.println(inputDelay);
  display.display();
}

char* readTCPData() {
  unsigned char buffer[5];
  char* dataString = NULL;
  client.read(buffer, 4);
  buffer[4] = '\0';
  if (strlen((const char*)buffer) == 0) return NULL;
  unsigned len = atoi((char*)buffer);
  dataString = (char*)malloc((len + 1) * sizeof(char));
  client.read((unsigned char*)dataString, len);
  dataString[len] = '\0';

  return dataString;
}
