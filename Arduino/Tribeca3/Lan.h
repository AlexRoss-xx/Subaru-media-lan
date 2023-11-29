#include "Arduino.h"

// maximum number of attempts to send a message
#define MAXSENDATTEMP	1

//Subaru spec

#define CONTROL_FLAGS	0xE

#define MAXMSGLEN		32


 
#define EV_NONE	 1	// no event
 

#define CLEAR_LENGTH				 0x2D

#define START_BIT_LENGTH			 0x2D
#define START_BIT_HOLD_ON_LENGTH	 0x10
#define NORMAL_BIT_LENGTH			 0x42
#define BIT_0_HOLD_ON_LENGTH		 0x39
#define BIT_1_HOLD_ON_LENGTH		 0x20 
#define U_LENGTH					 0x02  // 1 uS * (F_CPU / 1000000L / 8)  
#define BIT_0_HOLD_ON_MIN_LENGTH	 0x20

#define SEND_START_BIT_LENGTH			 0x32   // 0x30
#define SEND_START_BIT_HOLD_ON_LENGTH	 0x2D   // 0x2A 
#define SEND_NORMAL_BIT_LENGTH			 0x4D  // 0x4A
#define SEND_BIT_0_HOLD_ON_LENGTH		 0x4A	// 0x38
#define SEND_BIT_1_HOLD_ON_LENGTH		 0x28   //0x20 
#define SEND_U_LENGTH					 0x02   // 1 uS * (F_CPU / 1000000L / 8)  
#define SEND_BIT_0_HOLD_ON_MIN_LENGTH	 0x3C





typedef enum {
	EV_NOEVENT,
	EV_STATUS,
	EV_QUEUE_PUSH,
} EventID;


typedef enum
{   // No this is not a mistake, broadcast = 0!
	MSG_DIRECT = 1,
	MSG_BROADCAST = 0
} TransmissionMode;



typedef struct
{
	byte	actionID;           // Action id
	byte	dataSize;           // message size (bytes)
	byte	data[32];           // message
} InMessageTable;

typedef struct
{
	byte	actionID;           // Action id
	byte	dataSize;           // message size (bytes)
	byte	data[32];           // message
	word	mask;				// mask, set bit = 1 in not checked position (1<<5 or _BV(5) - datap[5] not checked)
} InMaskedMessageTable;

typedef struct
{
	byte	actionID;           // Action id
	byte	dataSize;           // message size (bytes)
	byte	data[32];           // message
	byte	ignorePositions[32]; 				// mask, set bit = 1 in not checked position (1<<5 or _BV(5) - datap[5] not checked)
} InMessageIgnorePostions;

typedef struct
{
	byte	actionID;           // Action id
	byte	dataSize;           // message size (bytes)
	byte	data[32];           // message
	byte	startIgnore; 				// mask, set bit = 1 in not checked position (1<<5 or _BV(5) - datap[5] not checked)
} InMessageIgnoreFromPosition;


typedef struct
{
	TransmissionMode	broadcast;          // Transmission mode: normal (1) or broadcast (0).
	byte                dataSize;           // message size (bytes)
	byte                data[32];           // message
} OutMessage;


class  LanDrv {
public:

	bool	busError;
	bool	broadcast;
	word	masterAddress;
	word	slaveAddress;
	word	deviceAddress;
	word    deviceAddressSAT=-1;
	word    deviceAddressCDC=-1;
	word	headAddress;
	byte	dataSize;
	byte	message[MAXMSGLEN];
	EventID	event;
	byte	actionID;
	bool	readonly;
	bool	isPrintMessage;


	void	begin();
	byte	readMessage(void);
	byte	sendMessage(void);
	byte	sendMessage(OutMessage*);
	void 	printMessage(bool incoming);
	bool	isAvcBusFree(void);
	void	loadMessage(OutMessage*);
	void	loadMessage(OutMessage*, word	deviceAddress);
	byte	getActionID(InMessageTable messageTable[], byte mtSize);
	byte	getActionID(InMaskedMessageTable messageTable[], byte mtSize);
	byte	getActionID(InMessageIgnoreFromPosition messageTable[], byte mtSize);
	//private:
	bool	_parityBit;
	word	readBits(byte nbBits);
	byte	_readMessage(void);
	byte	_sendMessage(void);
	void	send1BitWord(bool data);
	void	sendStartBit(void);
	void	send4BitWord(byte data);
	void	send8BitWord(byte data);
	void	send12BitWord(word data);
	bool	readAcknowledge(void);
	bool	handleAcknowledge(void);
	void	_printHEX(uint8_t *data, uint8_t length);
};

extern LanDrv lan;