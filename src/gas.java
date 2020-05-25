import java.io.IOException;

public class gas {
    public static void main(String[] args) {
        switch (args.length) {
            case 0:
                System.out.println("Source file has not mentioned.");
                System.exit(0);

            case 1:
                try {
                    new GasEncoder()
                            .data(args[0])
                            .build();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                System.exit(0);

            case 2:
                try {
                    new GasEncoder()
                            .data(args[0], args[1])
                            .build();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                System.exit(0);

            default:
                System.out.println("Multiple files are not allowed.");
        }
    }
}
