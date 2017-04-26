/*
 *  This sketch sends data via HTTP GET requests to data.sparkfun.com service.
 *
 *  You need to get streamId and privateKey at data.sparkfun.com and paste them
 *  below. Or just customize this script to talk to other HTTP servers.
 *
 */

#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

void appSetup(){

  pinMode(LED_BUILTIN, OUTPUT);
}

void appLoop(){
  digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(1000);                       // wait for a second
  digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  delay(1000);                       // wait for a second
}



























ESP8266WebServer server(80);
const char* ssid     = "Neptune";
const char* password = "Datamini!23454";
char* logger = "serial";
int startMode = 2;

void setup() {

  if(logger == "serial"){
    Serial.begin(115200);
    delay(10);
  }
  
  // We start by connecting to a WiFi network
  if(!connectToWIFI()){

    startStationMode();
  } else {

    updateFirmware();
  }
  appSetup();
  
}

void loop() {

  if(startMode == 2){
    server.handleClient();
  }else{  
    appLoop();
  }
}



#define VERSION 1



/**
 * This method will perform logging the given messages based on the configuration
 * set in cloud config file.
 * /iot/config/<MAC ADDRESS>
 * debug variable in config file can have 3 values
 * debug : "serial" <-- This will print log messages to serial output
 * debug : "mqtt" <-- This will send log messages to mqtt topic "/iot/<MAC ADDRESS>/logs"
 * debug : "" <-- This will disable all log messages
 */
void debug(String message){

  if(logger == "serial"){
    Serial.println(message);
  }
}

boolean connectToWIFI(){

  debug("");
  debug("");
  debug("Connecting to ");
  debug(ssid);

  int retryCount = 1;
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    debug(".");

    if(retryCount > 10){

      break;
    }
    
    retryCount += 1;
  }

  if(WiFi.status() != WL_CONNECTED){

    return false;
  }

  debug("");
  debug("WiFi connected");  
  debug("IP address: ");
  debug(String(WiFi.localIP()));
  debug(WiFi.macAddress());

  return true;
}

void updateFirmware(){

  // WiFiFlientSecure for SSL/TLS support
  WiFiClientSecure client;

  const char* host    =  "wasanka.github.io";
  const int port  = 443;
  String resource = "/iot/config/"  + WiFi.macAddress();

  debug("Connecting to ");
  debug(host);

  if (! client.connect(host, port)) {
    debug("Connection failed. Halting execution.");
    while(1);
  }

  // Checking for SSL fingerprint
  const char* fingerprint = "D7 9F 07 61 10 B3 92 93 E3 49 AC 89 84 5B 03 80 C1 9E 2F 8B";
  if (client.verify(fingerprint, host)) {
    debug("Connection secure.");
  } else {
    debug("Connection insecure! Still continuing execution.");
  }
  
  debug("Requesting URL: ");
  debug(resource);
  
  // This will send the request to the server
  client.print(String("GET ") + resource + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      debug(">>> Client Timeout !");
      client.stop();
      return;
    }
  }

  // Skipping response headers
  char endOfHeaders[] = "\r\n\r\n";

  client.setTimeout(5000);
  bool ok = client.find(endOfHeaders);

  if (!ok) {
    Serial.println("No response or invalid response!");
  }

  
  String content = "";
  
  // Read all the lines of the reply from server and print them to Serial
  while(client.available()){
    content += client.readStringUntil('\r');
    //Serial.print(line);
  }
  
  debug("---"+content+"---");
  DynamicJsonBuffer jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(content);
  const char* binary = root["binary"];
  int version = root["version"];

  debug("+++"+String(version)+"+++");

  if(version != VERSION){

    debug("Updating firmware");
  }
  
  debug("");
  debug("closing connection");
}



#define ACCESS_POINT_NAME  "MYESP" 
#define ACCESS_POINT_PASSWORD "12345678" 



#define QUOTE(...) #__VA_ARGS__
const char *PAGE_EXAMPLE = QUOTE(
<html>
<head>
 
<style> 
.title {
    padding: 5px;
    text-align: center;
    background-color: #e5eecc;
    border: solid 1px #c3c3c3;
    font-size: 24px; 
}

.lable {
    padding: 5px;
    text-align: right;
}
.text {
    padding: 5px;
    text-align: left;
}
</style>
</head>
<body>
<center>
<table style="width: 100%;" border=0>
<tbody>
<tr>
<td class="title" colspan="3">ESP IOT Application Framework</td>
</tr>
<tr>
<td class="lable">WIFI SSID :</td>
<td class="text">Neptune</td>
<td class="text"><input type="button" value="Edit" onclick="window.prompt('Enter WIFI Name','');"/></td>
</tr>
<tr>
<td class="lable">WIFI Password :</td>
<td class="text">Neptune</td>
<td class="text"><input type="button" value="Edit" onclick="window.prompt('Enter WIFI Password','');"/></td>
</tr>
<tr>
<td class="lable">IP :</td>
<td class="text">192.168.4.1</td>
<td class="text"><input type="button" value="Edit" onclick="window.prompt('Enter static IP','0.0.0.0');"/></td>
</tr>
<tr>
<td class="lable">Firmware Version :</td>
<td class="text">1.0</td>
<td class="text"></td>
</tr>
<tr>
<td class="lable">Up Time :</td>
<td class="text">25 minutes</td>
<td class="text"></td>
</tr>
<tr>
<td class="lable">MAC Address :</td>
<td class="text">5c:cf:7f:c3:c2:c4</td>
<td class="text"></td>
</tr>
<tr>
<td class="lable">Flash Size :</td>
<td class="text">4096 kB</td>
<td class="text"></td>
</tr>
<tr>
<td class="lable">Sketch Size (Free) :</td>
<td class="text">401 kB (620 kB)</td>
<td class="text"></td>
</tr>
<tr>
<td class="lable">Free Memory :</td>
<td class="text">27408</td>
<td class="text"></td>
</tr>
</tbody>
</table>
<center>
<!-- DivTable.com -->

</body>
</html>

);

void startStationMode(){

  debug("Starting station mode");
  startMode = 2;
  WiFi.mode(WIFI_STA);
  WiFi.softAP( "ESP");
  WiFi.begin("esp8266", "12345");
  if (MDNS.begin("esp8266")) {
    Serial.println("MDNS responder started");
  }

  // Admin page
  server.on ( "/favicon.ico",   []() {
        Serial.println("favicon.ico");
        server.send ( 200, "text/html", "" );
      }  );
  
  server.on ( "/", []() { server.send ( 200, "text/html", PAGE_EXAMPLE );  } );

server.onNotFound ( []() {
        Serial.println("Page Not Found");
        server.send ( 400, "text/html", "Page not Found" );
      }  );
  
  server.begin();
}
