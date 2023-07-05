package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 * @author Yuhan Zhang
 */
public class Repository {

    /* Directories */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The object directory. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The reference directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The log directory. */
    public static final File LOGS_DIR = join(GITLET_DIR, "logs");

    /* Files */

    /** The reference to the current branch file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The staging area. */
    public static final File INDEX = join(GITLET_DIR, "index");

    /* Methods */

    /** Creates a new Gitlet version-control system in the current directory. */
    public static void initialize() {
        // Abort when there is already a gitlet repository in the current directory.
        if (GITLET_DIR.exists()) {
            exit("A Gitlet version-control system already exists in the current directory.");
        }

        // Initializes the directories.
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        LOGS_DIR.mkdir();
        join(REFS_DIR, "heads").mkdir();
        join(LOGS_DIR, "refs").mkdir();
        join(LOGS_DIR, "refs", "heads").mkdir();

        // Creates staging area.
        writeObject(INDEX, new Index());

        // Creates default branch.
        Branch master = new Branch("master");
        writeContents(HEAD, master.getPath().toString());

        // Creates initial commit.
        Commit init = new Commit();
        init.commit();
    }

    /** Adds a copy of the file as it currently exists to the staging area. */
    public static void addFile(String fileName) {
        // File does not exist.
        File file = join(CWD, fileName);
        if (!file.exists()) {
            exit("File does not exist.");
        }
        Index idx = getIndex();
        idx.add(fileName, file);
    }

    /** Stages the file for removal and remove the file if it is tracked in the current commit. */
    public static void rmFile(String fileName) {
        // File does not exist.
        File file = join(CWD, fileName);
        Index idx = getIndex();
        idx.remove(fileName, file);
    }

    /** Adds a copy of the file as it currently exists to the staging area. */
    public static void commit(String message) {
        // Commit must have a non-blank message.
        if (message.length() == 0) {
            exit("Please enter a commit message.");
        }
        Commit c = new Commit(message);
        c.commit();
    }

    /** Starting at the current head commit, displays each commit backwards until the initial commit. */
    public static void log() {
        Branch b = getCurrentBranch();
        System.out.print(readContentsAsString(b.getLogFile()));
    }

    /** Displays information about all commits ever made. */
    public static void globalLog() {
        for (Commit c : Commit.findAll()) {
            System.out.println(c.toString());
        }
    }

    /** Displays what branches currently exist, (marking the current branch with a *) and
     *  what files have been staged for addition or removal. */
    public static void status() {
        System.out.print(getIndex().toString());
    }

    /** Takes the version of the file as it exists in the commit with the given id, and puts it in the working
     *  directory, overwriting the version of the file that’s already there if there is one. */
    public static void checkoutFile(String commitID, String fileName) {
        if (commitID == null) {
            commitID = getCurrentCommit().getUID();
        }
        Blob b = Commit.findBlob(commitID, join(CWD, fileName));
        b.overwrite();
    }

    /** Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *  overwriting the versions of the files that are already there if they exist. */
    public static void checkoutBranch(String branchName) {
        exit("Not implemented yet.");
    }

    /** Returns the object of the current working branch. */
    public static Branch getCurrentBranch() {
        return readObject(join(GITLET_DIR, readContentsAsString(HEAD)), Branch.class);
    }

    /** Returns the object of the current (latest) commit. */
    public static Commit getCurrentCommit() {
        return getCurrentBranch().getCommit();
    }

    /** Returns the repository staging area. */
    public static Index getIndex() {
        return readObject(INDEX, Index.class);
    }

}
