/*
 Basic ESP8266 MQTT example

 This sketch demonstrates the capabilities of the pubsub library in combination
 with the ESP8266 board/library.

 It connects to an MQTT server then:
  - publishes "hello world" to the topic "outTopic" every two seconds
  - subscribes to the topic "inTopic", printing out any messages
    it receives. NB - it assumes the received payloads are strings not binary
  - If the first character of the topic "inTopic" is an 1, switch ON the ESP Led,
    else switch it off

 It will reconnect to the server if the connection is lost using a blocking
 reconnect function. See the 'mqtt_reconnect_nonblocking' example for how to
 achieve the same result without blocking the main loop.

 To install the ESP8266 board, (using Arduino 1.6.4+):
  - Add the following 3rd party board manager under "File -> Preferences -> Additional Boards Manager URLs":
       http://arduino.esp8266.com/stable/package_esp8266com_index.json
  - Open the "Tools -> Board -> Board Manager" and click install for the ESP8266"
  - Select your ESP8266 in "Tools -> Board"

*/

#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// Update these with values suitable for your network.

const char* ssid = "Neptune";
const char* password = "Datamini!2345";
const char* mqtt_server = "192.168.1.194";

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = 0;
String deviceName;

String outTopic;
String inTopic;

void setup() {
  deviceName = "IOT_"+WiFi.macAddress();
  deviceName.replace(":","");
  //strcat (deviceNameChar, deviceName);
  //strcat (outTopic, deviceNameChar);
  outTopic = deviceName + "/out";
  inTopic = deviceName + "/in";
  
  pinMode(BUILTIN_LED, OUTPUT);     // Initialize the BUILTIN_LED pin as an output
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);

  if (!client.connected()) {
    reconnect();
  }

  client.publish(outTopic.c_str(), "WiFi connected");
  client.publish(outTopic.c_str(), "IP address: ");
  client.publish(outTopic.c_str(), WiFi.localIP().toString().c_str());
  client.publish(outTopic.c_str(), "Device Name:");
  client.publish(outTopic.c_str(), String(deviceName).c_str());
}

void setup_wifi() {

  
  
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.softAP(deviceName.c_str(), password);
  WiFi.begin(ssid, password);
  

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println(deviceName);
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");

  String message = "";
  
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    message = message + (char)payload[i];
  }
  Serial.println();
  Serial.println("---------------");

  //String message = String((char*)payload);
  //Serial.println(message);
  Serial.println("---------------" + message + "---------------");

  int ind = message.indexOf("GPIO/GET");
  Serial.println("Index of GPIO/GET is " + String(ind) + "---------------");

  // Switch on the LED if an 1 was received as first character
  if ((char)payload[0] == '1') {
    digitalWrite(BUILTIN_LED, LOW);   // Turn the LED on (Note that LOW is the voltage level
    // but actually the LED is on; this is because
    // it is acive low on the ESP-01)
  } else if (message == "reboot") {
    ESP.restart();
  } else if (message == "reset") {
    ESP.reset();
  } else if (message.indexOf("GPIO/GET") > -1) {
    Serial.println("---------------Get---------------");

    int i = message.lastIndexOf("/");
    String pin = message.substring(i + 1, message.length());

    pinMode(pin.toInt(), OUTPUT);
    
    int value = digitalRead(pin.toInt());
    client.publish(outTopic.c_str(), String(value).c_str());
  } else if (message.indexOf("GPIO/HIGH") > -1) {
    Serial.println("---------------High---------------");
    
    int i = message.lastIndexOf("/");
    String pin = message.substring(i + 1, message.length());

    pinMode(pin.toInt(), OUTPUT);
    digitalWrite(pin.toInt(), HIGH);
    
    client.publish(outTopic.c_str(), String("Set ping " + pin + " to HIGH").c_str());
  } else if (message.indexOf("GPIO/LOW") > -1) {
    Serial.println("---------------Low---------------");

    int i = message.lastIndexOf("/");
    String pin = message.substring(i + 1, message.length());

    pinMode(pin.toInt(), OUTPUT);
    digitalWrite(pin.toInt(), LOW);
    client.publish(outTopic.c_str(), String("Set ping " + pin + " to LOW").c_str());
  } else {
    digitalWrite(BUILTIN_LED, HIGH);  // Turn the LED off by making the voltage HIGH
  }

}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect("ESP8266Client")) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      client.publish(outTopic.c_str(), "hello world");
      // ... and resubscribe
      client.subscribe(inTopic.c_str());
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}
void loop() {

  if (!client.connected()) {
    reconnect();
  }
  client.loop();

//  long now = millis();
//  if (now - lastMsg > 2000) {
//    lastMsg = now;
//    ++value;
//    snprintf (msg, 75, "hello world #%ld", value);
//    Serial.print("Publish message: ");
//    Serial.println(msg);
//    client.publish(outTopic.c_str(), msg);
//  }
}
