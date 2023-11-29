#pragma once
#include "Lan.h"
#include "Messages.h"

/*
typedef enum {
	ACT_REGISTER = 1,
	ACT_INIT,
	ACT_INIT1,
	ACT_INIT2,
	ACT_INIT3,
	ACT_INIT4,
	ACT_INIT_END,
	ACT_INIT_END1,

	ACT_DEVSTATUS_E0,
	ACT_DEVSTATUS_E2,
	ACT_DEVSTATUS_E4,
	ACT_LAN_STATUS0,
	ACT_LAN_STATUS1,
	ACT_LAN_STATUS2,
	ACT_LAN_STATUS3,
	ACT_LAN_STATUS4,
	ACT_LAN_STATUS5,
	ACT_LAN_CHECK,

	ACT_TONE_BAL,
	ACT_TONE_BAL_INFO,

	ACT_TUNER_CURRENT,
	ACT_TUNER_STAITIONS,

	ACT_CDCH_INFO,


} ActionID;

*/

typedef enum {
	INIT_NONE,
	INIT_STARTED,
	INIT_SETMODE,
	INIT_FINISH
} InitStatus;


typedef enum {
	CONTROLLER_UNREG,
	CONTROLLER_REGISTERED,
} ControllerStatus;




typedef enum {
	EMPTY,
	REGISTER,
	INIT,
	GetSettingHU0,
	GetSettingHUCompleted,
	GetSettingHU2,
	GetSettingHU3,
	GetSettingHU4,
	GetSettingHU5,   
	GetSettingHU6,
	GetSettingHU7,
	GetSettingHU8,
	GetSettingHU9,  
	GetSettingHU10, 
	GetSettingHU11,
	GetSettingHU12,
	REGISTERED
} Status;

 



/*

const InMessageTable  mtMain[] PROGMEM = {
	{ ACT_LAN_STATUS0,    0x01,{ 0x1F } },

	{ ACT_INIT,		 0x11,{ 0x20, 0x02, 0x11, 0x06, 0x06, 0x00, 0x01, 0x02, 0x01, 0x06, 0x01, 0x0B, 0x01, 0x43, 0x01, 0x42, 0x01 } },           // AVC LAN init
	{ ACT_INIT1,	 0x03,{ 0x70, 0xC0, 0xD1 } },           // AVC LAN init
	{ ACT_INIT2,	 0x0A,{ 0x70, 0x02, 0x06, 0xD7, 0x00, 0x90, 0x00, 0x00, 0x37, 0x01 } },           // AVC LAN init
	{ ACT_INIT3,	 0x0B,{ 0x70, 0x06, 0x26, 0x16, 0x00, 0x00, 0x00, 0x02, 0x06, 0x01, 0x03 } },
	{ ACT_INIT4,	 0x0F,{ 0x70, 0x83, 0x00, 0x02, 0x00, 0xA8, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x09, 0x09, 0x00 } },

	// power off 0401015F01
};
const byte mtMainSize = sizeof(mtMain) / sizeof(InMessageTable);

 */
/*
const InMessageIgnoreFromPosition  inMesagesIgnorePos[] PROGMEM = {
	{ ACT_CDCH_INFO,		 0x16,{ 0x60, 0x02, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0xBB, 0xBB, 0x01, 0x05, 0xBB, 0xBB, 0xBB, 0xBB, 0xBB, 0xBB, 0x00 }, 3 },  //CD Changer info													 

	{ ACT_TUNER_CURRENT,     0x11,{ 0x60, 0x06, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x02, 0xBB, 0xBB, 0xBB, 0xB1, 0x05, 0x7B, 0x0B, 0xFF }, 3 },  //Radio Station
	{ ACT_TUNER_STAITIONS,   0x17,{ 0x60, 0x06, 0x02, 0x11, 0x02, 0xBB, 0x87, 0x9B, 0xBB, 0x87, 0x9B, 0xBB, 0x90, 0x1B, 0xBB, 0x98, 0x1B, 0xB1, 0x06, 0x1B, 0xB1, 0x07, 0x9B }, 3 },  //Radio Stations table
	{ ACT_TONE_BAL_INFO,     0x0E,{ 0x60, 0x83, 0x00, 0x00, 0x00, 0x00, 0x04, 0x03, 0x03, 0x02, 0x00, 0x00, 0x00, 0x00 }, 3 }, //TONE_BALANCE settings

	{ ACT_INIT_END,			 0x0C,{ 0x60 , 0xC0 , 0x00 , 0x0E , 0xFF , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 } , 5 },
	{ ACT_INIT_END1,		 0x0C,{ 0x60 , 0xC0 , 0x00 , 0x4F , 0xFF , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 , 0x00 } , 5 },
};
const byte inMesagesIgnorePosSize = sizeof(inMesagesIgnorePos) / sizeof(InMessageIgnoreFromPosition);


*/
/*
const InMessageTable  mtSearchHead[] PROGMEM = {
	{ ACT_REGISTER,  0x03,{ 0x01, 0x01, 0x5B } },
};
const byte mtSearchHeadSize = sizeof(mtSearchHead) / sizeof(InMessageTable);

const OutMessage CmdReset         PROGMEM = { MSG_DIRECT,	0x05,{ 0x00, 0x00, 0x00, 0x00, 0x00 } }; // reset AVCLan. This causes HU to send ACT_REGISTER
const OutMessage CmdRegister      PROGMEM = { MSG_DIRECT,       0x07,{ 0x11, 0, 0x01, 0x03, 0x92, 0x85,0x93 } }; // register CD-changer

const OutMessage CmdInit1         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x02, 0x10 } }; // init command Changer INFO
const OutMessage CmdInit2         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x06, 0x10 } }; // init command Current Station
//const OutMessage CmdInitSAT       PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x42, 0x10 } }; // init command for sat 260
const OutMessage CmdInit3         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0xC0, 0x10 } }; // init command 1
const OutMessage CmdInit4         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x83, 0x10 } }; // init command Tone BAL
const OutMessage CmdInit5         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x02, 0x00 } }; // init command 
const OutMessage CmdInit6         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x06, 0x00 } }; // init command
//const OutMessage CmdInitSAT1      PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x42, 0x00 } }; // init command  for sat 260
const OutMessage CmdInit7         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0xC0, 0x00 } }; // init command 1
const OutMessage CmdInit8         PROGMEM = { MSG_DIRECT,		0x03,{ 0x40, 0x83, 0x00 } }; // init command 

//after < b 130 FFF C 60 c0 00 0e ff 00 00 00 00 00 00 00
const OutMessage CmdInit9         PROGMEM = { MSG_DIRECT,		0x04,{ 0x40, 0x02, 0x02, 0x00 } }; // 
const OutMessage CmdInit10        PROGMEM = { MSG_DIRECT,		0x05,{ 0x40, 0x06, 0x02, 0x00, 0x01 } }; // 
const OutMessage CmdInit11        PROGMEM = { MSG_DIRECT,		0x05,{ 0x40, 0x02, 0x02, 0x00, 0x10 } }; // 



const OutMessage CmdInitEnd       PROGMEM = { MSG_DIRECT,		0x02,{ 0x13, 0xFF } }; // init command 
const OutMessage CmdInitEnd1      PROGMEM = { MSG_DIRECT,		0x04,{ 0xD0, 0x31, 0x0B,0x00 } }; //  finish init 

*/




class LanNAVI {
public:
	byte sendGetInit = 13;

	Status status;
	ControllerStatus controllerStatus;
	InitStatus initStatus;


	void begin();                    // initialisation
	void getActionID();               // get action id by recieved message
	void processAction(IncomeActionID);  // processing incoome actions
	void InitDevice();
	 
	void sendInit1();
	void sendReInit();
	void startSAT();
	void sendInit2();
	void sendInit3();
	void processEvent(EventID);    // process event  
	byte sendStatus();

};

extern LanNAVI lanDevice;

