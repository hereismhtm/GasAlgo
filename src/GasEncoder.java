import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GasEncoder extends Gas {
    private final byte[] buffer = new byte[Gas.BUFFSIZE];
    private final byte[] outbuff = new byte[Gas.BUFFSIZE];
    private final byte[] rawbuff = new byte[Gas.SELECTED];
    private final ApproximatePriority apr = new ApproximatePriority(Gas.SELECTED);
    private int bytes, lens, out, raw;

    private long filesize;
    private long header;
    private long guider;
    private long rawdata;
    private long outsize;

    public GasEncoder data(String sFile) {
        this.sFile = sFile;
        this.dFile = sFile.concat(".gas");
        return this;
    } // end of data(String)

    public GasEncoder data(String sFile, String dFile) {
        this.sFile = sFile;
        this.dFile = dFile;
        return this;
    } // end of data(String, String)

    public void build() throws IOException {
        int i, j;

        System.out.println("scanning...");
        InputStream fIn = new FileInputStream(new File(sFile));
        bytes = fIn.read(buffer);
        lens = 0;
        while (true) {
            if (lens == bytes - (Gas.LENSSIZE - 1)) {
                for (i = 0; lens < bytes; i++, lens++)
                    buffer[i] = buffer[lens];
                bytes = fIn.read(buffer, i, Gas.BUFFSIZE - i);
                if (bytes != -1) {
                    bytes += i;
                    lens = 0;
                } else
                    break;
            }
            scan();
        }
        for (j = 0; j < i; j++)
            filesize++;
        fIn.close();

        // --------

        apr.setup1(false);

        // --------

        System.out.println("analysing...");
        fIn = new FileInputStream(new File(sFile));
        bytes = fIn.read(buffer);
        lens = 0;
        while (true) {
            if (lens == bytes - (Gas.LENSSIZE - 1)) {
                for (i = 0; lens < bytes; i++, lens++)
                    buffer[i] = buffer[lens];
                bytes = fIn.read(buffer, i, Gas.BUFFSIZE - i);
                if (bytes != -1) {
                    bytes += i;
                    lens = 0;
                } else
                    break;
            }
            analyse();
        }
        fIn.close();

        // --------

        apr.setup2(false);

        // --------

        /* TODO: write the header to fOut file */
        apr.getHeaderBytes();
        header = apr.getHeaderSize();
        outsize += header;

        System.out.println("building...");
        fIn = new FileInputStream(new File(sFile));
        bytes = fIn.read(buffer);
        lens = out = raw = 0;
        while (true) {
            if (lens >= bytes - (Gas.LENSSIZE - 1)) {
                for (i = 0; lens < bytes; i++, lens++)
                    buffer[i] = buffer[lens];
                bytes = fIn.read(buffer, i, Gas.BUFFSIZE - i);
                if (bytes != -1) {
                    bytes += i;
                    lens = 0;
                } else
                    break;
            }
            write();
        }
        for (j = 0; j < i; j++)
            setraw(buffer[j]);
        checkraw(1);
        if (out > 0) {
            outsize += out;
            /* TODO: write output buffer to fOut file */
        }
        fIn.close();


        System.out.println("file size: " + filesize + " bytes");
        System.out.println((filesize / Math.pow(1024, 2)) + " Mb, " + (filesize / Math.pow(1024, 1)) + " Kb");
        System.out.println("header: " + header + " - L" + Gas.LENSSIZE);
        System.out.println("guider: " + guider);
        System.out.println("rawdata: " + rawdata);
        long sum = header + guider + rawdata;
        System.out.println("new size: " + sum + " bytes ( " + (filesize - sum) + " cutted )");
        System.out.println((sum / Math.pow(1024, 2)) + " Mb, " + (sum / Math.pow(1024, 1)) + " Kb");
        System.out.println(">" + outsize);

    }// end of build()

    private void scan() {
        filesize++;
        int dec, i, j, k;
        double value;
        for (i = Gas.MIN_LENSSIZE; i <= Gas.LENSSIZE; i++) {
            value = k = 0;
            for (j = lens + i - 1; j >= lens; j--) {
                dec = (buffer[j] >= 0) ? buffer[j] : buffer[j] + 256;
                value += dec * Gas.pow256(k++);
            }

            apr.pointToElement(value);
        }

        lens++;
    }// end of scan

    private void analyse() {
        int dec, i, j, k;
        double value;
        for (i = Gas.MIN_LENSSIZE; i <= Gas.LENSSIZE; i++) {
            value = k = 0;
            for (j = lens + i - 1; j >= lens; j--) {
                dec = (buffer[j] >= 0) ? buffer[j] : buffer[j] + 256;
                value += dec * Gas.pow256(k++);
            }

            apr.recycle(value, i);
        }

        lens++;
    } // end of analyse()

    private void write() {
        int dec, i, j, k, maxlens;
        double value;
        byte rank;

        rank = 0;
        maxlens = 0;
        for (i = Gas.MIN_LENSSIZE; i <= Gas.LENSSIZE; i++) {
            value = k = 0;
            for (j = lens + i - 1; j >= lens; j--) {
                dec = (buffer[j] >= 0) ? buffer[j] : buffer[j] + 256;
                value += dec * Gas.pow256(k++);
            }

            rank = apr.getRank(value);
            if (rank != -1) maxlens = i;
        }

        if (maxlens == 0) {
            for (i = 0; i < Gas.LENSSIZE; i++)
                setraw(buffer[lens++]);
        } else {
            guider++;

            checkraw(1);
            dec = rank + (128); // (1 * 2 ^ 7)
            dec = (dec <= 127) ? dec : dec - 256;
            output((byte) dec);
            lens += maxlens;

        }

    }// end of write()

    private void output(byte b) {
        outbuff[out++] = b;
        if (out == Gas.BUFFSIZE) {
            outsize += Gas.BUFFSIZE;
            /* TODO: write output buffer to fOut file */
            out = 0;
        }
    } // end of output(byte)

    private void setraw(byte b) {
        rawbuff[raw++] = b;
        checkraw(Gas.SELECTED);
    } // end of setraw(byte)

    private void checkraw(int raw_value) {
        if (raw >= raw_value) {
            guider++;
            rawdata += raw;
            output((byte) (raw - 1));
            for (int i = 0; i < raw; i++)
                output(rawbuff[i]);
            raw = 0;
        }
    } // end of checkraw(int)
}
