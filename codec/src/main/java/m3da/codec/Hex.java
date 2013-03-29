package m3da.codec;

public class Hex {

    private static final byte[] HEX_CHAR = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
                            'c', 'd', 'e', 'f' };

    public static String encodeHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            byte byteValue = data[i];
            sb.append(new String(new byte[] { HEX_CHAR[(byteValue & 0x00F0) >> 4], HEX_CHAR[byteValue & 0x000F] }));
        }
        return sb.toString();
    }

    public static byte[] decodeHex(String hexStr) {
        int len = hexStr.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4) + Character.digit(
                    hexStr.charAt(i + 1), 16));
        }
        return result;
    }
}
