/* 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details. 
 */

package Progger;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.Array;
import java.net.*;
/**
 *
 * @author cagatay
 */
public class Ogrenci {
    private String id;  // ogrenci no
    private String bolum;
    private int yil; // bolume baslama yili
    public LinkedList<Ders> dersler; // almasi gereken dersler
    private float ortalama;
    private float ing_puan; // yeterlilik sinavi puani
    private float kredi; // kac kredi vermis
    public int sinif;
    private boolean gozetim = false;    // gozetim durumu var mi yok mu
    private float alabilecegi_kredi; // kac kredi alabilir
    public static String verdigi_dersler = "";

    
    public Ogrenci(String id, String bolum, String yil) throws MalformedURLException, IOException {
        
        this.id = id;
        this.bolum = bolum;
        this.yil = Integer.parseInt(yil);

        dersler = new LinkedList();
    }

    public float getKredi() {
        // Alabilecegi krediyi dondurur.
        return alabilecegi_kredi;
    }
    
    public void notlariAl() throws MalformedURLException, IOException {
        /* 'Mezuniyetime ne kaldi?' sayfasini okuyup ogrencinin ne durumda olduguna bakar.
         * Bu sayfa biraz duzensiz oldugu icin once okunabilecek bir hale getirmek gerekiyor.
         */

        URL url = new URL("http://earth.sis.itu.edu.tr/cgi-bin/nekal?" + id);
        URLConnection con = url.openConnection();
        con.setRequestProperty("Referer", "http://node1.sis.itu.edu.tr:8092/pls/pprd/nekaldi.P_Mezun");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));
        
        String line = "";
        String result = "";
        String result2 = "";
        while ((line = br.readLine()) != null) 
            result +=line;
        br.close();
        
        Pattern pat = Pattern.compile("\\Q<tr bgcolor=#00AAFF>\\E.*?\\Q</tr>\\E");
        //Pattern pat = Pattern.compile("\\Q<td>\\E.*?\\Q </td>\\E");
        Matcher mat = pat.matcher(result);
        
        Pattern pat2 = Pattern.compile("\\Q<td>\\E.*?\\Q</td>\\E");

        while (mat.find()) {
            Matcher mat2 = pat2.matcher(mat.group());
            while(mat2.find())
            result2 += mat2.group() + "\n";
        }
        String result3 = result2.replaceAll("\\<.*?\\>", "").replaceAll("\\s\\n", "\n");
        
        pat = Pattern.compile("^.*?\\n");
        mat = pat.matcher(result3);
        
        String dizi[] = result3.split("\\n");
        String dizi2[] = new String[2];
        
        for(int i = 0; i< (Array.getLength(dizi)-6); i++) {
            dizi2 = dizi[++i].split("\\s\\(");
            for(int f = 0; f< dersler.size(); f++) {
                if(dersler.get(f).getIsim().equals(dizi[i-1])) {
                    if (!(dizi2[1].contains("AA") || dizi2[1].contains("BL"))) {
                        if (dersler.get(f).getKod().equals("")) {
                            for (int g = 0; g < dersler.get(f).options.size(); g++) {
                                if (dersler.get(f).options.get(g).getKod().equals(dizi2[0])) {
                                    dersler.get(f).setKredi(dersler.get(f).options.get(g).getKredi());
                                    dersler.get(f).isim = dersler.get(f).options.get(g).getIsim();
                                    break;
                                }

                            }
                            dersler.get(f).options.clear();
                        }
                        dersler.get(f).setKod(dizi2[0]);
                        dersler.get(f).setNot(dizi2[1].replaceAll("\\)", ""));
                        dersler.get(f).setVerildi();
                        verdigi_dersler += dizi2[0].replace(" ", "") + "|";
                        break;
                    } else {
                        dersler.remove(f);
                        verdigi_dersler += dizi2[0].replace(" ", "") + "|";
                        break;
                    }
                }
            }
        }
        ortalama = Float.parseFloat(dizi[Array.getLength(dizi) - 5]);
        kredi = Float.parseFloat(dizi[Array.getLength(dizi) - 3]);
        
        if(kredi < 35) sinif = 1;
        else if(kredi < 75) sinif = 2;
        else if(kredi < 110) sinif = 3;
        else sinif = 4;
        
        if((sinif == 1 || sinif == 2) && ortalama < 1.8 && kredi != 0) gozetim = true;
        else if((sinif == 3 || sinif == 4) && ortalama < 1.9) gozetim = true;
        
        if(gozetim) alabilecegi_kredi = 15;
        else if(ortalama < 2.25) alabilecegi_kredi = 22;
        else if(ortalama < 3) alabilecegi_kredi = 25;
        else alabilecegi_kredi = 0;
    }
    
    private String dpBul() throws MalformedURLException, IOException {
        /* Ogrenciye uygun ders planini URL'sini bulma fonksiyonu.
         * Cogu bolumun ders plani bolume baslama yilina gore farklılasiyor.
         * Sansliyiz ki linkler bize ipucu veriyor yoksa isimiz zordu. */

        URL dp = new URL("http://earth.sis.itu.edu.tr/plan/" + bolum + "/");
        URLConnection con = dp.openConnection();
        
        String temp = "";
        String temp1 = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));
        br.readLine(); // <html> satirini atlayalim.
        
        Pattern pat = Pattern.compile("\\d{6}"); // aradigimiz sey a href = "yyyy10" seklinde
        Matcher mat;
        
        while(true) {
            temp = br.readLine();
            mat = pat.matcher(temp);
            
            if(!mat.find()) break; // sayfanin sonuna geldiysek dur.
            
            if(temp1.equals("")) { // daha ilk satirdaysak
                temp1 = temp;
                continue;
            }

            if(yil < (Integer.parseInt(mat.group().substring(0, 4)) - 1))
                return "http://earth.sis.itu.edu.tr/plan/" + bolum + "/" + temp1 + ".html";
            else temp1 = mat.group();
        }

        br.close();

        // sadece tek satir varsa
        if(temp1.equals("")) return "http://earth.sis.itu.edu.tr/plan/" + bolum + "/" + "000000.html";
        // degilse temp1 de kalan deger adresi verir.
        else return "http://earth.sis.itu.edu.tr/plan/"+bolum+"/" + temp1 + ".html";
    }
    
    public void planAl() throws IOException {
        /* Ders plani sayfasindan ogrencinin almasi gereken dersleri bulup
         * bir linked list'e atar.
         * Bazi derslerin (secmeli dersler ve ingilizce) farklı opsiyonları oldugunu da
         * goz onunde bulundurmak lazim. */

        URL dp = new URL(dpBul());
        URLConnection con = dp.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));
        
        String line;
        String temp;
        
        Pattern pat = Pattern.compile("\\\".*?\\\"");
        Pattern pat2 = Pattern.compile("\\Q\">\\E.*?\\Q<\\E");
        Pattern pat3 = Pattern.compile("d\\>.+?\\<");
        Matcher mat;
        
        
        while((line = br.readLine()) != null) {
            if(line.matches("\\Q<td>\\E.*?\\d\\Q</td></tr>\\E")) {
                if(line.matches("\\Q<td>&\\E.*?")) {
                    mat = pat.matcher(line);
                    mat.find();
                    temp = mat.group().replaceAll("\\\"", "");
                    
                    mat = pat2.matcher(line);
                    mat.find();
                    dersler.add(new Ders(mat.group().replaceAll("\\\"\\>", "").replaceAll("\\<", "")));
                    
                    URL url = new URL("http://earth.sis.itu.edu.tr/plan/" + bolum + "/" + temp);
                    URLConnection con2 = url.openConnection();
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(con2.getInputStream(), "ISO8859-9"));
                    
                    br2.readLine(); br2.readLine(); br2.readLine();
                    
                    String temp_str;
                    String kod_temp, isim_temp, kredi_temp;
                    
                    while(true) {
                        temp_str = br2.readLine();
                        if(temp_str.equals("</table></html>")) break;
                        else {
                            mat = pat3.matcher(temp_str);
                            mat.find(); kod_temp = mat.group().replaceAll("\\Qd>\\E", "").replaceAll("\\<", "");
                            mat.find(); isim_temp = mat.group().replaceAll("\\Qd><td>", "").replaceAll("\\<", "");
                            mat.find(); kredi_temp = mat.group().replaceAll("\\Qd><td>", "").replaceAll("\\<", "");
                            
                            dersler.get(dersler.size()-1).options.add(new Ders(isim_temp, kod_temp, Float.parseFloat(kredi_temp)));
                        }
                    }
                    br2.close();
                }
                else {
                    dersler.add( new Ders(line.split("\\Q</td><td>\\E")[1], 
                                          line.split("\\Q</td><td>\\E")[0].replaceAll("\\Q<td>\\E", ""),
                                          Float.parseFloat(line.split("\\Q</td><td>\\E")[2]))
                               );
                }
            }
        }
        br.close();
    }
    
    private float puanAl() throws MalformedURLException, IOException {
        /* Ogrencinin yeterlilik sinavindan aldigi puani bulur.
         * Ozel bir durum varsa (TOEFL gibi) calismayacaktir dogal olarak. */

        URL post = new URL("http://www.ydy2.itu.edu.tr/yeterlilik/sonuc4.php");
        URLConnection con = post.openConnection();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));
        for(int i=0; i<17; i++) br.readLine(); // ilk satirlar tiri viri

        // post edilcek parametrenin ismi Op_Cevap[0, 1, 2 ...] seklinde gidiyor.
        // ilk siradakinin kac oldugunu bulup ondan geriye saymamız lazim.
        int temp = Integer.parseInt(br.readLine().replaceAll("^.*?\\\"Op_Cevap", "").replaceAll("\\\".*?$", ""));
        br.close();
        
        for(int i=temp; i>=0; i--) {
            con = post.openConnection();
            con.setDoOutput(true);
        
            OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
            
            if(i != 0) os.write("no=" + id + "&tarih=Op_Cevap" + String.valueOf(i));
            else os.write("no=" + id + "&tarih=Op_Cevap");

            os.close();

            br = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO8859-9"));

            for(int f=0; f<30; f++) br.readLine();
            String puan_temp = br.readLine();
            
            if(puan_temp.matches("\\Q<TD>\\E.+?\\Q</TD>\\E")) {
                br.close();
                return Float.parseFloat(puan_temp.replaceAll("\\<.*?\\>", "").replaceAll(",", "\\."));
            }
        }
        br.close();
        return 0; // ogrenci bulunamadiysa
    }
    
    public void dersPrograminiAl() throws MalformedURLException, IOException {
        for(int i=0; i<dersler.size(); i++) {
            if(dersler.get(i).getKod().equals("")) {
                for(int f=0; f<dersler.get(i).options.size(); f++) {
                    if(!dersler.get(i).options.get(f).acildimi(bolum))
                        dersler.get(i).options.remove(f--);
                    else {
                        dersler.get(i).options.get(f).setAcildi();
                        //dersler.get(i).panel.add(dersler.get(i).options.get(f).checkbox);
                    }
                }
                if(dersler.get(i).options.size() != 0)
                    dersler.get(i).setAcildi();
                else dersler.remove(i--);
            }
            else {
                if(!dersler.get(i).acildimi(bolum) || (dersler.get(i).getKod().contains("492") && sinif != 4))
                        dersler.remove(i--);
                    else dersler.get(i).setAcildi();
            }
        }
    }
    
    public static String[] bolumBul(String no) {
        /* Bolum - fakulte bulmaca. Ilk iki hane fakulteyi, sonraki uc hane yili
         * ondan sonraki iki hanede bolumu veriyor.
         * Ogrenci'in constructer'inda degil de main class'da kullanilacak.
         * Donus degeri sirayla fakulteyi, bolumu, yili ve
         * fakultenin bolumlerini icerecek bir string dizisi.
         * Her zaman dogru bolumu bulamayabilir.
         * 1900 yilindan once girmemis olmak kaydiyla okula giris yilini da bulur diyorlar.
         */

        int fakulte_temp = Integer.parseInt(no.substring(0, 2));
        int yil_temp = Integer.parseInt(no.substring(2, 5));
        int bolum_temp = Integer.parseInt(no.substring(5,7));

        String[] liste;

        switch(fakulte_temp) {
            case 01:
                liste = new String[6];
                liste[0] = "Insaat Fakultesi";

                switch(bolum_temp) {
                    case 1:
                    case 2:
                    case 8:
                    case 9:
                        liste[1] = "INS";
                        break;
                    case 3:
                    case 7:
                        liste[1] = "JDF";
                        break;
                    case 4:
                    case 5:
                        liste[1] = "CEV";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }
                liste[3] = "CEV";
                liste[4] = "INS";
                liste[5] = "JDF";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 02:
                liste = new String[8];
                liste[0] = "Mimarlik Fakultesi";

                switch(bolum_temp) {
                    case 1:
                    case 7:
                        liste[1] = "MIM";
                        break;
                    case 2:
                    case 9:
                        liste[1] = "SBP";
                        break;
                    case 3:
                        liste[1] = "EUT";
                        break;
                    case 4:
                        liste[1] = "ICM";
                        break;
                    case 5:
                        liste[1] = "PEM";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[3] = "EUT";
                liste[4] = "ICM";
                liste[5] = "MIM";
                liste[6] = "PEM";
                liste[7] = "SBP";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 03:
                liste = new String[5];
                liste[0] = "Makina Fakultesi";

                switch(bolum_temp) {
                    case 1:
                    case 2:
                    case 7:
                        liste[1] = "MAK";
                        break;
                    case 9:
                        liste[1] = "IML";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[3] = "IML";
                liste[4] = "MAK";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 04:
                liste = new String[8];
                liste[0] = "Elektrik - Elektronik Fakultesi";

                switch(bolum_temp) {
                    case 1:
                        liste[1] = "ELK";
                        break;
                    case 2:
                    case 7:
                    case 8:
                        liste[1] = "BLG";
                        break;
                    case 3:
                        liste[1] = "ELE";
                        break;
                    case 4:
                        liste[1] = "KON";
                        break;
                    case 5:
                        liste[1] = "TEL";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }
                liste[3] = "BLG";
                liste[4] = "ELE";
                liste[5] = "ELK";
                liste[6] = "KON";
                liste[7] = "TEL";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 05:
                liste = new String[8];
                liste[0] = "Maden Fakultesi";

                switch(bolum_temp) {
                    case 1:
                        liste[1] = "JEO";
                        break;
                    case 2:
                    case 8:
                    case 9:
                        liste[1] = "JEF";
                        break;
                    case 3:
                        liste[1] = "PET";
                        break;
                    case 4:
                        liste[1] = "CHZ";
                        break;
                    case 7:
                        liste[1] = "MAD";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[3] = "CHZ";
                liste[4] = "JEF";
                liste[5] = "JEO";
                liste[6] = "MAD";
                liste[7] = "PET";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 06:
                liste = new String[6];
                liste[0] = "Kimya - Metalurji Fakultesi";

                switch(bolum_temp) {
                    case 1:
                        liste[1] = "MET";
                        break;
                    case 2:
                    case 9:
                        liste[1] = "GID";
                        break;
                    case 7:
                        liste[1] = "KMM";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[0] = "GID";
                liste[1] = "KMM";
                liste[2] = "MET";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 07:
                liste = new String[5];
                liste[0] = "Isletme Fakultesi";

                switch(bolum_temp) {
                    case 1:
                        liste[1] = "ISL";
                        break;
                    case 2:
                    case 3:
                        liste[1] = "END";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[3] = "END";
                liste[4] = "ISL";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 8:
                liste = new String[5];
                liste[0] = "Gemi Insaat Fakultesi";

                switch(bolum_temp) {
                    case 1:
                    case 8:
                        liste[1] = "DEN";
                        break;
                    case 7:
                        liste[1] = "GEM";
                        break;
                    default:
                        liste[1] = "";
                        break;
                }

                liste[3] = "DEN";
                liste[4] = "GEM";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 9:
                liste = new String[7];
                liste[0] = "Fen - Edebiyat Fakultesi";

                switch(bolum_temp) {
                    case 1:
                        liste[1] = "FIZ";
                        break;
                    case 2:
                    case 3:
                    case 8:
                        liste[1] = "KIM";
                        break;
                    case 4:
                    case 7:
                    case 9:
                        liste[1] = "BIO";
                        break;
                    default:
                        liste[1] = "MAT";
                        break;
                }

                liste[3] = "BIO";
                liste[4] = "FIZ";
                liste[5] = "KIM";
                liste[6] = "MAT";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 11:
                liste = new String[6];
                liste[0] = "Ucak - Uzay Fakultesi";
                switch(bolum_temp) {
                    case 1:
                        liste[1] = "UZB";
                        break;
                    case 7:
                        liste[1] = "UCB";
                        break;
                    case 8:
                        liste[1] = "MET";
                        break;
                    default:
                        liste[1] = "";
                }
                
                liste[3] = "MET";
                liste[4] = "UCB";
                liste[5] = "UZB";
                
                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 13:
                liste = new String[5];
                liste[0] = "Denizcilik Fakultesi";
                switch(bolum_temp) {
                    case 1:
                        liste[1] = "DUI";
                        break;
                    case 2:
                    case 8:
                        liste[1] = "GMI";
                        break;
                    default:
                        liste[1] = "";
                }
                
                liste[3] = "DUI";
                liste[4] = "GMI";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;

            case 14:
                liste = new String[3];
                liste[0] = "Tekstil Fakultesi";
                liste[1] = "TEK";

                if(yil_temp>900) liste[2] = String.valueOf(1000 + yil_temp);
                else liste[2] = String.valueOf(2000 + yil_temp);
                return liste;
         }
        // Sadece hata vermesin diye koydum.
        return null;
    }
    
}

