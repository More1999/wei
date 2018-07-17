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
	 * 16����ת����Ϊstring�����ַ���
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
	 * ��ȡʱ�������
	 * 
	 * @param s
	 * @return
	 */
	public String GetSimpleNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
		return sdf.format(new Date());
	}

	/**
	 * ���ݷ��ͣ������͵��������͸�������
	 * 
	 * @param s
	 * @return
	 */
	private void Httpsend(String message) throws ClientProtocolException, IOException {
		System.out.println("sendMessagepacket");/////////////////////////////////////
		// ����httpclient����
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("keyword", message));
		String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8)); // ͨ�����ഴ���Ķ������ģ�⴫ͳ��HTML������POST�����еĲ���
		HttpGet httpGet = new HttpGet("http://861067451.applinzi.com/fuwuqiver0.php" + "?" + str); // ����httpget���󣬷�������
		CloseableHttpResponse response = httpClient.execute(httpGet); // ���󷵻�һ��httpresponse
		HttpEntity entity = response.getEntity(); // �����ȡ����
		// ������Ӧͷ
		String content = EntityUtils.toString(entity, "utf-8");
		System.out.println(content);
		System.out.println(str);
		httpClient.close();
	}
	
	/**
	 * ���ݴ���ƴ�����ݰ�
	 * 
	 * @param s
	 * @return
	 */
	private String[] ProcessMessage(String data){
		String[] deldata;
		String time;
		deldata = data.split(",");
		time = GetSimpleNowTime();    //��ȡʱ���
		for (int i = 0; i < deldata.length; i++) {
			deldata[i]=deldata[i]+"/" + time + "/";
			  }
		return deldata;
	}

	/**
	 * ��ȡ���յ�������
	 * 
	 * @param s
	 * @return
	 */
	@Override
	public void MessageReceiveed(String snifmodel) {
		// TODO Auto-generated method stub
		rx_data = hexStr2Str(snifmodel);  //��16��������ת��ΪString����
		System.out.println("This is " + rx_data);
		sourceStrArray=ProcessMessage(rx_data);    //���ݴ��������ݼ���ʱ���
		try {
			for (int i = 0; i < sourceStrArray.length; i++) {
				System.out.println(sourceStrArray[i]);
				Httpsend(sourceStrArray[i]);   //�ϴ����ݵ�������
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
