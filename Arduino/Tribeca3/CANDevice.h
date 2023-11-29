#pragma once

#include "mcp_can.h"
#include <SPI.h>
// CAN Interrupt and Chip Select Pins
#define CAN0_INT 9 
#define standard 0
#define FUNCTIONAL_ID 0x70
 

class CANDevice {
public:	 
	uint16_t tpms = 0;
	String str;
	void begin();
	 

	void readMessage();
	char * getMessage();
	void sendMessage(uint8_t command);
	void send();
};
extern CANDevice canDevice;