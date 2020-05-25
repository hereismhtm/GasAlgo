public class Galaxy {
    private final int SOLARS_NUM = 10;
    private final int LIMIT_NUM = 100000;

    private Colony[] solars;
    private Colony coloniesList = null;
    private Planet lastPlanet = null;
    private long coloniesCount;
    private int coloniesPrune;

    Galaxy() {
        this.solars = new Colony[SOLARS_NUM];
        for (int i = 0; i < SOLARS_NUM; i++)
            this.solars[i] = null;
    }

    public void setPrune(int prune) {
        this.coloniesPrune = prune;
    }// end of setPrune(int)

    public Colony getList() {
        return this.coloniesList;
    } // end of getList()

    public Planet getPlanet() {
        return this.lastPlanet;
    } // end of getPlanet()

    public long getCount() {
        return this.coloniesCount;
    } // end of getCount()

    public int getPrune() {
        return this.coloniesPrune;
    } // end of getPrune()

    public Galaxy colony(double life, int prune) {
        colony(life);
        if (coloniesCount > LIMIT_NUM) {
            Galaxy g = new Galaxy();
            sort();
            int i = 0;
            while (i < prune && coloniesList != null) {
                g.colony(coloniesList.planet.life).power = coloniesList.planet.power;
                coloniesList = coloniesList.next;
                i++;
            }
            solars = null;
            coloniesList = null;
            g.setPrune(coloniesPrune + 1);
            return g;
        } else
            return this;
    } // end of colony(double, int)

    public Planet colony(double life) {
        double val = life;
        Colony colony;
        Moon moon;
        int way = (int) (val % SOLARS_NUM);
        val /= SOLARS_NUM;
        if (solars[way] == null) {
            colony = new Colony();
            solars[way] = colony;
        } else {
            colony = solars[way];
        }

        while (val > 0) {
            way = (int) (val % SOLARS_NUM);
            val /= SOLARS_NUM;

            moon = colony.moons;
            while (moon != null && moon.location != way)
                moon = moon.next;

            if (moon == null) {
                moon = new Moon((byte) way);
                moon.next = colony.moons;
                colony.moons = moon;
            }
            colony = moon.station;
        }

        colony.planet = new Planet(life);
        colony.next = coloniesList;
        coloniesList = colony;
        coloniesCount++;

        lastPlanet = colony.planet;
        return colony.planet;
    } // end of colony(double)

    public Planet find(double life) {
        Colony colony;
        Moon moon;
        int way = (int) (life % SOLARS_NUM);
        life /= SOLARS_NUM;
        if (solars[way] == null)
            return null;
        else
            colony = solars[way];

        while (life > 0) {
            way = (int) (life % SOLARS_NUM);
            life /= SOLARS_NUM;

            moon = colony.moons;
            while (moon != null && moon.location != way)
                moon = moon.next;

            if (moon == null)
                return null;
            else
                colony = moon.station;
        }

        lastPlanet = colony.planet;
        return colony.planet;
    } // end of find(double)

    public Planet bring(double life) {
        Colony colony;
        Moon moon;
        int way = (int) (life % SOLARS_NUM);
        life /= SOLARS_NUM;
        colony = solars[way];

        while (life > 0) {
            way = (int) (life % SOLARS_NUM);
            life /= SOLARS_NUM;

            moon = colony.moons;
            while (moon.location != way)
                moon = moon.next;

            colony = moon.station;
        }

        lastPlanet = colony.planet;
        return colony.planet;
    } // end of bring(double)

    public void sort() {
        if (coloniesList == null)
            return;

        boolean flag;
        Colony prev, p1, p2;

        while (true) {
            flag = false;
            prev = null;
            p1 = coloniesList;
            p2 = coloniesList.next;

            while (p2 != null) {
                if (p1.planet.power < p2.planet.power) {
                    flag = true;
                    p1.next = p2.next;
                    p2.next = p1;
                    if (prev == null) {
                        prev = p2;
                        coloniesList = p2;
                    } else {
                        prev.next = p2;
                        prev = p2;
                    }
                    p2 = p1.next;

                } else {
                    prev = p1;
                    p1 = p2;
                    p2 = p2.next;
                }
            }

            if (!flag) break;
        }
    } // end of sort()

    public class Colony // 6 bytes
    {
        Planet planet = null;
        Moon moons = null;
        Colony next = null;
    }

    public class Moon // 5 bytes
    {
        byte location;
        Colony station;
        Moon next = null;

        Moon(byte location) {
            this.location = location;
            this.station = new Colony();
        }
    }

    public class Planet // 14 bytes
    {
        double life;
        int power = 1;
        byte rank = -1;
        byte model = 0;

        Planet(double life) {
            this.life = life;
        }
    }
}
