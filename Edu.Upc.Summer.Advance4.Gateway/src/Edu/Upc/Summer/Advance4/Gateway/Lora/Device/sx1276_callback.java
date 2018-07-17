package Edu.Upc.Summer.Advance4.Gateway.Lora.Device;

//import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.SnifModel;

public interface sx1276_callback {
	public void MessageReceiveed(String snifmodel);
	//public void MessageReceiveed(SnifModel snifmodel);
	public void Error(int foutCode);
}