/*
 *This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */
package Progger;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author cagatay
 */
public class Ders {
    public String isim;
    private String kod;
    private boolean verildi = false;
    private boolean acildi = false;
    private float kredi;
    private String not;
    public List<Ders> options;
    public List<Sinif> sinif;
    public static LinkedList<Sinif> cache = new LinkedList();
    public static String cachelist = "";

    
    public Ders(String s, String k, Float f) {
        isim = s;
        kod = k;
        kredi = f;
        options = new LinkedList();
        sinif = new LinkedList();
     }
    
    public Ders(String s) {
        isim = s;
        kod = "";
        kredi = 0;
        options = new LinkedList();
        sinif = new LinkedList();
    }
    
    public String getIsim() {
        return isim;
    }
    
    public float getKredi() {
        return kredi;
    }
    
    public void setKredi(float k) {
        kredi = k;
    }
    
    public String getKod() {
        return kod;
    }
    
    public String getNot() {
        return not;
    }
    
    public void setKod(String s) {
        kod = s;
    }
    
    public void setNot(String s) {
        not = s;
    }
    
    public void setVerildi() {
        verildi = true;
    }
    
    public boolean getVerildi() {
        return verildi;
    }
    
    
    public boolean getAcildi() {
        return acildi;
    }
    
    public void setAcildi() {
        acildi = true;
    }
    
    public boolean acildimi(String ogrenci_bolum) throws MalformedURLException, IOException {
        String bolum = kod.split("\\s")[0];
        if(cachelist.contains(bolum)) {
            for(int i= 0; i<cache.size(); i++) {
                if(cache.get(i).getKod().split("\\s")[0].equals(bolum)) {
                    if(kod.compareTo(cache.get(i).getKod()) == -1) return false;
                    while(cache.get(i).getKod().split("\\s")[0].equals(bolum)) {
                        if(cache.get(i).getKod().equals(kod)) {
                            sinif.add(cache.get(i));
                            return true;
                        }
                        if(i<cache.size() -1) i++;
                        else break;
                    }
                    return false;
                }
            }
        }
        
        URL url = new URL("http://earth.sis.itu.edu.tr/program/" + bolum.toLowerCase() + ".html");
        URLConnection con = url.openConnection();
        
        String crn_temp, kod_temp, ogr_temp, bina_temp, 
                gun_temp, saat_temp, derslik_temp, onsart_temp, bolumler_temp;
        
        String pattern = "\\<.*?\\>";
        
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));
        
        for(int i=0; i<43; i++) br.readLine();
        
        while(true) {
            br.readLine(); br.readLine();

            crn_temp = br.readLine().replaceAll(pattern, "");
            
            if(!crn_temp.matches("\\d{5}")) break;
            
            br.readLine();
            kod_temp = br.readLine().replaceAll(pattern, "");
            if(!kod_temp.equals(kod)) {
                br.readLine(); br.readLine(); br.readLine();
                ogr_temp = br.readLine().replaceAll(pattern, "");
                 br.readLine();
                bina_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, "");
                br.readLine();
                gun_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, "");
                br.readLine(); 
                saat_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, ""); 
                br.readLine();
                derslik_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, ""); 
                br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine();
                bolumler_temp = br.readLine().replaceAll(pattern, "");
                br.readLine();
                onsart_temp = br.readLine().replaceAll(pattern, "").trim();
                br.readLine();
                if(bolumler_temp.contains(ogrenci_bolum) && onSartKontrol(Ogrenci.verdigi_dersler, onsart_temp))
                    cache.add(new Sinif(crn_temp, kod_temp, ogr_temp, bina_temp, gun_temp, saat_temp, derslik_temp, bolumler_temp));   
            }
            else {
                br.readLine(); br.readLine(); br.readLine();
                ogr_temp = br.readLine().replaceAll(pattern, "");
                br.readLine();
                bina_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, "");
                br.readLine();
                gun_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, "");
                br.readLine(); 
                saat_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, ""); 
                br.readLine();
                derslik_temp = br.readLine().replaceAll("\\Q<br>\\E", "&").replaceAll(pattern, "");
                br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine(); br.readLine();
                bolumler_temp = br.readLine().replaceAll(pattern, "");
                br.readLine();
                onsart_temp = br.readLine().replaceAll(pattern, "").trim();
                br.readLine();
                if(bolumler_temp.contains(ogrenci_bolum) && onSartKontrol(Ogrenci.verdigi_dersler, onsart_temp)) {
                    sinif.add(new Sinif(crn_temp, kod_temp, ogr_temp, bina_temp, gun_temp, saat_temp, derslik_temp, bolumler_temp));
                }
            }
        }
        if(!cachelist.contains(bolum))
            cachelist += bolum +"|";
        if (sinif.size() != 0) return true;
        else return false;
    }
    
    public boolean onSartKontrol(String dersler, String os) {
        String pattern1 = "\\(.*?\\)";
        String pattern2 = ".*?\\Qveya\\E.*?";
        String pattern3 = ".*?\\Qve\\E.*?";
        //String pattern4 = "\\s\\QMIN\\E.*?";

        if(os.equals("Özel") || os.equals("Yok")) return true;
        else {
            if(os.matches(pattern1)) {
                return onSartKontrol(dersler, os.substring(1, os.length()-1));
            }
            else if(os.matches(pattern2)) {
                boolean temp = false;
                for(int i=0; i<os.split("\\Qveya\\E").length; i++) {
                    temp = temp || onSartKontrol(dersler, os.split("\\Qveya\\E")[i]);
                    if(temp) break;
                }
                return temp;
            }
            else if(os.matches(pattern3)) {
                boolean temp = true;
                for(int i=0; i<os.split("\\Qve\\E").length; i++) {
                    temp = temp && onSartKontrol(dersler, os.split("\\Qve\\E")[i]);
                    if(!temp) break;
                }
                return temp;
            }
            else return dersler.contains(os.split("\\s")[0]);
        }
    }
}
