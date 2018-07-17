package Edu.Upc.Summer.Advance4.Gateway.Lora.Model;

import java.io.Serializable;

import Edu.Upc.Summer.Advance4.Gateway.Lora.Model.JsonUpRxpk.rxpk;

public class SnifModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public int ID = 0;
	public int address;
	public int sequence;
	public int rssi;
	public String header;
	public String bdata;
	public rxpk rXpk;
	
}
