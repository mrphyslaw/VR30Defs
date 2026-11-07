package com.romraider.maps.checksum;

import java.util.Map;

/**
 * Nissan SH72533 checksum layout with calibration/program ADD16 fields and
 * full-image XOR32/SUM32 fields.
 *
 * Full dumps validate four fields. Calibration-only files validate three:
 * calibration ADD16 plus XOR32/SUM32. Partial mode requires the aggregate
 * SUM32/XOR32 contribution of the unchanged program and fails closed when
 * either seed is absent.
 */
public class ChecksumNissanSH72533Add16Xor32Sum32 implements ChecksumManager {
    private static final String START = "start";
    private static final String END = "end";
    private static final String SUMLOC = "sumloc";
    private static final String PROGRAMSTART = "programstart";
    private static final String SECONDARYSUMLOC = "secondarysumloc";
    private static final String STDSTART = "stdstart";
    private static final String STDSUMLOC = "stdsumloc";
    private static final String XORLOC = "xorloc";
    private static final String CALIBRATION_ONLY = "calibrationonly";
    private static final String SUM32_SEED = "sum32seed";
    private static final String XOR32_SEED = "xor32seed";

    private int start = 0x010400;
    private int end = 0x200000;
    private int sumloc = 0x010400;
    private int programstart = 0x080000;
    private int secondarysumloc = 0x080000;
    private int stdstart = 0x010404;
    private int stdsumloc = 0x010898;
    private int xorloc = 0x01088C;
    private boolean calibrationOnly;
    private int sum32Seed;
    private int xor32Seed;
    private boolean sum32SeedConfigured;
    private boolean xor32SeedConfigured;

    @Override
    public void configure(Map<String, String> vars) {
        if (vars.get(START) != null) start = parseHex(vars.get(START));
        if (vars.get(END) != null) end = parseHex(vars.get(END));
        if (vars.get(SUMLOC) != null) sumloc = parseHex(vars.get(SUMLOC));
        if (vars.get(PROGRAMSTART) != null) programstart = parseHex(vars.get(PROGRAMSTART));
        if (vars.get(SECONDARYSUMLOC) != null) secondarysumloc = parseHex(vars.get(SECONDARYSUMLOC));
        if (vars.get(STDSTART) != null) stdstart = parseHex(vars.get(STDSTART));
        if (vars.get(STDSUMLOC) != null) stdsumloc = parseHex(vars.get(STDSUMLOC));
        if (vars.get(XORLOC) != null) xorloc = parseHex(vars.get(XORLOC));
        if (vars.get(CALIBRATION_ONLY) != null) {
            calibrationOnly = Boolean.parseBoolean(vars.get(CALIBRATION_ONLY).trim());
        }
        if (vars.get(SUM32_SEED) != null) {
            sum32Seed = parseHex(vars.get(SUM32_SEED));
            sum32SeedConfigured = true;
        }
        if (vars.get(XOR32_SEED) != null) {
            xor32Seed = parseHex(vars.get(XOR32_SEED));
            xor32SeedConfigured = true;
        }
    }

    @Override
    public int getNumberOfChecksums() {
        return calibrationOnly ? 3 : 4;
    }

    @Override
    public int validate(byte[] data) {
        if (!validLayout(data)) return 0;
        int valid = 0;
        if (readUInt16BE(data, sumloc) == calculateAdd16(data, start, calibrationEnd(), sumloc)) valid++;
        if (!calibrationOnly
                && readUInt16BE(data, secondarysumloc)
                == calculateAdd16(data, programstart, end, secondarysumloc)) valid++;
        int[] std = calculateStd32(data);
        if (readUInt32BE(data, stdsumloc) == std[0]) valid++;
        if (readUInt32BE(data, xorloc) == std[1]) valid++;
        return valid;
    }

    @Override
    public int update(byte[] data) {
        if (!validLayout(data)) return 0;
        int changed = 0;

        // Calibration ADD16 covers the XOR32/SUM32 fields, so update the
        // 32-bit pair before either ADD16 field.
        int[] std = calculateStd32(data);
        changed += writeUInt32BEIfChanged(data, stdsumloc, std[0]);
        changed += writeUInt32BEIfChanged(data, xorloc, std[1]);
        if (!calibrationOnly) {
            changed += writeUInt16BEIfChanged(
                    data, secondarysumloc,
                    calculateAdd16(data, programstart, end, secondarysumloc));
        }
        changed += writeUInt16BEIfChanged(
                data, sumloc,
                calculateAdd16(data, start, calibrationEnd(), sumloc));
        return changed;
    }

    private int calibrationEnd() {
        return calibrationOnly ? end : programstart;
    }

    private boolean validLayout(byte[] data) {
        if (data == null
                || start < 0
                || stdstart < start
                || end > data.length
                || end <= stdstart
                || !inRange(sumloc, 2, data.length)
                || !inRange(stdsumloc, 4, data.length)
                || !inRange(xorloc, 4, data.length)) {
            return false;
        }
        if (calibrationOnly) {
            return sum32SeedConfigured && xor32SeedConfigured;
        }
        return programstart > stdstart
                && end > programstart
                && inRange(secondarysumloc, 2, data.length);
    }

    private boolean inRange(int offset, int size, int length) {
        return offset >= 0 && offset <= length - size;
    }

    private int calculateAdd16(byte[] data, int rangeStart, int rangeEnd, int field) {
        int sum = 0;
        for (int offset = rangeStart; offset + 1 < rangeEnd; offset += 2) {
            if (offset == field) continue;
            sum = (sum + readUInt16BE(data, offset)) & 0xFFFF;
        }
        return sum;
    }

    private int[] calculateStd32(byte[] data) {
        int sum = calibrationOnly ? sum32Seed : 0;
        int xor = calibrationOnly ? xor32Seed : 0;
        for (int offset = stdstart; offset + 3 < end; offset += 4) {
            if (offset == stdsumloc || offset == xorloc || offset == programstart) continue;
            int word = readUInt32BE(data, offset);
            sum += word;
            xor ^= word;
        }
        return new int[] { sum, xor };
    }

    private int readUInt16BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private int readUInt32BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    private int writeUInt16BEIfChanged(byte[] data, int offset, int value) {
        if (readUInt16BE(data, offset) == value) return 0;
        data[offset] = (byte) ((value >>> 8) & 0xFF);
        data[offset + 1] = (byte) (value & 0xFF);
        return 1;
    }

    private int writeUInt32BEIfChanged(byte[] data, int offset, int value) {
        if (readUInt32BE(data, offset) == value) return 0;
        data[offset] = (byte) ((value >>> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >>> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >>> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
        return 1;
    }

    private int parseHex(String value) {
        String text = value.trim();
        if (text.startsWith("0x") || text.startsWith("0X")) {
            return (int) Long.parseLong(text.substring(2), 16);
        }
        return Integer.parseInt(text);
    }
}
