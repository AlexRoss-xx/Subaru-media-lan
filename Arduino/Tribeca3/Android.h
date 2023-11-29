#pragma once
#include "Arduino.h"
//#include "Lan.h"

#define MAXMSGLEN 32

typedef struct 
{
	byte	type;
	word	deviceAddress;
	byte	dataSize;
	byte	message[MAXMSGLEN];
} AndroidMessage;

 
typedef enum
{   // No this is not a mistake, broadcast = 0!
	LAN = 1,
	CAN = 0
} MessageType;

 

class  Android {
public:

	uint16_t cnt = 0;
	uint16_t cntCan = 0;

	bool status = 0;// 0-none   1-registered  
	bool procmessage = 0;
	void begin();
	void PutMessage(MessageType type, word deviceAddress, byte dataSize, byte message[MAXMSGLEN]);

	void ProceedCommand();
	void onCD_T();
	void onR2();
	void onR0();
	void onR1();
	void onR3();
	void onX();
	bool ProcessQueue();

private:
	void serialize( AndroidMessage );
	void recvWithEndMarker();
	
};

 extern Android android;

