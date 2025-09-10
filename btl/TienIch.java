package btl;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


class TienIch {
static String sha256(String input){
try{
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(input.getBytes());
StringBuilder sb = new StringBuilder();
for(byte b: hash) sb.append(String.format("%02x", b));
return sb.toString();
}catch(NoSuchAlgorithmException e){return input;}
}
static String dinhDangVND(long v){ return new DecimalFormat("###,###,###").format(v)+" đ"; }


// Giao thức rất đơn giản: key=value;cặp khác phân tách bằng '|'
static String toKv(Map<String,String> map){
StringBuilder sb = new StringBuilder();
boolean first=true;
for (Map.Entry<String,String> e: map.entrySet()){
if(!first) sb.append("|"); first=false;
sb.append(e.getKey()).append("=").append(e.getValue()==null?"":e.getValue().replace("|"," ").replace("="," "));
}
return sb.toString();
}
static Map<String,String> parseKv(String line){
Map<String,String> m = new HashMap<>();
if(line==null) return m;
for(String part: line.split("\\|")){
int i = part.indexOf('=');
if(i>0){ m.put(part.substring(0,i).trim(), part.substring(i+1).trim()); }
}
return m;
}


static void ghiLog(String msg){
try(PrintWriter pw = new PrintWriter(new FileWriter("bank.log", true))){
pw.println(new java.util.Date()+" | "+msg);
}catch(Exception ignored){}
}
}
