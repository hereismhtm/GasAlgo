public class Gas {
    public static final int BUFFSIZE = 104857600; // 100MB
    public static final int MIN_LENSSIZE = 2;
    public static final int LENSSIZE = 4;
    public static final int SELECTED = 128;

    protected String sFile;
    protected String dFile;

    protected static double pow256(int k) {
        if (k > 15) System.exit(1);
        double result = 0;
        if (k == 0) result = 1d;
        else if (k == 1) result = 256d;
        else if (k == 2) result = 65536d;
        else if (k == 3) result = 16777216d;
        else if (k == 4) result = 4294967296d;
        else if (k == 5) result = 1099511627776d;
        else if (k == 6) result = 281474976710656d;
        else if (k == 7) result = 72057594037927936d;
        else if (k == 8) result = 18446744073709551616d;
        else if (k == 9) result = 4722366482869645213696d;
        else if (k == 10) result = 1208925819614629174706176d;
        else if (k == 11) result = 309485009821345068724781056d;
        else if (k == 12) result = 79228162514264337593543950336d;
        else if (k == 13) result = 20282409603651670423947251286016d;
        else if (k == 14) result = 5192296858534827628530496329220096d;
        else if (k == 15) result = 1329227995784915872903807060280344576d;
        return result;
    }
}
