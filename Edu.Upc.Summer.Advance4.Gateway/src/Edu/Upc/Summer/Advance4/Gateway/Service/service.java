package Edu.Upc.Summer.Advance4.Gateway.Service;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276;
import Edu.Upc.Summer.Advance4.Gateway.Lora.Device.sx1276_callback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class service extends Thread implements sx1276_callback {
	private String rx_data;
	public static String[] sourceStrArray ;

	final GpioController gpio = GpioFactory.getInstance();
	
	
	public service() {
		new sx1276(this, gpio);
		this.start();
	}

	public static void main(String[] args) {
		new service();
	}

	public void run() {
		System.out.println("Run Test");

	}
	
	/**
	 * 16进制转换成为string类型字符串
	 * 
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
	
	/**
	 * 获取时间戳函数
	 * 
	 * @param s
	 * @return
	 */
	public String GetSimpleNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
		return sdf.format(new Date());
	}

	/**
	 * 数据发送，将传送的数据推送给服务器
	 * 
	 * @param s
	 * @return
	 */
	private void Httpsend(String message) throws ClientProtocolException, IOException {
		System.out.println("sendMessagepacket");/////////////////////////////////////
		// 创建httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("keyword", message));
		String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8)); // 通过该类创建的对象可以模拟传统的HTML表单传送POST请求中的参数
		HttpGet httpGet = new HttpGet("http://861067451.applinzi.com/fuwuqiver0.php" + "?" + str); // 创建httpget对象，发送请求
		CloseableHttpResponse response = httpClient.execute(httpGet); // 请求返回一个httpresponse
		HttpEntity entity = response.getEntity(); // 请求获取对象
		// 接收响应头
		String content = EntityUtils.toString(entity, "utf-8");
		System.out.println(content);
		System.out.println(str);
		httpClient.close();
	}
	
	/**
	 * 数据处理，拼接数据包
	 * 
	 * @param s
	 * @return
	 */
	private String[] ProcessMessage(String data){
		String[] deldata;
		String time;
		deldata = data.split(",");
		time = GetSimpleNowTime();    //获取时间戳
		for (int i = 0; i < deldata.length; i++) {
			deldata[i]=deldata[i]+"/" + time + "/";
			  }
		return deldata;
	}

	/**
	 * 获取接收到的数据
	 * 
	 * @param s
	 * @return
	 */
	@Override
	public void MessageReceiveed(String snifmodel) {
		// TODO Auto-generated method stub
		rx_data = hexStr2Str(snifmodel);  //将16进制数据转化为String类型
		System.out.println("This is " + rx_data);
		sourceStrArray=ProcessMessage(rx_data);    //数据处理，给数据加上时间戳
		try {
			for (int i = 0; i < sourceStrArray.length; i++) {
				System.out.println(sourceStrArray[i]);
				Httpsend(sourceStrArray[i]);   //上传数据到服务器
				  }
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void Error(int foutCode) {
		// TODO Auto-generated method stub

	}

}
