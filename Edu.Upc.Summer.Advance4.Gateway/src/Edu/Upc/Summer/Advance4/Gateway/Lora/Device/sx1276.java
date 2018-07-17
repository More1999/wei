package Edu.Upc.Summer.Advance4.Gateway.Lora.Device;

import java.io.IOException;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
//import org.bouncycastle.util.encoders.Base64;
//import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

//import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.SnifModel;
//import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.JsonUpRxpk;
import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.KeysHolder;
//import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.JsonUpRxpk.rxpk;

import Edu.Upc.Summer.Advance4.Gateway.Lora.Crypto.LoraCrypto;
import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276;
import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276_callback;

public class sx1276 extends Thread {
	public static int REG_FIFO                    	= 0x00;//数据输入输出寄存器
	public static int REG_OPMODE                  	= 0x01;//模式切换寄存器
	public static int REG_FIFO_ADDR_PTR           	= 0x0D;//Lora接收数据写入位置的动态指针，SpI接口指针
	public static int REG_FIFO_TX_BASE_AD         	= 0x0E;//起始Tx数据
	public static int REG_FIFO_RX_BASE_AD         	= 0x0F;//起始Rx数据
	public static int REG_FIFO_RX_CURRENT_ADDR    	= 0x10;//显示最后接收的数据包在FIFO数据缓存中的存储位置
	public static int REG_IRQ_FLAGS_MASK          	= 0x11;//可选IRQ标志屏蔽
	public static int REG_IRQ_FLAGS               	= 0x12;//IRQ标志
	public static int REG_RX_NB_BYTES             	= 0x13;//表示到现在为止收到的字节数
	public static int REG_PKT_SNR_VALUE				= 0x19;//最后接收到的数据包的SNR预估值
	public static int REG_PKT_RSSI_VALUE			= 0x1A;//最后接收到的数据包的RSSI预估值
	public static int REG_RSSI_VALUE				= 0x1B;//电流RSSI值
	public static int REG_MODEM_CONFIG            	= 0x1D;//调制解调器配置1
	public static int REG_MODEM_CONFIG2           	= 0x1E;//调制解调器配置2
	public static int REG_SYMB_TIMEOUT_LSB  		= 0x1F;//接收机超时
	public static int REG_PAYLOAD_LENGTH          	= 0x22;//负载字节长度
	public static int REG_MAX_PAYLOAD_LENGTH 		= 0x23;//负载长度最大值
	public static int REG_HOP_PERIOD              	= 0x24;//频率跳变之间的符号周期
	public static int REG_MODEM_CONFIG3           	= 0x26;//调制解调器配置3
	public static int REG_SYNC_WORD					= 0x39;//
	public static int REG_DIO_MAPPING_1           	= 0x40;//DIO0到DIO3引脚映射
	public static int REG_DIO_MAPPING_2           	= 0x41;//DIO4到DIO5引脚映射、clkout频率	
	public static int REG_VERSION	  				= 0x42; //芯片版本
	public static int REG_4D_PA_DAC					= 0x4d;
	
	public static int PAYLOAD_LENGTH              	= 0x40;
	
	public static int SX7X_MODE_SLEEP             	= 0x80;
	public static int SX7X_MODE_STANDBY           	= 0x81;
	public static int SX7X_MODE_TX                	= 0x83;
	public static int SX7X_MODE_RX_CONTINUOS      	= 0x85;
	
	// FRF
	public static int REG_FRF_MSB          			= 0x06;
	public static int REG_FRF_MID          			= 0x07;
	public static int REG_FRF_LSB          			= 0x08;

	public static int FRF_MSB              			= 0x69; // 420 Mhz
	public static int FRF_MID              			= 0x00;
	public static int FRF_LSB              			= 0x00;
	
	// LOW NOISE AMPLIFIER
	public static int REG_LNA                     	= 0x0C;//LNA设置
	public static int LNA_MAX_GAIN                	= 0x23;
	public static int LNA_OFF_GAIN                	= 0x00;
	public static int LNA_LOW_GAIN		    		= 0x20;

	public static int PA_DAC_DISABLE              	= 0x04;
	public static int PA_DAC_ENABLE              	= 0x07;
	
	public static SpiDevice spi = null;
	private GpioPinDigitalOutput RstPin;
	private GpioPinDigitalOutput SSPin;
	private GpioPinDigitalInput DIOPin;
	private Pin ssPin 	= RaspiPin.GPIO_10;  		//pin 10
	private Pin dio0 	= RaspiPin.GPIO_07;			//pin 7
	private Pin RST 	= RaspiPin.GPIO_00;			//pin 0
	private GpioController gpio;
	private boolean LastSend = false;
	private sx1276_callback callback;
	private KeysHolder keyS = new KeysHolder();
	private LoraCrypto crypto;
	private String DeviceAddress;
	private int messageCounter = 0;
	
	public sx1276(sx1276_callback _callback, GpioController _gpio){
		new sx1276(_callback,_gpio,null);
	}
	
	public sx1276(sx1276_callback _callback, GpioController _gpio, String _DeviceAddress){
		gpio = _gpio;
		callback = _callback;
		DeviceAddress = _DeviceAddress;
		InitStuff();
		if (_DeviceAddress!=null) SetupKeys();
		try {
			SetupLoRa();
			this.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void run(){
		while(true){
			try {
				//System.out.println(Thread.currentThread().getName()+" is running 22");//显示当前的线程名
				sleep(10000);
			} catch (InterruptedException e) {
				//swall
			}
		}
	}
	
	public void SendMessage(String Message){          //将string类型转换为byte型进行传输
		byte[] message = Message.getBytes();
		SendMessagePacket(message);
	}
	
	private byte[] GetMessage(){
		writeSPI(REG_IRQ_FLAGS,0x40);  //clear rx
		int irqflags = readSPI(REG_IRQ_FLAGS);
		System.out.println("irqflags = "+irqflags);
		
		//System.out.println("zzzzzzzz my irqflags");///////////////////////////////////////////
		
		if ((irqflags & 0x20) == 0x20){
			System.out.println("CRC Error");
			writeSPI(REG_IRQ_FLAGS, 0x20);
			return null;
		}
		int currentAddr = readSPI(REG_FIFO_RX_CURRENT_ADDR);   //最后一个数据的地址
		int receivedCount = readSPI(REG_RX_NB_BYTES);
		writeSPI(REG_FIFO_ADDR_PTR,currentAddr);             //将指针指向缓冲区入口
		byte[] res = new byte[receivedCount];               //接收的数据长度
		for (int i = 0; i < receivedCount; i++){            //读出缓冲区的数据
			int c = readSPI(REG_FIFO) & 0xff;
			res[i] = (byte) c;
		}
		return res;
	}
	
	private void SendMessagePacket(byte[] message){
		System.out.println("me sendMessagepacket");/////////////////////////////////////
		writeSPI(REG_OPMODE, SX7X_MODE_STANDBY);
		LastSend = true;
		System.out.println("SendMessagePacket");
		writeSPI(sx1276.REG_FIFO_ADDR_PTR,0);
		for (Byte b : message){
			writeSPI(REG_FIFO, b.intValue());
		}
		writeSPI(REG_PAYLOAD_LENGTH,message.length);
		writeSPI(REG_OPMODE, SX7X_MODE_TX);
		writeSPI(REG_DIO_MAPPING_1,0x40);
	}
	
	private void selectSPI(){
		SSPin.low();
	}
	
	private void unselectSPI(){
		SSPin.high();
	}
	
	private int readSPI(int addr){
		byte spibuf[] = new byte[2];
		int res = 0x00;
		spibuf[0] = (byte)(addr);
		spibuf[1] = 0x00;
		selectSPI();
		try {
			byte[] result = spi.write(spibuf);
			res = result[1];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unselectSPI();
		return res;
	}
	
	private void writeSPI(int addr, int value){
		byte spibuf[] = new byte[2];
		spibuf[0] = (byte)(addr | 0x80);
		spibuf[1] = (byte)value;
		selectSPI();
		try {
			spi.write(spibuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unselectSPI();
	}
	
	public void SendEncPayloadMessage(String Message){
		byte[] mdata = crypto.LoraEncrypter(Message, messageCounter++);
		SendMessagePacket(mdata);
	}
	
	public String DecriptMessage(byte[] messin){
		return crypto.LoraDecrypter(messin);
	}
	
	/**
	 * 16进制转换成为string类型字符串
	 * @param s
	 * @return
	 */
	public static String hexStr2Str(String hexStr) {
	    String str = "0123456789abcdef";
	    char[] hexs = hexStr.toCharArray();
	    byte[] bytes = new byte[hexStr.length() / 2];
	    int n;
	    for (int i = 0; i < bytes.length; i++) {
	        n = str.indexOf(hexs[2 * i]) * 16;
	        n += str.indexOf(hexs[2 * i + 1]);
	        bytes[i] = (byte) (n & 0xff);
	    }
	    return new String(bytes);
	}
	
	private void ReceiveMessagePacket(){
		if (LastSend){
			LastSend = false;
			writeSPI(REG_OPMODE, SX7X_MODE_RX_CONTINUOS);
			writeSPI(0x40,0x00);
		} else {
			if (DIOPin.isHigh()){
				
				//System.out.println(" heihei my receivedPacket");////////////////////////////////////////
				
				byte[] message = GetMessage();
				if (message!=null){
					//System.out.println("Message = "+ ByteUtils.toHexString(message) + " length = "+message.length);
					
					String rx_data;
					rx_data=hexStr2Str(ByteUtils.toHexString(message));
					System.out.println("Message = "+ rx_data + " length = "+message.length);
					
					//System.out.println("jjjjjjjjjj my message deal");/////////////////////////////////////
					
					//SnifModel sm = ProcessMessage(message);
					
					//System.out.println("This is "+sm);//////////////////////////////////////////
					
					if (callback !=null) callback.MessageReceiveed(ByteUtils.toHexString(message));
				}
			}
		}
	}
	
//	private SnifModel ProcessMessage(byte[] message){                        //包处理函数
//		System.out.println("ProcessMessage");
//		
//		System.out.println("123my ProcessMessage");///////////////////////////////////////////
//		
//		SnifModel sm = new SnifModel();
//		sm.bdata = ByteUtils.toHexString(message);
//		int value = readSPI(sx1276.REG_PKT_SNR_VALUE);
//		int SNR = (value & 0xFF) >> 2;
//		int rssi = readSPI(sx1276.REG_RSSI_VALUE)-157;
//
//		System.out.println("uclear snifmodel hhhhhhhhhhhhhhhhhhhhhh1.2.1");//////////////////////////////////
//		
//		//int A = message[6];
//		//int B = message[7];
//		//if (message[6]<0) A = 256 + message[6];
//		//if (message[7]<0) B = 256 + message[7];
//		//int sequence = 256*B+A;
//		// Get the adress
//		//byte[] addrbytes = Arrays.copyOfRange(message, 1, 5);
//		//int address = java.nio.ByteBuffer.wrap(addrbytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
//		//String addr = Integer.toHexString(address).toUpperCase();
//		//byte[] header = new byte[9];
//		//for (int i = 0; i < 9 ; i++){
//		//	header[i] = message[i];
//		//}
//		String BaseData = new String(Base64.encode(message));
//	    DateTime dt = new DateTime();
//		//JsonUpRxpk us = new JsonUpRxpk();
//		rxpk rx= new rxpk();
//		rx.tmst = dt.getSecondOfDay()*10000;
//		//rx.freq = 420;
//		//rx.chan = 0;
//		//rx.rfch = 0;
//		//rx.stat = 1;
//		//rx.modu = "LORA";
//		//rx.datr = "SF7BW125";
//		//rx.codr = "4/5";
//		rx.rssi = rssi;
//		rx.lsnr = SNR;
//		rx.size = message.length;
//		rx.data = BaseData;
//		//sm.address = address;
//		//sm.sequence = sequence;
//		sm.rssi = rssi;
//		sm.rXpk = rx;
//		//sm.header = ByteUtils.toHexString(header);
//		System.out.println("uclear "+sm);//////////////////////////////////
//		return sm;
//	}
	
	private boolean SetupLoRa() throws Exception{
		System.out.println("SetupLoRa");
		RstPin.low();
		SSPin.low();
		sleep(100);
		RstPin.high();
		//SSPin.high();
		sleep(100);
		int version = readSPI(REG_VERSION);
		if (version == 0x12){
			System.out.println("SX1276 detected, starting");
		} else {
			System.out.println("Unrecornized transceiver. version 0x"+Integer.toHexString(version));
			return false;
		}
		writeSPI(REG_OPMODE, SX7X_MODE_SLEEP);
		//Set freq
		writeSPI(REG_FRF_MSB, FRF_MSB);
		writeSPI(REG_FRF_MID, FRF_MID);
		writeSPI(REG_FRF_LSB, FRF_LSB);
		//LoRaWAN public sync word
		writeSPI(REG_SYNC_WORD, 0x34);	
		writeSPI(REG_MODEM_CONFIG3, 0x04);
		writeSPI(REG_MODEM_CONFIG, 0x72);
		writeSPI(REG_MODEM_CONFIG2, 0x74);	
		writeSPI(REG_SYMB_TIMEOUT_LSB, 0x08);
		writeSPI(REG_MAX_PAYLOAD_LENGTH, 0x80);
		writeSPI(REG_PAYLOAD_LENGTH, 0x40);
		writeSPI(REG_HOP_PERIOD, 0xFF);
		writeSPI(REG_FIFO_ADDR_PTR, readSPI(REG_FIFO_RX_BASE_AD));
		writeSPI(REG_FIFO_TX_BASE_AD,0x00);
		
		/*writeSPI(REG_FIFO_ADDR_PTR, readSPI(REG_FIFO_TX_BASE_AD));
		writeSPI(REG_FIFO_RX_BASE_AD,0x00);*/
		
		//Set Continous Receive Mode
		writeSPI(REG_LNA, sx1276.LNA_MAX_GAIN);
		writeSPI(REG_4D_PA_DAC, PA_DAC_ENABLE );
		writeSPI(REG_OPMODE, SX7X_MODE_RX_CONTINUOS);
		
		//Set Continous Send Mode
//		//writeSPI(REG_LNA, sx1276.LNA_MAX_GAIN);
//		/*writeSPI(REG_4D_PA_DAC, PA_DAC_ENABLE );
//		writeSPI(REG_OPMODE, SX7X_MODE_TX);*/
		return true;
	}
	
	public void ReceiverOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_RX_CONTINUOS);
	}
	
	public void transmitterOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_TX);
		writeSPI(REG_DIO_MAPPING_1,0x40);
		LastSend = true;
	}
	
	public void StandbyOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_STANDBY);
	}
	
	public void SleepOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_SLEEP);
	}
	
	private boolean SetupKeys(){
		if (keyS.LoadKeysFromFile(DeviceAddress)){
			int da = Integer.parseInt(keyS.getDeviceAddress(), 16);
			crypto = new LoraCrypto(keyS.getAppSessionKey(),keyS.getNetworkSessionKey(),da);
			return true;
		}
		return false;
	}
	
	private void InitStuff(){
		System.out.println("InitStuff");
		try {
			//SpiDevice.DEFAULT_SPI_SPEED
			spi = SpiFactory.getInstance(SpiChannel.CS0,500000,SpiMode.MODE_0);
			RstPin = gpio.provisionDigitalOutputPin(RST, "RST", PinState.HIGH);
			RstPin.setShutdownOptions(true, PinState.LOW);
			RstPin.low();
//			SSPin = gpio.provisionDigitalOutputPin(ssPin, "SS", PinState.HIGH);
//			SSPin.setShutdownOptions(true, PinState.LOW);
//			SSPin.low();
			
			DIOPin = gpio.provisionDigitalInputPin(dio0,PinPullResistance.PULL_DOWN);
			DIOPin.setShutdownOptions(true);
			DIOPin.addListener(new GpioPinListenerDigital(){
				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
					//引脚状态变化回调函数
					if (arg0.getEdge() == PinEdge.RISING){
						System.out.println("ReceiveMessage");
						Logger.info("ReceiveMessage");
						
						//System.out.println("hahahahha my receive");////////////////////////////////////////
						
						ReceiveMessagePacket();
					}
				}
			});
			SSPin = gpio.provisionDigitalOutputPin(ssPin, "SS", PinState.HIGH);
			SSPin.setShutdownOptions(true, PinState.LOW);
			SSPin.high();
			RstPin.high();
			
//			RstPin = gpio.provisionDigitalOutputPin(RST, "RST", PinState.HIGH);
//			RstPin.setShutdownOptions(true, PinState.LOW);
//			RstPin.high();
//			SSPin.high();
			//System.out.println("123my InitStuff");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

