public class ApproximatePriority {
    private final int HASH_VALUE = 100000000;
    private final int PRUNE = 1128;

    private Node[] table;
    private int[] priorityNodes;
    private int pri = 0;
    private Galaxy galaxy;
    private int nodes;
    private int pri_elements;
    private int header_size;

    ApproximatePriority(int pri_elements) {
        this.pri_elements = pri_elements;
        this.table = new Node[HASH_VALUE];
        this.galaxy = new Galaxy();
        for (int i = 0; i < HASH_VALUE; i++)
            this.table[i] = null;
    }

    public void pointToElement(double element) {
        int index = (int) (element % HASH_VALUE);
        if (table[index] == null) {
            table[index] = new Node();
            nodes++;
        }
        table[index].points++;
    } // end of pointToElement(double)

    public void recycle(double element, int element_lens) {
        int index = (int) (element % HASH_VALUE);
        if (table[index].ispriority) {
            if (galaxy.find(element) == null) {
                galaxy = galaxy.colony(element, PRUNE);
                try {
                    galaxy.getPlanet().model = (byte) element_lens;
                } catch (Exception e) {
                    System.out.println("<null planet>");
                }
            } else
                galaxy.getPlanet().power++;
        }
    } // end of recycle(double, int)

    public byte getRank(double element) {
        return (galaxy.find(element) != null) ? galaxy.getPlanet().rank : -1;
    } // end of getRank(double)

    public void setup1(boolean print) {
        int i, j, max, index;
        if (print) {
            priorityNodes = new int[pri_elements];
        }

        for (j = 0; j < pri_elements; j++) {
            max = index = 0;
            for (i = 0; i < HASH_VALUE; i++) {
                if (table[i] == null || table[i].ispriority)
                    continue;
                if (table[i].points > max) {
                    max = table[i].points;
                    index = i;
                }
            }
            if (max != 0) {
                table[index].ispriority = true;
                if (print) {
                    priorityNodes[pri++] = index;
                }
            } else
                break;
        }

        if (print) {
            for (i = 0; i < pri; i++)
                System.out.println("index " + priorityNodes[i] + "\t\t (" + table[priorityNodes[i]].points + ")");
        }
        System.out.println("nodes count: " + nodes);
    } // end of setup1(boolean)

    public void setup2(boolean print) {
        table = null;
        priorityNodes = null;
        galaxy.sort();

        int i = 0;
        Galaxy.Colony c = galaxy.getList();
        while (c != null && i < pri_elements) {
            if (print) {
                System.out.println("pattern " + c.planet.life + "\t\t (" + c.planet.power + ")");
            }
            c.planet.rank = (byte) i++;
            c = c.next;
        }
        System.out.println("total patterns: " + galaxy.getCount());
        System.out.println("total prunes: " + galaxy.getPrune());

        pri_elements = i;
        System.out.println("selected patterns: " + pri_elements);
    } // end of setup2(boolean)

    public byte[] getHeaderBytes() {
        int i, j, k, dec;
        byte model;
        double value;
        Galaxy.Colony c;

        byte[] x = new byte[2112]; // 64+128*16
        int xp = 0;
        x[xp++] = (byte) (pri_elements - 1);

        boolean[] y = new boolean[8];
        int yp = 3;
        int yc;

        int[] z = new int[Gas.LENSSIZE];
        int zp;

        i = 0;
        c = galaxy.getList();
        while (i++ < pri_elements) {
            model = c.planet.model;
            yc = 0;
            while (model > 0) {
                y[yp--] = model % 2 != 0;
                model /= 2;
                yc++;
            }
            while (yc++ < 4) {
                y[yp--] = false;
            }
            if (yp == 3) { // means it is was 7 before (a complete byte)
                k = dec = 0;
                for (j = 7; j >= 0; j--) {
                    dec += (!y[j] ? 0 : 1) * Math.pow(2, k++);
                }
                dec = (dec <= 127) ? dec : dec - 256;
                x[xp++] = (byte) dec;
            }
            yp = (yp == -1) ? 7 : 3;

            c = c.next;
        }

        if (yp == 7) {
            for (j = 7; j >= 4; j--) {
                y[j] = false;
            }
            k = dec = 0;
            for (j = 7; j >= 0; j--) {
                dec += (!y[j] ? 0 : 1) * Math.pow(2, k++);
            }
            dec = (dec <= 127) ? dec : dec - 256;
            x[xp++] = (byte) dec;
        }

        i = 0;
        c = galaxy.getList();
        while (i++ < pri_elements) {
            System.out.println("\nLIFE= " + c.planet.life + " , MODEL= " + c.planet.model);
            value = c.planet.life;
            if (value == 0) {
                for (j = 0; j < c.planet.model; j++) {
                    System.out.println("> value= " + 0 + " , j= " + j);
                    z[j] = 0;
                }
            } else {
                zp = c.planet.model - 1;
                while (value > 0) {
                    System.out.println("> value= " + value + " , zp= " + zp);
                    z[zp--] = (int) (value % 256);
                    value = (int) value / 256;
                }
            }

            for (zp = 0; zp < c.planet.model; zp++) {
                dec = (z[zp] <= 127) ? z[zp] : z[zp] - 256;
                x[xp++] = (byte) dec;
            }

            c = c.next;
        }

        header_size = xp;
        return x;
    }// end of getHeaderBytes()

    public int getHeaderSize() {
        return header_size;
    }// end of getHeaderSize()

    private static class Node // 5.08 bytes
    {
        int points = 0;
        boolean ispriority = false;
    }

}
