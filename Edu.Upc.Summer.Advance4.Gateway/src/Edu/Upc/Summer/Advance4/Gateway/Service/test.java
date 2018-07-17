package Edu.Upc.Summer.Advance4.Gateway.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class test {
	public static String data="moved/0/0,gas/123,water/123,temperature/123,gps/3213.1/32131.1,";
	public static String time;
	public static String[] sourceStrArray = null ;
	public static String getdataStrArray ;
	
	public static void main(String[] args) {
		sourceStrArray = data.split(",");
		time = GetSimpleNowTime();    //获取时间戳
		for (int i = 0; i < sourceStrArray.length; i++) {
			sourceStrArray[i]=sourceStrArray[i]+"/" + time + "/";
			   System.out.println(sourceStrArray[i]);
			   getdataStrArray=getdataStrArray+sourceStrArray[i];
			  }
		getdataStrArray=sourceStrArray[0];
		for (int i = 1; i < sourceStrArray.length; i++) {
			   getdataStrArray=getdataStrArray+sourceStrArray[i];
			  }
		System.out.println(getdataStrArray);
	}
	/**
	 * 获取时间戳函数
	 * 
	 * @param s
	 * @return
	 */
	public static String GetSimpleNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("MMddhhmmss");
		return sdf.format(new Date());
	}
}
