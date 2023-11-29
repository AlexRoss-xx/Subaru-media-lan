// Prepare and send LAN or  CAN information  into an Android Unit
// or recive  message from Android and send commands into  CAN network or   LAN network



#pragma once
#include "Android.h"
#include <QList.h>
#include "LanNAVI.h"
#include "Config.h"
#include "CANDevice.h"
#include <MemoryFree.h>

#define QUEUESIZE     14
#define PGMSTR(x) (__FlashStringHelper*)(x)

QList<char *> queue = QList<char *>();


const byte numChars = 5;
char receivedChars[numChars];   // an array to store the received data
boolean newData = false;
uint8_t inputParam = 0;
uint8_t cd = 0;
uint8_t folder = 0;
byte satMode = 0;
static  char buf[7];


// Version Message
const char verMessage[50] PROGMEM= "{\"T\":3,\"A\":0,\"M\":[1,2,9,7,67,230,155,215]}";
 
void Android::begin() {
	status = 0;
	while (queue.length() > 0) {
		char * jsMessage = queue.front();
		queue.pop_front();
		free(jsMessage);
	}
}

void Android::PutMessage(MessageType type, word	dAddress, byte	dataSize, byte message[MAXMSGLEN])
{
	//Serial.println("PUT");
	//	uint8_t oldSREG = SREG;
	//	cli();
	if (status == 1) {

		//Serial.println(str);
	/*	if (queue.size() == 0 && lanDevice.status == REGISTER   && Serial) {
			Serial.println(str);
			 procmessage=  false;
		}
		else */
		//Serial.print(freeMemory
		 
		if (queue.size()<= QUEUESIZE && freeMemory() >= 256   )
		{
			int len = 30 + dataSize * 3 + dataSize;
			char * mess = (char *)malloc(len);
			strcpy(mess, "{\"T\":1,\"A\":");
			strcat(mess, utoa(dAddress, buf, 10));//0   
			strcat(mess, ",\"M\":[");
			for (int i = 0; i < dataSize; i++) {
				strcat(mess, itoa(message[i], buf, 10));//0    
				if (i != dataSize - 1)
					strcat(mess, ",");
			}
			strcat(mess, "]}");
			queue.push_back(mess);
			//Serial.println(mess);
		/*	String str;
			str.reserve(30 + dataSize * 3 + dataSize);
			str = "{\"T\":";
			str += type;
			str += ",\"A\":";
			str += dAddress;
			str += ",\"M\":[";
			for (int i = 0; i < dataSize; i++) {
				str += message[i];
				if (i != dataSize - 1)
					str += ",";
			}
			str += "]}";
			queue.push_back(str);*/
			//procmessage = true;
		}
		 
	}
	//SREG = oldSREG;
	   //lan.printMessage(true);
}



bool Android::ProcessQueue()
{
	// recvWithEndMarker();
	// ProceedCommand();
    // uint8_t oldSREG = SREG;
	// cli();

	// if (status == 1)
	{
		/*if ((cnt > 50000)) {
			String jsMessage = "{\"T\":1,\"A\":320,\"M\":[96,7,2,17,1,187,135,80,187,135,144,187,152,16,177, 5, 16,177, 07,144, 177, 08, 00]}";
			Serial.println(jsMessage);
			cnt = 0;
		}*/
		 
		if ((queue.size() > 0) && (cnt > 1000)) {
			if (Serial.availableForWrite() >= 255) {
				//String jsMessage = queue.front();	
				char * jsMessage = queue.front();
				Serial.println(jsMessage);
				queue.pop_front();
				free(jsMessage);
				jsMessage = NULL;
				//Serial.println(freeMemory());
				cnt = 0;
				cntCan = 1000;
				return true;
			}
		}
		else {
			cnt = cnt + 1;
			if (cntCan >= 5000) {
				if (Serial.availableForWrite() >= 255) {						
				 	Serial.println(canDevice.getMessage());
					cntCan = 0;	
					return true;
				}
			}			 
			else
				cntCan = cntCan + 1;
		}
		return false;
	}
	return false;
	// SREG = oldSREG;
}


void Android::recvWithEndMarker() {

	static byte ndx = 0;
	char endMarker = '\n';
	char rc;

	while (Serial.available() > 0 && newData == false) {
		//Serial.print("serial availible:");
		rc = Serial.read();

		if (rc != endMarker) {
			receivedChars[ndx] = rc;
			ndx++;
			if (ndx >= numChars) {
				ndx = numChars - 1;
			}
		}
		else {
			receivedChars[ndx] = '\0'; // terminate the string
			ndx = 0;
			newData = true;
		}
	}
}

// Proceed  input (from Android) Command 
void Android::ProceedCommand() {

	recvWithEndMarker();

	if (newData) {
		if (receivedChars[0] == 'M' && receivedChars[1] == '1') {
			lan.loadMessage(&CmdMuteOn);
			lan.sendMessage();
			TRANSMIT_AUDIO_OFF;
		}
		else if (receivedChars[0] == 'M' && receivedChars[1] == '0') {
			lan.loadMessage(&CmdMuteOff);
			lan.sendMessage();
			TRANSMIT_AUDIO_ON;
		}
		else if (receivedChars[0] == 'R' && receivedChars[1] == '2') {
			onR2();
		}
		else if (receivedChars[0] == 'R' && receivedChars[1] == '1') {
			onR1();
		}
		else if (receivedChars[0] == 'R' && receivedChars[1] == '0') {
			onR0();
		}
		else if (receivedChars[0] == 'R' && receivedChars[1] == '3') {
			onR3();
		}
		else  if (receivedChars[0] == 'D') {
			inputParam = atoi(receivedChars + 1);
			cd = inputParam;
		}
		else  if (receivedChars[0] == 'F') {
			inputParam = atoi(receivedChars + 1);

			lan.loadMessage(&CmdGetMP3FolderInfo);
			lan.message[3] = cd;

			lan.message[5] = inputParam;
			lan.sendMessage();
			folder = inputParam;
		}
		else  if (receivedChars[0] == 'T') {
			onCD_T();
		}
		else  if (receivedChars[0] == 'C') {
			inputParam = atoi(receivedChars + 1);
			canDevice.sendMessage(inputParam);
		}
		else if (receivedChars[0] == 'S') {
			inputParam = atoi(receivedChars + 1);

			lan.loadMessage(&CmdSwitch);
			//lan.message[2] = inputParam;
			lan.sendMessage();
		}
		else if (receivedChars[0] == 'V') {
			inputParam = atoi(receivedChars + 1);
			if (inputParam == 1) {
				Serial.println(PGMSTR(verMessage));
			}
		}
		else if (receivedChars[0] == 'X') {
			onX();
		}

		newData = false;
	}
}

// To load MP3 Tags from CD 
void Android::onCD_T()
{
	inputParam = atoi(receivedChars + 1);

	lan.loadMessage(&CmdGetMP3FileInfo);
	lan.message[3] = cd;
	lan.message[5] = folder;
	lan.message[6] = inputParam;
	lan.sendMessage();

	lan.loadMessage(&CmdGetMP3FileInfo);
	lan.message[3] = cd;
	lan.message[5] = folder;
	lan.message[6] = inputParam;
	lan.message[7] = 0x01;
	lan.sendMessage();
}

//Begin  send items from   Message QUE
//by default MCU does not  send  messages after ON  because android wake up later  and we need  to send initial info from devices into Android  to show correct settings
void Android::onR2()
{
	//Serial.clearWriteError();
	//Serial.flush();
	//while (Serial.available())
	//	Serial.read();

	while (queue.length() > 0) {
		char * jsMessage = queue.front();
		queue.pop_front();
		free(jsMessage);
	}
	cnt = 0;

	lanDevice.sendGetInit = 0;
	status = 1;
	//canDevice.begin();
}

// Reset units registration 
void Android::onR0()
{
	lanDevice.begin();
	android.begin();
	lan.sendMessage(&CmdRegisterReset);
}

// Reset units registration 
void Android::onR1()
{
	status = 0;
	cnt = 0;

	while (queue.length() > 0) {
		char * jsMessage = queue.front();
		queue.pop_front();
		free(jsMessage);
	}

	//Serial.clearWriteError();
	//Serial.flush();
	//while (Serial.available())
	//Serial.read();

	if (lanDevice.status != REGISTER)
	{
		lanDevice.begin();
		//canDevice.begin();
		lan.sendMessage(&CmdRegisterReset);
	}
}

void Android::onR3()
{
	//lan.loadMessage(&CmdDevice, lan.deviceAddressCDC);
	//lan.sendMessage();
}

// Clear message Que
void Android::onX()
{
	status = 0;
	cnt = 0;

	while (queue.length() > 0) {
		char * jsMessage = queue.front();
		queue.pop_front();
		free(jsMessage);
	}
	Serial.end();
	Serial.begin(2000000);
}

Android android;
