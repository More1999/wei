package Edu.Upc.Summer.Advance4.Gateway.Lora.Httpclient;

//import java.io.IOException;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//public class Httpclient {
//	 public static void main(String[] args) throws ClientProtocolException, IOException{
//		          //����httpclient����
//		          CloseableHttpClient httpClient = HttpClients.createDefault();
//		          //�������󷽷���ʵ���� ��ָ������url
//		          HttpGet httpget=new HttpGet("http://861067451.applinzi.com/fuwuqiver0.php?keyword=moved/1/0/07131628");
//		          //��ȡhttp��Ӧ״̬��
//		          CloseableHttpResponse response=httpClient.execute(httpget);
//		          HttpEntity entity=response.getEntity();
//		          //������Ӧͷ
//		          String content=EntityUtils.toString(entity, "utf-8");
//		         System.out.println(httpget.getURI());
//		          System.out.println(content);
//		          httpClient.close();
//		      }
//		  
//		  }

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


public class Httpclient {
	public static void main(String[] args) throws ClientProtocolException, IOException {
		// ����httpclient����
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("keyword", "moved/0/0/0717144422"));
		String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));    //ͨ�����ഴ���Ķ������ģ�⴫ͳ��HTML������POST�����еĲ���
		HttpGet httpGet = new HttpGet("http://861067451.applinzi.com/fuwuqiver0.php" + "?" + str);    //����httpget���󣬷�������
		CloseableHttpResponse response = httpClient.execute(httpGet);    //���󷵻�һ��httpresponse
		HttpEntity entity = response.getEntity();       //�����ȡ����
		// ������Ӧͷ
		String content = EntityUtils.toString(entity, "utf-8");
		System.out.println(content);
		System.out.println(str);
		httpClient.close();
		String time;
		time = GetSimpleNowTime();    //��ȡʱ���
		System.out.println(time);
	}

	public static String GetSimpleNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddhhmmss");
		return sdf.format(new Date());
	}
}

//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//
//public class Httpclient {
//	public static void main(String[] args) throws ClientProtocolException, IOException {
//
//		CloseableHttpClient httpClient = HttpClients.createDefault();
//		HttpPost httpPost = new HttpPost("http://861067451.applinzi.com/fuwuqiver0.php");
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//		nameValuePairs.add(new BasicNameValuePair("keyword", "moved/1/0/07131628"));
//		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));    //�������������������
//		CloseableHttpResponse response = httpClient.execute(httpPost);            //��������÷�������һ��HttpResponse
//		HttpEntity entity = response.getEntity();                                //��ȡHttpEntity���󣬸ö����װ�˷���������Ӧ����
//		String content = EntityUtils.toString(entity, "utf-8");
//		System.out.println(content);
//		System.out.println(httpPost.getURI());
//		System.out.println(response);
//		httpClient.close();
//
//	}
//
//}
