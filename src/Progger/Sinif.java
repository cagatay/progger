/*
 *This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package Progger;

/**
 *
 * @author cagatay
 */
public class Sinif {
    private String CRN;
    private String ogr_gor;
    private String bina;
    private String derslik;
    private String kod;
    private String gun;
    private String saat;
    
    public Sinif(String c, String k,String o, String b, String g, String s, String d, String bl) {
        CRN = c;
        kod = k;
        ogr_gor = o;
        bina = b;
        gun = g;
        saat = s;
        derslik = d;
    }
    
    public String getOgr() {
        return ogr_gor;
    }
    
    public String getBina() {
        return bina;
    }
    
    public String getKod() {
        return kod;
    }

}
