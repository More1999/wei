package Edu.Upc.Summer.Advance4.Gateway.Lora.Device;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276;
import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276_callback;


public class receive extends Thread implements sx1276_callback{


	final GpioController gpio = GpioFactory.getInstance();
	//private String Test_message = "0123456789";//发送的数据
	//private long LastStatsTime = 0;
//	private sx1276 myloradevice;
	
	public receive() {
		new sx1276(this,gpio);
		this.start();
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
	
	public static void main(String[] args) {
			new receive();
		}
	
	public void run(){
		System.out.println("Run Test");
		
	}

	@Override
	public void MessageReceiveed(String snifmodel) {
		// TODO Auto-generated method stub
		String rx_data;
		rx_data=hexStr2Str(snifmodel);
		System.out.println("This is "+rx_data);
		
	}

	@Override
	public void Error(int foutCode) {
		// TODO Auto-generated method stub
		
	}

}

