package ca.korenevskiy;

public class Program {

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid number of file paths. There must be 2 file paths.");
        }

        Backuper backuper = Backuper.getInstance(args[0], args[1]);
        backuper.backupDirectory();
    }
}
