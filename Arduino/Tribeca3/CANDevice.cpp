#include "CANDevice.h"
#include <mcp_can.h>
#include <SPI.h>
#include "Android.h"


MCP_CAN CAN0(10);
byte canReady = 0;

// CAN TX Variables  
//byte   txData[] = { 0x00,0x00,0x0C,0x03,0x00,0x00 };

byte   txData[] = { 0x00,0x00,0x00,0x00,0x00,0x00 };

unsigned long rxId;
unsigned char len = 0;
unsigned char rxBuf[8];


static char canMessage[80];
static char buf[10];

unsigned int avgFC = 0;
unsigned char index = 0;
int fuel[10] = { 0,0,0,0,0,0,0,0,0,0 };

uint8_t  id40_1 = 0x00, id40_4 = 0x00, id40_5 = 0x00, id40_6 = 0x00, id40_7 = 0x00;
uint8_t  id41_2 = 0x00;
uint8_t  id80_3 = 0x00, id80_2 = 0x00;
uint8_t  id82_0 = 0x00, id82_1 = 0x00, id82_4 = 0x00;
uint8_t  id23_7 = 0x00;

void CANDevice::begin()
{
	tpms = 0;
	pinMode(9, INPUT);
	if (CAN0.begin(MCP_STDEXT, CAN_125KBPS, MCP_8MHZ) == CAN_OK)
		Serial.print("");

	// We don't need to receive all messages , we need messages only from specific units
	// https://forum.arduino.cc/t/mcp2515-canbus-filtering/152383/2
	// https://github.com/coryjfowler/MCP_CAN_lib
	// 
	// 0x073C0000
	// 0x070C0000 
	CAN0.init_Mask(0, 0, 0x071C0000);                // Init first mask...  
	CAN0.init_Filt(0, 0, 0x00000000);                // Init first filter...
	CAN0.init_Filt(1, 0, 0x00000000);                // Init second filter...

	CAN0.init_Mask(1, 0, 0x071C0000);                // Init second mask... 
	CAN0.init_Filt(2, 0, 0x00000000);                // Init third filter...
	CAN0.init_Filt(3, 0, 0x00000000);                // Init fouth filter...
	CAN0.init_Filt(4, 0, 0x00000000);                // Init fifth filter...
	CAN0.init_Filt(5, 0, 0x00000000);

	 
	CAN0.setMode(MCP_NORMAL);                        // Set operation mode to normal so the MCP2515 sends acks to received data.

}


// Read and  extract  CAN message values 
void CANDevice::readMessage() {
	// uint8_t oldSREG = SREG;
	// cli(); 	// disable inte
	if (!digitalRead(9))                  
	{
		CAN0.readMsgBuf(&rxId, &len, rxBuf); // Read data: len = data length, buf = data byte(s)	

	  switch (rxId)
		{
		case 0x23:
			id23_7 = rxBuf[7];
			break;
		case 0x40:
			id40_1 = rxBuf[1];
			id40_4 = fuel[index] = rxBuf[4];
			id40_5 = rxBuf[5];
			id40_6 = rxBuf[6];
			id40_7 = rxBuf[7];
			
			if (index < 9)
				index++;
			else
				index = 0;
			break;
		case 0x41:
			id41_2 = rxBuf[2];
			break;
		case 0x80:
			id80_3 = rxBuf[3];
			id80_2 = rxBuf[2];
			break;
		case 0x82:
			id82_0 = rxBuf[0];
			id82_1 = rxBuf[1];
			id82_4 = rxBuf[4];
			break;
		default:
			break;
		}
		//	Serial.println(str);
	}

	//SREG = oldSREG;
}


char* CANDevice::getMessage() {
	uint8_t oldSREG = SREG;
	cli(); 	// disable interrupts

	strcpy(canMessage, "{\"T\":2,\"A\":4095,\"M\":[");
	strcat(canMessage, itoa(id40_1, buf, 10));//0   
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id40_5, buf, 10));//1
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id40_6, buf, 10));//2
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id40_7, buf, 10));//3
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id41_2, buf, 10));//4
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id80_3, buf, 10));//5
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id82_0, buf, 10));//6
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id82_1, buf, 10));//7
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id82_4, buf, 10));//8
	strcat(canMessage, ",");
	strcat(canMessage, itoa(tpms, buf, 10));  //9
	strcat(canMessage, ",");
	strcat(canMessage, itoa(id23_7, buf, 10));//10
	strcat(canMessage, ",");

	// Average fuel consumption
	avgFC = 0;
	for (int i = 0; i < 10; i++)
		avgFC = avgFC+ fuel[i];
	avgFC = avgFC / 10;

	strcat(canMessage, itoa(avgFC, buf, 10));//11   id40_4
	strcat(canMessage, "]}");

	SREG = oldSREG;
	return canMessage;
}


void CANDevice::sendMessage(uint8_t command) {

	txData[4] = command;
	send();
	send();
 
}


void CANDevice::send()
{
	if (CAN0.sendMsgBuf(FUNCTIONAL_ID, 6, txData) == CAN_OK) {
		if (txData[5] == 255) {
			txData[5] = 0x00;
			if (txData[1] == 255)
				txData[1] = 0x00;
			else
				txData[1] = txData[1] + 1;
		}
		else
			txData[5] = txData[5] + 1;
	}
}

CANDevice canDevice;

