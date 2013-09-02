#ifndef _RTL8192C_RECV_H_
#define _RTL8192C_RECV_H_

#include <drv_conf.h>
#include <osdep_service.h>
#include <drv_types.h>


#ifdef PLATFORM_OS_XP
	#ifdef CONFIG_SDIO_HCI
		#define NR_RECVBUFF 1024//512//128
	#else
		#define NR_RECVBUFF (16)
	#endif
#elif defined(PLATFORM_OS_CE)
	#ifdef CONFIG_SDIO_HCI
		#define NR_RECVBUFF (128)
	#else
		#define NR_RECVBUFF (4)
	#endif
#else
	#define NR_RECVBUFF (4)
	#define NR_PREALLOC_RECV_SKB (8)
#endif

#define RXDESC_SIZE	24
#define RXDESC_OFFSET RXDESC_SIZE

#define RECV_BLK_SZ 512
#define RECV_BLK_CNT 16
#define RECV_BLK_TH RECV_BLK_CNT

//#define MAX_RECVBUF_SZ 2048 // 2k
//#define MAX_RECVBUF_SZ (8192) // 8K
//#define MAX_RECVBUF_SZ (16384) //16K
//#define MAX_RECVBUF_SZ (16384 + 1024) //16K + 1k
//#define MAX_RECVBUF_SZ (30720) //30k
//#define MAX_RECVBUF_SZ (30720 + 1024) //30k+1k
//#define MAX_RECVBUF_SZ (32768) // 32k

#if defined(CONFIG_SDIO_HCI)

#define MAX_RECVBUF_SZ (50000) //30k //(2048)//(30720) //30k

#elif defined(CONFIG_USB_HCI)

#ifdef PLATFORM_OS_CE
#define MAX_RECVBUF_SZ (8192+1024) // 8K+1k
#else
//#define MAX_RECVBUF_SZ (32768) // 32k
//#define MAX_RECVBUF_SZ (16384) //16K
//#define MAX_RECVBUF_SZ (10240) //10K
#define MAX_RECVBUF_SZ (15360) // 15k < 16k
#endif

#endif

#define RECV_BULK_IN_ADDR		0x80
#define RECV_INT_IN_ADDR		0x81

#define RECVBUFF_ALIGN_SZ 512

#define RSVD_ROOM_SZ (0)


//These definition is used for Rx packet reordering.
#define SN_LESS(a, b)		(((a-b)&0x800)!=0)
#define SN_EQUAL(a, b)	(a == b)
//#define REORDER_WIN_SIZE	128
//#define REORDER_ENTRY_NUM	128
#define REORDER_WAIT_TIME	(30) // (ms)


struct recv_stat
{
	unsigned int rxdw0;

	unsigned int rxdw1;

	unsigned int rxdw2;

	unsigned int rxdw3;

	unsigned int rxdw4;

	unsigned int rxdw5;
};

struct phy_cck_rx_status
{
	/* For CCK rate descriptor. This is a unsigned 8:1 variable. LSB bit presend
	   0.5. And MSB 7 bts presend a signed value. Range from -64~+63.5. */
	u8	adc_pwdb_X[4];
	u8	sq_rpt;
	u8	cck_agc_rpt;
};

struct phy_stat
{
	unsigned int phydw0;

	unsigned int phydw1;

	unsigned int phydw2;

	unsigned int phydw3;

	unsigned int phydw4;

	unsigned int phydw5;

	unsigned int phydw6;

	unsigned int phydw7;
};
#define PHY_STAT_GAIN_TRSW_SHT 0
#define PHY_STAT_PWDB_ALL_SHT 4
#define PHY_STAT_CFOSHO_SHT 5
#define PHY_STAT_CCK_AGC_RPT_SHT 5
#define PHY_STAT_CFOTAIL_SHT 9
#define PHY_STAT_RXEVM_SHT 13
#define PHY_STAT_RXSNR_SHT 15
#define PHY_STAT_PDSNR_SHT 19
#define PHY_STAT_CSI_CURRENT_SHT 21
#define PHY_STAT_CSI_TARGET_SHT 23
#define PHY_STAT_SIGEVM_SHT 25
#define PHY_STAT_MAX_EX_PWR_SHT 26

// Rx smooth factor
#define	Rx_Smooth_Factor (20)

union recvstat {
	struct recv_stat recv_stat;
	unsigned int value[RXDESC_SIZE>>2];
};


struct recv_buf{

	_list list;

	_lock recvbuf_lock;

	u32	ref_cnt;

	_adapter  *adapter;

#ifdef CONFIG_SDIO_HCI
#ifdef PLATFORM_OS_XP
	PMDL mdl_ptr;
#endif
	u8	cmd_fail;
#endif


#ifdef CONFIG_USB_HCI

	#if defined(PLATFORM_OS_XP)||defined(PLATFORM_LINUX)
	PURB	purb;

	#endif

	#ifdef PLATFORM_OS_XP
		PIRP		pirp;
	#endif

	#ifdef PLATFORM_OS_CE
		USB_TRANSFER	usb_transfer_read_port;
	#endif

	u8  irp_pending;
	int  transfer_len;

#endif

#ifdef PLATFORM_LINUX
	_pkt *pskb;
	u8 reuse;
#endif

	uint  len;
	u8 *phead;
	u8 *pdata;
	u8 *ptail;
	u8 *pend;

	u8 *pbuf;
	u8 *pallocated_buf;


};


/*
	head  ----->

		data  ----->

			payload

		tail  ----->


	end   ----->

	len = (unsigned int )(tail - data);

*/
struct recv_frame_hdr{

	_list	list;
	_pkt	*pkt;
	_pkt *pkt_newalloc;

	_adapter  *adapter;
	
	u8 fragcnt;

	int frame_tag;

	struct rx_pkt_attrib attrib;

	uint  len;
	u8 *rx_head;
	u8 *rx_data;
	u8 *rx_tail;
	u8 *rx_end;

	void *precvbuf;


	//
	struct sta_info *psta;

	//for A-MPDU Rx reordering buffer control
	struct recv_reorder_ctrl *preorder_ctrl;

};


union recv_frame{

	union{
		_list list;
		struct recv_frame_hdr hdr;
		uint mem[RECVFRAME_HDR_ALIGN>>2];
	}u;

	//uint mem[MAX_RXSZ>>2];

};


int init_recvbuf(_adapter *padapter, struct recv_buf *precvbuf);

void rtl8192cu_update_recvframe_attrib_from_recvstat(_adapter *padapter,union recv_frame *precvframe, struct recv_stat *prxstat);

void reordering_ctrl_timeout_handler(void *pcontext);

void rtl8192c_query_rx_phy_status(union recv_frame *prframe, struct recv_stat *prxstat);

//void rtl8192c_process_phy_info(_adapter *padapter, union recv_frame *prframe);
void rtl8192c_process_phy_info(_adapter *padapter, void *prframe);

#endif

