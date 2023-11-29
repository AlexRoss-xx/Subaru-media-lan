
// The main program  to emulate  subaru media devices and MFA or Color monitor  unit 
// Board  Arduino Nano (FTDI)
// Dependency Library :
//  https://www.arduino.cc/reference/en/libraries/qlist/
//  https://github.com/mpflaga/Arduino-MemoryFree
//  https://github.com/coryjfowler/MCP_CAN_lib
// Project was made using Visual Studio Micro, but can be easily adopted for Arduino IDE or PlatformIO
// Most of code contains commented  blocks, they helped me for reverse enginering  of  CAN & LAN  protocols , I do not remove tham becouse  I see that there are more info that can be decoded 




#include "mcp_can_dfs.h"
#include "mcp_can.h"
#include "Config.h"
#include "LanNAVI.h"
// #include "QueueArray.h"
#include "Android.h"
#include "CANDevice.h"
// #include <ArduinoJson.h>

// #include "MemoryFree.h"


//int busoff = 0;
boolean buson = 0;
boolean tpms = 0;
uint8_t prev_interval_tpms = 0;
uint16_t interval = 15000;

uint16_t  currentduration = 0;


#define BUS_OFF_IN_PORT	PORTD
#define BUS_OFF_IN_PIN	PIND
#define BUS_OFF_IN		2

#define AUDIO_MUTE_IN_PORT	PORTD
#define AUDIO_MUTE_IN_PIN	PIND
#define AUDIO_MUTE_IN		4

#define TPMS_IN_PORT	PORTD
#define TPMS_IN_PIN	PIND
#define TPMS_IN		5

#define INPUT_BUS_OFF_SET	(bit_is_set(BUS_OFF_IN_PIN, BUS_OFF_IN))
#define INPUT_BUS_OFF_CLEAR	(bit_is_clear(BUS_OFF_IN_PIN, BUS_OFF_IN))

#define INPUT_AUDIO_MUTE_SET	(bit_is_set(AUDIO_MUTE_IN_PIN, AUDIO_MUTE_IN))
#define INPUT_AUDIO_MUTE_CLEAR	(bit_is_clear(AUDIO_MUTE_IN_PIN, AUDIO_MUTE_IN))

#define INPUT_TPMS_SET		(bit_is_set(TPMS_IN_PIN, TPMS_IN))
#define INPUT_TPMS_CLEAR	(bit_is_clear(TPMS_IN_PIN, TPMS_IN))

//#define READONLY


void setup()
{
	Serial.begin(2000000);

	cbi(BUS_OFF_IN_PORT, BUS_OFF_IN);

	cbi(AUDIO_MUTE_IN_PORT, AUDIO_MUTE_IN);
	cbi(TPMS_IN_PORT, TPMS_IN);

	sbi(AUDIO_OUT_DDR, AUDIO_OUT);

	TRANSMIT_0;

	TRANSMIT_AUDIO_ON;

	lan.begin();
	lan.headAddress = 0x130;
	lan.deviceAddress = 0x140; // ID 140 HEADUNIT
#ifdef SAT
	lan.deviceAddressSAT = 0x260; // ID 260
#endif // SAT	
#ifdef CDC  // CD changer
	lan.deviceAddressCDC = 0x230; // ID 230 
#endif // 

	lanDevice.begin();
	lan.readonly = false;

	//lanDevice.status = REGISTER;
	//lanDevice.devStatus = REG;

#ifdef READONLY
	lan.readonly = true;
	lan.isPrintMessage = true;
#endif

	// lan.isPrintMessage = true;
	// lan.readonly = true;

	android.begin();
	canDevice.begin();
}

void loop()
{
	if (INPUT_BUS_ON_SET)
	{
		if (buson == 0) {
			buson = 1;
			Serial.begin(2000000);			
		}
		
		byte res = lan.readMessage();
		if (!res) {
#ifdef READONLY
			if (!lan.readonly)
#endif
			{
				android.procmessage = true;
				//lan.event = EV_QUEUE_PUSH;
				lanDevice.getActionID();
				//Serial.println(lan.actionID);

				if (lan.actionID != ACT_NONE) {
					lanDevice.processAction((IncomeActionID)lan.actionID);
				}

				switch (lan.event) {
				case EV_NOEVENT:					 
					// Serial.println("noevent");
					break;

				case EV_STATUS:
                     // CAN unit have to send periditical message , signaling that device works
					canDevice.sendMessage(0x00);					 
					// sendStatus();
					// lan.event &= ~EV_STATUS;
					break;

				case EV_QUEUE_PUSH:
					if (lan.masterAddress == 0x130) {
						if (lan.slaveAddress != 0x230) {
							// Serial.println("push");
							android.PutMessage(LAN, lan.slaveAddress, lan.dataSize, lan.message);
						}
					}
					else if (lan.masterAddress == 0x230 && android.status == 1) {
						// Serial.println("push2");
						android.PutMessage(LAN, lan.slaveAddress, lan.dataSize, lan.message);
					}
					break;

				default:
					break;
				};
			}
		}

#ifdef READONLY
		if (!lan.readonly)
		{
#endif	
			android.ProceedCommand();
			if (lanDevice.status == REGISTER)
			{	
				if (android.status == 1)
				{
					android.ProcessQueue();
					canDevice.readMessage();
#ifdef TPMS
					checkTPMS();
#endif					
				}
			}
#ifdef READONLY
		}
#endif
	}
	else if (INPUT_BUS_ON_CLEAR)
		if (buson == 1) {
			/*	Serial.print("freeMemory()=");
				Serial.println(freeMemory());
				Serial.println("BUS OFF");*/

			buson = 0;
			/* lanDevice.begin();
			   android.begin();*/
			// lanDevice.begin();
			// android.begin();
			canDevice.tpms = 0;

			Serial.clearWriteError();
			 
			while (Serial.available())
				Serial.read();
			Serial.end();
			
		}
}

#ifdef TPMS
void checkTPMS() {
	if (INPUT_TPMS_SET) {
		// Serial.println("TPMS SET");
		if (tpms == 0) {
			tpms = 1;

			if (prev_interval_tpms >= 5) {
				prev_interval_tpms = 0;				
				if (currentduration > 3000) {
					if (currentduration < 5500 && currentduration > 3500)
						canDevice.tpms = 1;
					else
						canDevice.tpms = 0;
				}
			}
			else
				prev_interval_tpms += 1;
		}
	}

	if (INPUT_TPMS_CLEAR) {
		// Serial.println("TPMS clear");
		if (tpms == 1) {
			tpms = 0;
			// Serial.println(prev_interval_tpms);
			currentduration = 0;
		}

		if (currentduration > 15000) {
			canDevice.tpms = 0;
			currentduration = 0;
		}
		else
			currentduration += 1;
	}
}
#endif
