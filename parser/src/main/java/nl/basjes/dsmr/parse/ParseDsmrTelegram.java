package nl.basjes.dsmr.parse;

import nl.basjes.dsmr.parse.DsmrBaseVisitor;
import nl.basjes.dsmr.parse.DsmrLexer;
import nl.basjes.dsmr.parse.DsmrParser;
import nl.basjes.dsmr.parse.DsmrParser.EmptyContext;
import nl.basjes.dsmr.parse.DsmrParser.TelegramContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.dsmr.parse.DsmrParser.*;

public class ParseDsmrTelegram extends DsmrBaseVisitor<Void> {

// Basic form of a telegram

//      /XXXZ Ident CR LF CR LF Data ! CRC CR LF

// From the documentation about Electricity data (section 7.1)
//    Electricity –P1 transfers every second
//    OBIS reference      Value
//    0-0:96.1.1.255      Equipment identifier
//    1-0:1.8.1.255       Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
//    1-0:1.8.2.255       Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
//    1-0:2.8.1.255       Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
//    1-0:2.8.2.255       Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
//    0-0:96.14.0.255     Tariff indicator electricity. The tariff indicator can be used to switch tariff dependent loads e.g boilers. This is responsibility of the P1 user
//    1-0:1.7.0.255       Actual electricity power delivered (+P) in 1 Watt resolution
//    1-0:2.7.0.255       Actual electricity power received (-P) in 1 Watt resolution
//    0-0:96.7.21.255     Number of power failures in any phases
//    0-0:96.7.9.255      Number of long power failures in any phases
//    1-0:99:97.0.255     Power failure event log
//    1-0:32.32.0.255     Number of voltage sags in phase L1
//    1-0:52.32.0.255     Number of voltage sags in phase L2
//    1-0:72.32.0.255     Number of voltage sags in phase L3
//    1-0:32.36.0.255     Number of voltage swells in phase L1
//    1-0:52.36.0.255     Number of voltage swells in phase L2
//    1-0:72.36.0.255     Number of voltage swells in phase L3
//    1-0:32.7.0.255      Instantaneous voltage L1
//    1-0:52.7.0.255      Instantaneous voltage L2
//    1-0:72.7.0.255      Instantaneous voltage L3
//    1-0:31.7.0.255      Instantaneous current L1
//    1-0:51.7.0.255      Instantaneous current L2
//    1-0:71.7.0.255      Instantaneous current L3
//    1-0:21.7.0.255      Instantaneous active power L1 (+P)
//    1-0:41.7.0.255      Instantaneous active power L2 (+P)
//    1-0:61.7.0.255      Instantaneous active power L3 (+P)
//    1-0:22.7.0.255      Instantaneous active power L1 (-P)
//    1-0:42.7.0.255      Instantaneous active power L2 (-P)
//    1-0:62.7.0.255      Instantaneous active power L3 (-P)

    public static class DSMRTelegramValue {

        public DSMRTelegramValue(String cosemId) {
            this.cosemId = cosemId;
            switch (cosemId) {
                case "0-0:1.0.0":   description = "Timestamp"; break;
                case "0-0:96.1.1":  description = "Equipment identifier"; break;
                case "1-0:1.8.1":   description = "Meter Reading electricity delivered to client (low tariff) in 0,001 kWh"; break;
                case "1-0:1.8.2":   description = "Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh"; break;
                case "1-0:2.8.1":   description = "Meter Reading electricity delivered by client (low tariff) in 0,001 kWh"; break;
                case "1-0:2.8.2":   description = "Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh"; break;
                case "0-0:96.14.0": description = "Tariff indicator electricity"; break;
                case "1-0:1.7.0":   description = "Actual electricity power delivered (+P) in 1 Watt resolution"; break;
                case "1-0:2.7.0":   description = "Actual electricity power received (-P) in 1 Watt resolution"; break;
                case "0-0:96.7.21": description = "Number of power failures in any phases"; break;
                case "0-0:96.7.9":  description = "Number of long power failures in any phases"; break;
                case "1-0:99:97.0": description = "Power failure event log"; break;
                case "1-0:32.32.0": description = "Number of voltage sags in phase L1"; break;
                case "1-0:52.32.0": description = "Number of voltage sags in phase L2"; break;
                case "1-0:72.32.0": description = "Number of voltage sags in phase L3"; break;
                case "1-0:32.36.0": description = "Number of voltage swells in phase L1"; break;
                case "1-0:52.36.0": description = "Number of voltage swells in phase L2"; break;
                case "1-0:72.36.0": description = "Number of voltage swells in phase L3"; break;
                case "1-0:32.7.0":  description = "Instantaneous voltage L1"; break;
                case "1-0:52.7.0":  description = "Instantaneous voltage L2"; break;
                case "1-0:72.7.0":  description = "Instantaneous voltage L3"; break;
                case "1-0:31.7.0":  description = "Instantaneous current L1"; break;
                case "1-0:51.7.0":  description = "Instantaneous current L2"; break;
                case "1-0:71.7.0":  description = "Instantaneous current L3"; break;
                case "1-0:21.7.0":  description = "Instantaneous active power L1 (+P)"; break;
                case "1-0:41.7.0":  description = "Instantaneous active power L2 (+P)"; break;
                case "1-0:61.7.0":  description = "Instantaneous active power L3 (+P)"; break;
                case "1-0:22.7.0":  description = "Instantaneous active power L1 (-P)"; break;
                case "1-0:42.7.0":  description = "Instantaneous active power L2 (-P)"; break;
                case "1-0:62.7.0":  description = "Instantaneous active power L3 (-P)"; break;
                case "0-0:96.13.0": description = "Text message max 1024 characters."; break;
                default:            description = "?";
            }
        }
        String cosemId;
        BigDecimal numericValue;
        String numericUnit;
        String textValue;
        String description; // FIXME: Should make this optional

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Value{  Id='").append(cosemId).append('\'');

            if (numericValue != null) {
                sb.append("  value = ").append(numericValue).append(" ").append(numericUnit);
            }
            if (textValue != null) {
                sb.append("  text = ").append(textValue);
            }
                sb.append("  description = ").append(description);
            sb.append(" }\n");
            return sb.toString();
        }

    }

    public static class DSMRTelegram {
        private String telegram;
        private String ident;

        public String getTelegram() {
            return telegram;
        }

        public String getIdent() {
            return ident;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public String getCrc() {
            return crc;
        }

        public List<DSMRTelegramValue> getValues() {
            return values;
        }

        private ZonedDateTime timestamp;

        private String crc;
        private List<DSMRTelegramValue> values = new ArrayList<>();

        public boolean isValid() {
            return CheckCRC.crcIsValid(telegram);
        }

        @Override
        public String toString() {
            return "DSMRTelegram{" +
                "telegram='" + telegram + '\'' +
                ", ident='" + ident + '\'' +
                ", timestamp=" + timestamp +
                ", " + (isValid() ? "Valid " : "INVALID ") + "crc=" + crc +
                ", values=" + values +
                '}';
        }
    }

    public static synchronized DSMRTelegram parse(String telegram) {
        return new ParseDsmrTelegram(telegram).parse();
    }

    private DSMRTelegram dsmrTelegram;


    private ParseDsmrTelegram(String telegram) {
        dsmrTelegram = new DSMRTelegram();
        dsmrTelegram.telegram = telegram;
    }

    private DSMRTelegram parse() {
        CodePointCharStream input = CharStreams.fromString(dsmrTelegram.telegram);
        DsmrLexer lexer = new DsmrLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        DsmrParser parser = new DsmrParser(tokens);

//        if (!verbose) {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
//        }
//        lexer.addErrorListener(PARSER);
//        parser.addErrorListener(PARSER);

        TelegramContext telegramContext = parser.telegram();

        this.visitTelegram(telegramContext);

        return dsmrTelegram;
    }

    @Override
    public Void visitTelegram(TelegramContext ctx) {
        dsmrTelegram.ident = ctx.ident.getText();
        dsmrTelegram.crc = ctx.crc.getText();
        return visitChildren(ctx);
    }

    @Override
    public Void visitEmpty(EmptyContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.textValue = "";
        dsmrTelegram.values.add(value);
        return null;
    }

    TimestampParser timestampParser = new TimestampParser();
    @Override
    public Void visitTimestamp(TimestampContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.textValue = ctx.timestamp.getText();
        if ("0-0:1.0.0".equals(value.cosemId)) {
            dsmrTelegram.timestamp = timestampParser.parse(value.textValue);
        }

        dsmrTelegram.values.add(value);
        return null;
    }

    // https://stackoverflow.com/questions/50712987/hex-string-to-byte-array-conversion-java
    public byte[] hexStringToByteArray(String s) {
        byte[] data = new byte[s.length()/2];
        for (int i = 0; i < data.length; i ++) {
            data[i] = (byte) ((Character.digit(s.charAt(i*2), 16) << 4)
                + Character.digit(s.charAt(i*2 + 1), 16));
        }
        return data;
    }

    @Override
    public Void visitHexstring(HexstringContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.textValue = new String(hexStringToByteArray(ctx.value.getText()), UTF_8);
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitNumber(NumberContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitElectricityKiloWattHour(ElectricityKiloWattHourContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        value.numericUnit= ctx.unit.getText();
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitElectricityKiloWatt(ElectricityKiloWattContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        value.numericUnit= ctx.unit.getText();
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitElectricityVolt(ElectricityVoltContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        value.numericUnit= ctx.unit.getText();
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitElectricityAmpere(ElectricityAmpereContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        value.numericUnit= ctx.unit.getText();
        dsmrTelegram.values.add(value);
        return null;
    }

    @Override
    public Void visitGasCubicMeter(GasCubicMeterContext ctx) {
        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
        value.numericValue = new BigDecimal(ctx.value.getText());
        value.numericUnit= ctx.unit.getText();
        dsmrTelegram.values.add(value);
        return null;
    }

// TODO: Implement the nested list
//    @Override
//    public Void visitEventList(EventListContext ctx) {
//        DSMRTelegramValue value = new DSMRTelegramValue(ctx.id.getText());
//        value.numericValue = new BigDecimal(ctx.value.getText());
//        value.numericUnit= ctx.unit.getText();
//        dsmrTelegram.values.add(value);
//        return null;
//    }
}
