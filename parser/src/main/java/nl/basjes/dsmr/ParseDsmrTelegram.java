package nl.basjes.dsmr;

import java.util.List;

public class ParseDsmrTelegram {

// Basic form of a telegram

//      /XXXZ Ident CR LF CR LF Data ! CRC CR LF


public static class DSMRTelegram{
    private String ident;
    private int crc;
    private List<String> values;
}



}
